package io.codeontap.freemarkerwrapper.files.processors;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import freemarker.template.Configuration;
import freemarker.template.DefaultMapAdapter;
import freemarker.template.TemplateModelException;
import io.codeontap.freemarkerwrapper.files.FileFinder;
import io.codeontap.freemarkerwrapper.files.layers.Layer;
import io.codeontap.freemarkerwrapper.RunFreeMarkerException;
import io.codeontap.freemarkerwrapper.files.meta.LayerMeta;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.json.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class LayerProcessor {

    protected Map<String, String> fileSystem;
    protected Map<String, Layer> layerMap = new LinkedHashMap<>();
    private Map<String, List<Path>> filesPerLayer = new LinkedHashMap<>();
    protected Configuration configuration;
    protected String fileSystemShareVariableName;
    protected String layerMapShareVariableName;

    public abstract void createLayerFileSystem(LayerMeta meta) throws RunFreeMarkerException;

    public abstract void postProcessMeta(LayerMeta meta);

    public abstract JsonObjectBuilder buildInformation(JsonObjectBuilder jsonObjectBuilder, Layer layer, Path file);

    public abstract JsonArrayBuilder buildLayers(LayerMeta meta);

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    protected void setSharedVariables(){
        try {
            configuration.setSharedVariable(fileSystemShareVariableName, fileSystem);
            configuration.setSharedVariable(layerMapShareVariableName, layerMap);
        } catch (TemplateModelException e) {
            e.printStackTrace();
        }
    }

    public JsonArray getLayers(LayerMeta meta) throws RunFreeMarkerException {
        createLayerFileSystem(meta);
        postProcessMeta(meta);
        return buildLayers(meta).build();
    }

    public void initLayers(LayerMeta meta) throws RunFreeMarkerException {
        if (meta.getStartingPath() != null && !meta.getStartingPath().startsWith("/")) {
            meta.setStartingPath("/".concat(meta.getStartingPath()));
        }
        createLayerFileSystem(meta);
    }

    public int mkdirLayers(LayerMeta meta) throws RunFreeMarkerException {
        if (meta.getStartingPath() != null && !meta.getStartingPath().startsWith("/")) {
            meta.setStartingPath("/".concat(meta.getStartingPath()));
        }
        createLayerFileSystem(meta);
        postProcessMeta(meta);
        Path pathToCreate = Paths.get(meta.getStartingPath());
        Path pathToScan = Paths.get(meta.getStartingPath());
        while (pathToScan != null) {
            for (String layerName : meta.getLayersNames()) {
                Layer layer = layerMap.get(layerName);
                Path result = getDirectoryOnFileSystem(pathToScan.toString(), layer.getPath(), layer.getFileSystemPath());
                if (result != null) {
                    if (pathToCreate.equals(pathToScan))
                        return 0;
                    String layerPath = StringUtils.substringAfter(layer.getPath(), forceUnixStyle(pathToScan.toString()));
                    // TODO: refactor this ugly layerPath calculation
                    if (layer.getPath().equalsIgnoreCase(pathToScan.toString())) {
                        layerPath = "";
                    } else if ("".equalsIgnoreCase(layerPath) && !"/".equalsIgnoreCase(pathToScan.toString()) && !"\\".equalsIgnoreCase(pathToScan.toString())) {
                        layerPath = pathToScan.toString();
                    }

                    String commonPath = StringUtils.substringBeforeLast(layer.getPath(), layerPath);
                    /*if(commonPath.equalsIgnoreCase("/"))
                        commonPath = pathToCreate.getFileSystem().getSeparator();*/

                    String newPath = StringUtils.substringAfter(forceUnixStyle(pathToCreate.toString()), commonPath);
                    Path fsNewPath = Paths.get(getStartingDir(StringUtils.substringBeforeLast(result.toString(), layerPath)).concat(newPath));
                    File newDirectory = new File(fsNewPath.toString());
                    if (meta.isParents()) {
                        if (newDirectory.mkdirs()) {
                            return 0;
                        } else {
                            return 1;
                        }
                    } else {
                        if (newDirectory.mkdir()) {
                            return 0;
                        } else {
                            return 1;
                        }
                    }
                }
            }
            pathToScan = pathToScan.getParent();
        }
        return 1;
    }

    public int rmLayers(LayerMeta meta) throws RunFreeMarkerException, IOException {
        if (meta.getStartingPath() != null && !meta.getStartingPath().startsWith("/")) {
            meta.setStartingPath("/".concat(meta.getStartingPath()));
        }
        createLayerFileSystem(meta);
        postProcessMeta(meta);
        Path sourceDirectory = getExistingDirectory(meta.getLayersNames(), meta.getToPath());
        if (sourceDirectory != null) {
            File file = sourceDirectory.toFile();
            if (meta.isForce()) {
                FileUtils.deleteQuietly(file);
            } else {
                if (meta.isRecurse())
                    FileUtils.deleteDirectory(file);
                else if (file.delete()) {
                    return 0;
                } else return 1;
            }
        } else {
            String sourceStartingPath = StringUtils.substringBeforeLast(meta.getToPath(), "/");
            String sourceFilenameGlob = StringUtils.substringAfterLast(meta.getToPath(), "/");
            meta.setStartingPath(sourceStartingPath);
            meta.setFilenameGlob(sourceFilenameGlob);
            List<Path> files = getFilesPerLayerMeta(meta);
            if (files != null) {
                for (Path file : files) {
                    if (meta.isForce()) {
                        FileUtils.deleteQuietly(file.toFile());
                    } else if (file.toFile().delete()) {
                        return 0;
                    } else return 1;
                }
            }
        }

        return 0;
    }

    public int toLayers(LayerMeta meta) throws RunFreeMarkerException, IOException {
        if (meta.getStartingPath() != null && !meta.getStartingPath().startsWith("/")) {
            meta.setStartingPath("/".concat(meta.getStartingPath()));
        }
        createLayerFileSystem(meta);
        postProcessMeta(meta);


        Object content = meta.getContent();

        final StringWriter writer = new StringWriter();
        if ("json".equalsIgnoreCase(meta.getFormat())) {
            ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
            objectMapper.writeValue(writer, content);
        } else if ("yml".equalsIgnoreCase(meta.getFormat()) || "yaml".equalsIgnoreCase(meta.getFormat())) {
            ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
            objectMapper.writeValue(writer, content);
        } else {
            writer.write(content.toString());
        }

        Path destinationFile = getDestinationPath(meta, "singleFile");
        if (meta.isAppend()) {
            Files.write(destinationFile, writer.toString().getBytes(), StandardOpenOption.APPEND);
        } else {
            Files.write(destinationFile, writer.toString().getBytes());
        }
        return 0;
    }

    public int cpLayers(LayerMeta meta) throws RunFreeMarkerException, IOException {
        if (meta.getStartingPath() != null && !meta.getStartingPath().startsWith("/")) {
            meta.setStartingPath("/".concat(meta.getStartingPath()));
        }
        createLayerFileSystem(meta);
        postProcessMeta(meta);

        CopyOption copyAttributes = null;
        if (meta.isPreserve()) {
            copyAttributes = StandardCopyOption.COPY_ATTRIBUTES;
        }


        Path sourceDirectory = getExistingDirectory(meta.getLayersNames(), meta.getFromPath());
        if (sourceDirectory != null) {
            Path destinationDirectory = getDestinationPath(meta, "directory");
            if (destinationDirectory != null) {
                int maxDepth = 1;
                if (meta.isRecurse())
                    maxDepth = Integer.MAX_VALUE;
                for (Object sourceObject : Files.walk(sourceDirectory, maxDepth).toArray()) {
                    Path source = (Path) sourceObject;
                    Path destinationFile = destinationDirectory.resolve(sourceDirectory.relativize(source));
                    if (!destinationFile.equals(destinationDirectory)) {
                        if (meta.isPreserve()) {
                            copy(source, destinationFile, StandardCopyOption.COPY_ATTRIBUTES);
                        } else {
                            copy(source, destinationFile, null);
                        }
                    }
                }
            }
        } else {
            String sourceStartingPath = StringUtils.substringBeforeLast(meta.getFromPath(), "/");
            String sourceFilenameGlob = StringUtils.substringAfterLast(meta.getFromPath(), "/");
            meta.setStartingPath(sourceStartingPath);
            meta.setFilenameGlob(sourceFilenameGlob);
            List<Path> files = getFilesPerLayerMeta(meta);
            if (files != null) {
                if (files.size() == 1) {
                    Path destinationPath = getDestinationPath(meta, "singleFile");
                    if (destinationPath != null) {
                        if (Files.isDirectory(destinationPath)) {
                            destinationPath = Paths.get(destinationPath.toString().concat("/").concat(files.get(0).getFileName().toString()));
                        }
                        copy(files.get(0), destinationPath, copyAttributes);
                        return 0;
                    }
                } else {
                    Path destinationDirectory = getDestinationPath(meta, "fileNameGlob");
                    if (destinationDirectory != null) {
                        for (Path file : files) {
                            Path destinationFile = Paths.get(destinationDirectory.toString().concat("/").concat(file.getFileName().toString()));
                            copy(file, destinationFile, copyAttributes);
                        }
                        return 0;
                    }
                }
            }
        }
        return 1;
    }

    private void copy(Path source, Path destination, CopyOption copyOption) throws IOException {
        if (copyOption != null) {
            Files.copy(source, destination, copyOption);
        } else {
            Files.copy(source, destination);
        }
    }

    public Set<JsonObject> getLayerTree(LayerMeta meta) throws RunFreeMarkerException {
        Set<JsonObject> output = new LinkedHashSet<>();

        if (meta.getStartingPath() != null && !meta.getStartingPath().startsWith("/")) {
            meta.setStartingPath("/".concat(meta.getStartingPath()));
        }

        if (configuration.getSharedVariableNames().contains(fileSystemShareVariableName)){
            fileSystem = (TreeMap)((DefaultMapAdapter)configuration.getSharedVariable(fileSystemShareVariableName)).getWrappedObject();
        }

        if (configuration.getSharedVariableNames().contains(layerMapShareVariableName)){
            layerMap = (LinkedHashMap)((DefaultMapAdapter)configuration.getSharedVariable(layerMapShareVariableName)).getWrappedObject();
        }

        if (fileSystem == null) {
            createLayerFileSystem(meta);
        }

/*
        createLayerFileSystem(meta);
*/
        postProcessMeta(meta);

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

        for (String regex : refinedRegexList) {
            for (String layerName : meta.getLayersNames()) {
                Layer layer = layerMap.get(layerName);
                if (layersToSkip.contains(layerName)) {
                    continue;
                }

                boolean matchFound = false;
                Pattern p = null;
                if (meta.isCaseSensitive()) {
                    p = Pattern.compile(regex);
                } else {
                    p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                }

                // get physical starting dir for the current layer
                for (Path file : getFilesPerLayer(meta, layer)) {
                    String path = file.toString();
                    String layerPath = getPathOnLayerFileSystem(path, layer);
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
                            layerFilesMapping.put(layerPath, path);
                            layerPhysicalFilesMapping.put(path, layer.getName());

                            if (meta.isStopAfterFirstMatch() || meta.isIgnoreSubtreeAfterMatch())
                                break;
                        }
                    }
                }
                if (meta.isStopAfterFirstMatch() && matchFound)
                    break;
            }
        }

        for (String file : layerFilesMapping.keySet()) {
            files.put(forceUnixStyle(file), Paths.get(layerFilesMapping.get(file)));
        }

        for (String key : files.keySet()) {
            Path file = files.get(key);
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
            String path = StringUtils.substringBeforeLast(key, file.getFileName().toString());
            String extension = StringUtils.substringAfterLast(file.getFileName().toString(), ".");
            jsonObjectBuilder.add("File", key);
            jsonObjectBuilder.add("Path", forceUnixStyle(path));
            jsonObjectBuilder.add("Filename", file.getFileName().toString());
            jsonObjectBuilder.add("Extension", extension);
            if (Files.isDirectory(file)) {
                jsonObjectBuilder.add("IsDirectory", Boolean.TRUE);
            } else {
                String contents = null;
                try (FileInputStream inputStream = new FileInputStream(file.toString())) {
                    contents = IOUtils.toString(inputStream);
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
                    //
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
        return null;
    }

    protected String forceUnixStyle(final String path) {
        String result = StringUtils.replaceEachRepeatedly(path, new String[]{"\\", "//"}, new String[]{"/", "/"});
        if (!"/".equalsIgnoreCase(result) && result.endsWith("/"))
            result = StringUtils.substringBeforeLast(result, "/");
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
            // search starting point optimisation, see https://github.com/codeontap/gen3-freemarker-wrapper/issues/22
            Path startingDir = getDirectoryOnFileSystem(meta.getStartingPath(), layer.getPath(), layer.getFileSystemPath());
            if (startingDir == null)
                return files;
            FileFinder.Finder finder = new FileFinder.Finder(meta.getFilenameGlob(), meta.isIgnoreDotDirectories(), meta.isIgnoreDotFiles());
            String relativeLayerPath = StringUtils.substringAfter(forceUnixStyle(layer.getPath()), forceUnixStyle(meta.getStartingPath()));
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
    private Path getDirectoryOnFileSystem(String startingPath, String layerPath, String fileSystemPath) {
        startingPath = forceUnixStyle(startingPath);
        fileSystemPath = forceUnixStyle(fileSystemPath);
        if (StringUtils.equalsIgnoreCase("/", layerPath)) {
            fileSystemPath = fileSystemPath.concat("/");
        } else {
            layerPath = forceUnixStyle(layerPath);
        }
        String path;
        if (layerPath.startsWith(startingPath)) {
            // if layer path includes the starting path,
            // the physical filesystem path is the starting point
            // physical filesystem path - /var/opt/codeontap/api
            // layer path - /products/api
            // starting path - /products
            // starting point - /var/opt/codeontap/api
            path = fileSystemPath;
        } else {
            // if layer path does not include the starting path,
            // relative layer path is added to the physical filesystem path to adjust the starting point
            // physical filesystem path - /var/opt/codeontap/api
            // layer path - /products/api
            // starting path - /products/api/config
            // starting point - /var/opt/codeontap/api/config
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
        }catch (InvalidPathException e){
            e.printStackTrace();
            return null;
        }

    }

    private String getPathOnLayerFileSystem(String pathOnFileSystem, Layer layer) {
        return forceUnixStyle(StringUtils.replaceOnce(pathOnFileSystem, Paths.get(layer.getFileSystemPath()).toString(), layer.getPath()));
    }

    private Path getExistingDirectory(Set<String> layerNames, String directoryPath) {
        for (String layerName : layerNames) {
            Layer layer = layerMap.get(layerName);
            Path destinationDirectory = getDirectoryOnFileSystem(directoryPath, layer.getPath(), layer.getFileSystemPath());
            if (destinationDirectory != null && Files.exists(destinationDirectory) && Files.isDirectory(destinationDirectory)) {
                return destinationDirectory;
            }
        }
        return null;
    }

    private List<Path> getFilesPerLayerMeta(LayerMeta meta) {
        for (String layerName : meta.getLayersNames()) {
            Layer layer = layerMap.get(layerName);
            List<Path> files = getFilesPerLayer(meta, layer);
            if (files != null)
                return files;
        }
        return null;
    }

    private Path getDestinationPath(LayerMeta sourceMeta, String sourceType) throws RunFreeMarkerException {
        if ("directory".equalsIgnoreCase(sourceType)) {
            //destination can be an existing or a new directory
            //check if destination directory exists
            Path destinationDirectory = null;
            destinationDirectory = getExistingDirectory(sourceMeta.getLayersNames(), sourceMeta.getToPath());
            //if destination directory doesn't exist, create it
            if (destinationDirectory == null) {
                try {
                    LayerMeta destinationMeta = (LayerMeta) sourceMeta.clone();
                    destinationMeta.setStartingPath(sourceMeta.getToPath());
                    destinationMeta.setParents(Boolean.FALSE);
                    mkdirLayers(destinationMeta);
                    destinationDirectory = getExistingDirectory(sourceMeta.getLayersNames(), sourceMeta.getToPath());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
            return destinationDirectory;
        }
        if ("singleFile".equalsIgnoreCase(sourceType)) {
            // destination can be an existing directory or a file in an existing directory
            Path destinationDirectory = getExistingDirectory(sourceMeta.getLayersNames(), sourceMeta.getToPath());
            if (destinationDirectory != null) {
                return destinationDirectory;
            } else {
                String parentDestination = StringUtils.substringBeforeLast(sourceMeta.getToPath(), "/");
                String destinationFileName = StringUtils.substringAfterLast(sourceMeta.getToPath(), "/");
                destinationDirectory = getExistingDirectory(sourceMeta.getLayersNames(), parentDestination);
                if (destinationDirectory != null) {
                    return Paths.get(destinationDirectory.toString().concat("/").concat(destinationFileName));
                } else return null;
            }
        }
        if ("fileNameGlob".equalsIgnoreCase(sourceType)) {
            // destination must be an existing directory
            return getExistingDirectory(sourceMeta.getLayersNames(), sourceMeta.getToPath());
        }
        return null;
    }
}
