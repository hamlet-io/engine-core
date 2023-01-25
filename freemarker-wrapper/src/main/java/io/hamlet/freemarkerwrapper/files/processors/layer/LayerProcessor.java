package io.hamlet.freemarkerwrapper.files.processors.layer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import freemarker.core.Environment;
import freemarker.template.DefaultMapAdapter;
import freemarker.template.TemplateModelException;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.FileFinder;
import io.hamlet.freemarkerwrapper.files.layers.Layer;
import io.hamlet.freemarkerwrapper.files.meta.Meta;
import io.hamlet.freemarkerwrapper.files.meta.layer.LayerMeta;
import io.hamlet.freemarkerwrapper.files.processors.Processor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import jakarta.json.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class LayerProcessor extends Processor {

    protected Map<String, String> fileSystem;
    protected Map<String, Layer> layerMap = new LinkedHashMap<>();
    protected String fileSystemShareVariableName;
    protected String layerMapShareVariableName;
    private final Map<String, List<Path>> filesPerLayer = new LinkedHashMap<>();

    public abstract void createLayerFileSystem(LayerMeta meta) throws TemplateModelException, IOException;

    public abstract void postProcessMeta(LayerMeta meta);

    public abstract JsonObjectBuilder buildInformation(JsonObjectBuilder jsonObjectBuilder, Layer layer, Path file);

    public abstract JsonArrayBuilder buildLayers(LayerMeta meta);


    protected void setSharedVariables() throws TemplateModelException{
        Environment.getCurrentEnvironment().getConfiguration().setSharedVariable(fileSystemShareVariableName, fileSystem);
        Environment.getCurrentEnvironment().getConfiguration().setSharedVariable(layerMapShareVariableName, layerMap);
    }

    public JsonArray getLayers(LayerMeta meta) throws TemplateModelException, IOException {
        checkFileSystemAndPostProcessMeta(meta);
        return buildLayers(meta).build();
    }

    public void initLayers(LayerMeta meta) throws TemplateModelException, IOException {
        if (meta.getStartingPath() != null && !meta.getStartingPath().startsWith("/")) {
            meta.setStartingPath("/".concat(meta.getStartingPath()));
        }
        createLayerFileSystem(meta);
    }

    public int mkdirLayers(LayerMeta meta) throws TemplateModelException, IOException {
        return checkFileSystemAndPostProcessMeta(meta);
    }


    private int checkFileSystemAndPostProcessMeta(LayerMeta meta) throws TemplateModelException, IOException {
        if (meta.getStartingPath() != null && !meta.getStartingPath().startsWith("/")) {
            meta.setStartingPath("/".concat(meta.getStartingPath()));
        }
        checkFileSystem(meta);
        postProcessMeta(meta);
        return 0;
    }

    public int rmLayers(LayerMeta meta) throws TemplateModelException, IOException {
        return checkFileSystemAndPostProcessMeta(meta);
    }

    public int toMethod(Meta meta) throws TemplateModelException, IOException, CloneNotSupportedException {
        return checkFileSystemAndPostProcessMeta((LayerMeta) meta);
    }

    public int cpLayers(Meta meta) throws TemplateModelException, IOException, CloneNotSupportedException {
        return checkFileSystemAndPostProcessMeta((LayerMeta)meta);
    }

    protected void copy(Path source, Path destination, CopyOption copyOption) throws IOException {
        if (copyOption != null) {
            Files.copy(source, destination, copyOption);
        } else {
            Files.copy(source, destination);
        }
    }

    public Set<JsonObject> getLayerTree(LayerMeta meta) throws TemplateModelException, IOException, CloneNotSupportedException {
        checkFileSystemAndPostProcessMeta(meta);

        Set<JsonObject> output = new LinkedHashSet<>();
        if (fileSystem == null) {
            return output;
        }

        Map<String, Path> files = new LinkedHashMap<>();
        Map<String, String> layerFilesMapping = new LinkedHashMap<>();
        Set<String> layerFilesMappingCaseInsensitive = new HashSet<>();
        Map<String, String> layerPhysicalFilesMapping = new LinkedHashMap<>();

        List<String> refinedRegexList = refineRegexList(meta.getStartingPath(), meta.getRegexList(),
                meta.isAddStartingWildcard(), meta.isAddEndingWildcard());
        Set<String> layersToSkip = new HashSet<>();
        for (String layerName : meta.getLayersNames()) {
            Layer layer = layerMap.get(layerName);
            /**
             * Check is starting path exists in a layer
             */
            boolean skipLayer = true;
            /**
             * Don't skip a layer if its path starts with a starting path
             * layer path - /products/api
             * starting path - /products
             */
            /**
             * Don't skip a layer if a starting path starts with its path and the resolved path exists
             * layer path - /products/api
             * starting path - /products/api/config
             */
            if (getDirectoryOnFileSystem(meta.getStartingPath(), layer.getPath(), layer.getFileSystemPath()) != null) {
                skipLayer = false;
            }
            if (skipLayer) {
                layersToSkip.add(layerName);
            }
        }

        Path matchParentPath = null;

        boolean matchFound = false;
        for (String regex : refinedRegexList) {
            for (String layerName : meta.getLayersNames()) {
                Layer layer = layerMap.get(layerName);
                if (layersToSkip.contains(layerName)) {
                    continue;
                }

                Pattern p = null;
                if (meta.isCaseSensitive()) {
                    p = Pattern.compile(regex);
                } else {
                    p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                }

                // get physical starting dir for the current layer
                for (Path file : getFilesPerLayer(meta, layer)) {
                    if (matchParentPath != null && meta.isIgnoreSubtreeAfterMatch() && file.getParent().startsWith(matchParentPath))
                        continue;
                    String path = file.toString();
                    String layerPath = getPathOnLayerFileSystem(path, layer);
                    matchFound = false;
                    Matcher m = p.matcher(layerPath);
                    if (m.matches()) {
                        if (meta.isCaseSensitive()) {
                            if (!layerFilesMapping.containsKey(layerPath)) {
                                matchFound = true;
                            }
                        } else {
                            String layerPathLowerCase = layerPath.toLowerCase();
                            if (!layerFilesMappingCaseInsensitive.contains(layerPathLowerCase)) {
                                matchFound = true;
                                layerFilesMappingCaseInsensitive.add(layerPathLowerCase);
                            }
                        }
                        if (matchFound) {
                            matchParentPath = file.getParent();

                            layerFilesMapping.put(layerPath, path);
                            layerPhysicalFilesMapping.put(path, layer.getName());

                            if (meta.isStopAfterFirstMatch())
                                break;
                        }
                    }
                }
                if (meta.isStopAfterFirstMatch() && matchFound)
                    break;
            }
            if (meta.isStopAfterFirstMatch() && matchFound)
                break;
        }

        for (String file : layerFilesMapping.keySet()) {
            files.put(forceUnixStyle(file), Paths.get(layerFilesMapping.get(file)));
        }

        for (String key : files.keySet()) {
            Path file = files.get(key);
            Boolean isDirectoryFlag = Files.isDirectory(file);
            if (isDirectoryFlag) {
                if (meta.isIgnoreDirectories())
                    continue;
            } else {
                if (meta.isIgnoreFiles())
                    continue;
            }

            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
            String path = StringUtils.substringBeforeLast(key, file.getFileName().toString());
            String extension = StringUtils.substringAfterLast(file.getFileName().toString(), ".");
            jsonObjectBuilder.add("File", key);
            jsonObjectBuilder.add("Path", forceUnixStyle(path));
            jsonObjectBuilder.add("Filename", file.getFileName().toString());
            jsonObjectBuilder.add("Extension", extension);
            if (isDirectoryFlag) {
                jsonObjectBuilder.add("IsDirectory", Boolean.TRUE);
            } else {
                jsonObjectBuilder.add("IsDirectory", Boolean.FALSE);
                String contents = null;
                try (FileInputStream inputStream = new FileInputStream(file.toString())) {
                    contents = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (contents != null) {
                    jsonObjectBuilder.add("Contents", contents);
                }
                try {
                    /**
                     * Check if a file is a freemarker template - starts with [#ftl]
                     * if not, attempt to parse it as a json file
                     */
                    try (Stream<String> lines = Files.lines(file)) {
                        String firstNonBlankLine = null;
                        for (String line : (Iterable<String>) lines::iterator) {
                            if (StringUtils.isNotBlank(line)) {
                                firstNonBlankLine = StringUtils.trim(line);
                                break;
                            }
                        }
                        if (firstNonBlankLine != null) {
                            if (StringUtils.startsWith(firstNonBlankLine, "[#ftl]")) {
                                jsonObjectBuilder.add("IsTemplate", Boolean.TRUE);
                            } else {
                                JsonReader reader = null;
                                switch (extension.toLowerCase()) {
                                    case "json":
                                        reader = Json.createReader(new StringReader(contents));
                                        break;
                                    case "yaml":
                                    case "yml":
                                        try {
                                            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
                                            Object obj = yamlReader.readValue(contents, Object.class);
                                            ObjectMapper jsonWriter = new ObjectMapper();
                                            String json = jsonWriter.writeValueAsString(obj);
                                            reader = Json.createReader(new StringReader(json));
                                        } catch (JsonProcessingException e) {
                                            //do nothing
                                            System.err.println(String.format("Unable to convert yaml file %s to json. Error details: %s", file, e.getMessage()));
                                        }
                                        break;
                                }
                                if (reader != null) {
                                    try {
                                        JsonStructure jsonStructure = reader.read();
                                        reader.close();
                                        jsonObjectBuilder.add("ContentsAsJSON", cleanUpJson(jsonStructure));
                                    } catch (JsonException e) {
                                        //do nothing
                                        System.err.println(String.format("Unable to parse json file %s. Error details: %s", file, e.getMessage()));
                                    }
                                }
                            }
                        }
                    }
                } catch (UncheckedIOException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (meta.isIncludeInformation()) {
                String layerName = layerPhysicalFilesMapping.get(file.toString());
                jsonObjectBuilder = buildInformation(jsonObjectBuilder, layerMap.get(layerName), file);
            }
            output.add(jsonObjectBuilder.build());
        }
        return output;
    }

    protected Path readJSONFileUsingDirectoryStream(String dir, String fileName) throws IOException {
        if (Files.isDirectory(Paths.get(dir))) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
                for (Path path : stream) {
                    if (!Files.isDirectory(path)) {
                        if (fileName.equals(path.getFileName().toString())) {
                            return path;
                        }
                    }
                }
            } catch (NullPointerException e) {
                System.err.println(String.format("Cannot find path %s", dir));
            }
        }

        return null;
    }

    protected String forceUnixStyle(final String path) {
        return forceUnixStyle(path, false);
    }

    protected String forceUnixStyle(final String path, boolean directoryWithEndingSlash) {
        String result = StringUtils.replaceEachRepeatedly(path, new String[]{"\\", "//"}, new String[]{"/", "/"});
        if (directoryWithEndingSlash && !result.endsWith("/")) {
            result = result.concat("/");
        } else {
            if (!"/".equalsIgnoreCase(result) && result.endsWith("/"))
                result = StringUtils.substringBeforeLast(result, "/");
        }
        return result;
    }

    protected List<String> refineRegexList(final String startingDir, final List<String> regexList,
                                           boolean addStartingWildcard, boolean addEndingWildcard) {
        List<String> result = new ArrayList<>();

        for (String regex : regexList) {
            if (!regex.startsWith("^")) {
                if (addStartingWildcard) {
                    regex = ".*".concat(regex);
                }
                regex = StringUtils.join("^", getStartingDir(startingDir), regex);
            }
            if (!regex.endsWith("$")) {
                if (addEndingWildcard) {
                    regex = regex.concat(".*");
                }
                regex = regex.concat("$");
            }
            result.add(regex);
        }
        return result;
    }

    protected String getStartingDir(final String startingDir) {
        if (StringUtils.endsWith(startingDir, "/")) {
            return startingDir;
        } else {
            return startingDir.concat("/");
        }
    }

    protected JsonValue cleanUpJson(JsonValue jsonValue) {
        JsonObjectBuilder jsonObjectBuilder = null;
        JsonArrayBuilder jsonArrayBuilder = null;
        if (jsonValue instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) jsonValue;
            jsonObjectBuilder = Json.createObjectBuilder(jsonObject);
            for (String s : jsonObject.keySet()) {
                if (jsonObject.get(s) == JsonValue.NULL) {
                    jsonObjectBuilder.remove(s);
                } else {
                    jsonObjectBuilder.add(s, cleanUpJson(jsonObject.get(s)));
                }
            }
            return jsonObjectBuilder.build();
        } else if (jsonValue instanceof JsonArray) {
            JsonArray jsonArray = (JsonArray) jsonValue;
            jsonArrayBuilder = Json.createArrayBuilder();
            Iterator<JsonValue> iterator = jsonArray.iterator();
            while (iterator.hasNext()) {
                JsonValue jsonValue1 = iterator.next();
                if (jsonValue1.getValueType() != JsonValue.ValueType.NULL) {
                    jsonArrayBuilder.add(cleanUpJson(jsonValue1));
                }
            }
            return jsonArrayBuilder.build();
        } else {
            return jsonValue;
        }
    }

    private List<Path> getFilesPerLayer(LayerMeta meta, Layer layer) {
        List<Path> files = filesPerLayer.get(layer.getName());
        if (files == null) {
            // search starting point optimisation, see https://github.com/hamlet-io/engine-core/issues/22
            Path startingDir = getDirectoryOnFileSystem(meta.getStartingPath(), layer.getPath(), layer.getFileSystemPath());
            if (startingDir == null)
                return files;
            FileFinder.Finder finder = new FileFinder.Finder(meta.getFilenameGlob(), meta.isIgnoreDotDirectories(), meta.isIgnoreDotFiles());
            String relativeLayerPath = StringUtils.substringAfter(forceUnixStyle(layer.getPath()), forceUnixStyle(meta.getStartingPath()).concat("/"));
            int relativeLayerPathDepth = StringUtils.split(relativeLayerPath, "/").length;
            Integer depth = Integer.MAX_VALUE;
            if (meta.getMaxDepth() != null) {
                depth = meta.getMaxDepth();
            }
            // adjust max depth based on layer path related to the starting path
            // for example if starting path is set to /products
            // and maxDepth=3 and layer path is /products/almv2
            // depth needs to be decreased by 1
            // as the layer one layer deeper on the layer filesystem than on physical one
            depth -= relativeLayerPathDepth;
            if (depth <= 0)
                return files;
            try {
                Files.walkFileTree(startingDir, EnumSet.of(FileVisitOption.FOLLOW_LINKS), depth, finder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            files = finder.done();
            if (meta.getMinDepth() != null) {
                List<Path> list = new ArrayList<>();
                list.addAll(files);
                for (Path path : list) {
                    String layerFilePath = StringUtils.replaceOnce(forceUnixStyle(path.toString()), forceUnixStyle(Paths.get(layer.getFileSystemPath()).toString()), forceUnixStyle(layer.getPath()));
                    String relativeLayerFilePath = StringUtils.substringAfter(layerFilePath, forceUnixStyle(meta.getStartingPath()));
                    int relativeLayerFilePathDepth = StringUtils.split(relativeLayerFilePath, "/").length;
                    // exclude files that do not meet min depth requirements
                    // relative layer path is checked here as well as for max depth
                    if (relativeLayerFilePathDepth < meta.getMinDepth()) {
                        files.remove(path);
                    }
                }
            }
            filesPerLayer.put(layer.getName(), files);
        }
        return files;
    }

    /**
     * Returns the starting point on the physical file system
     *
     * @param startingPath
     * @param layerPath
     * @param fileSystemPath
     * @return
     */
    protected Path getDirectoryOnFileSystem(String startingPath, String layerPath, String fileSystemPath) {
        startingPath = forceUnixStyle(startingPath, true);
        fileSystemPath = forceUnixStyle(fileSystemPath, true);
        layerPath = forceUnixStyle(layerPath, true);
        if (StringUtils.equalsIgnoreCase("/", layerPath)) {
            fileSystemPath = fileSystemPath.concat("/");
        }
        String path;
        if (layerPath.startsWith(startingPath)) {
            // if layer path includes the starting path,
            // the physical filesystem path is the starting point
            // physical filesystem path - /var/opt/hamlet/api
            // layer path - /products/api
            // starting path - /products
            // starting point - /var/opt/hamlet/api
            path = fileSystemPath;
        } else {
            // if layer path does not include the starting path,
            // relative layer path is added to the physical filesystem path to adjust the starting point
            // physical filesystem path - /var/opt/hamlet/api
            // layer path - /products/api
            // starting path - /products/api/config
            // starting point - /var/opt/hamlet/api/config
            if (startingPath.startsWith(layerPath)) {
                path = StringUtils.replaceOnce(startingPath, layerPath, fileSystemPath);
            } else {
                path = null;
            }
        }
        try {
            if (path != null && Files.isDirectory(Paths.get(path))) {
                return Paths.get(path);
            } else {
                return null;
            }
        } catch (InvalidPathException e) {
            e.printStackTrace();
            return null;
        }

    }

    private String getPathOnLayerFileSystem(String pathOnFileSystem, Layer layer) {
        return forceUnixStyle(StringUtils.replaceOnce(pathOnFileSystem, Paths.get(layer.getFileSystemPath()).toString(), layer.getPath()));
    }

    protected Path getExistingDirectory(Set<String> layerNames, String directoryPath) {
        for (String layerName : layerNames) {
            Layer layer = layerMap.get(layerName);
            Path destinationDirectory = getDirectoryOnFileSystem(directoryPath, layer.getPath(), layer.getFileSystemPath());
            if (destinationDirectory != null && Files.exists(destinationDirectory) && Files.isDirectory(destinationDirectory)) {
                return destinationDirectory;
            }
        }
        return null;
    }

    protected List<Path> getFilesPerLayerMeta(LayerMeta meta) {
        for (String layerName : meta.getLayersNames()) {
            Layer layer = layerMap.get(layerName);
            List<Path> files = getFilesPerLayer(meta, layer);
            if (files != null)
                return files;
        }
        return null;
    }

    protected void checkFileSystem(LayerMeta meta) throws TemplateModelException, IOException {
        if (Environment.getCurrentEnvironment().getConfiguration().getSharedVariableNames().contains(fileSystemShareVariableName)) {
            fileSystem = (TreeMap) ((DefaultMapAdapter) Environment.getCurrentEnvironment().getConfiguration().getSharedVariable(fileSystemShareVariableName)).getWrappedObject();
        }

        if (Environment.getCurrentEnvironment().getConfiguration().getSharedVariableNames().contains(layerMapShareVariableName)) {
            layerMap = (LinkedHashMap) ((DefaultMapAdapter) Environment.getCurrentEnvironment().getConfiguration().getSharedVariable(layerMapShareVariableName)).getWrappedObject();
        }
        if (fileSystem == null) {
            createLayerFileSystem(meta);
        }
    }
}
