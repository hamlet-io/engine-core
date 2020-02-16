package io.codeontap.freemarkerwrapper.files.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.codeontap.freemarkerwrapper.files.FileFinder;
import io.codeontap.freemarkerwrapper.files.layers.Layer;
import io.codeontap.freemarkerwrapper.RunFreeMarkerException;
import io.codeontap.freemarkerwrapper.files.meta.LayerMeta;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.json.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class LayerProcessor {

    protected Map<String, String> fileSystem;
    protected Map<String, Layer> layerMap = new LinkedHashMap<>();
    private Map<String, List<Path>> filesPerLayer = new LinkedHashMap<>();

    public abstract void createLayerFileSystem(LayerMeta meta) throws RunFreeMarkerException;
    public abstract JsonObjectBuilder buildInformation(JsonObjectBuilder jsonObjectBuilder, Layer layer, Path file);
    public abstract JsonArrayBuilder buildLayers(LayerMeta meta);

    public JsonArray getLayers(LayerMeta meta) throws RunFreeMarkerException {
        createLayerFileSystem(meta);
        return buildLayers(meta).build();
    }

    public Set<JsonObject> getLayerTree(LayerMeta meta) throws RunFreeMarkerException{
        Set<JsonObject> output = new LinkedHashSet<>();

        if(meta.getStartingPath()!=null && !meta.getStartingPath().startsWith("/")){
            meta.setStartingPath("/".concat(meta.getStartingPath()));
        }

        createLayerFileSystem(meta);

        if(fileSystem == null){
            return output;
        }

        Map<String, Path> files = new LinkedHashMap<>();
        Map<String, String> layerFilesMapping = new LinkedHashMap<>();
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
            if (getDirectoryOnFileSystem(meta.getStartingPath(), layer.getPath(), layer.getFileSystemPath())!=null){
                skipLayer = false;
            }
            if (skipLayer){
                layersToSkip.add(layerName);
            }
        }

        for (String regex:refinedRegexList){
            boolean matchFound = false;
            for (String layerName : meta.getLayersNames()) {
                Layer layer = layerMap.get(layerName);
                if (layersToSkip.contains(layerName)){
                    continue;
                }

                // get physical starting dir for the current layer
                for (Path file : getFilesPerLayer(meta, layer)) {
                    String path = file.toString();
                    String layerPath = forceUnixStyle(StringUtils.replaceOnce(path, Paths.get(layer.getFileSystemPath()).toString(), layer.getPath()));
                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(layerPath);
                    if(m.matches()){
                        if(!layerFilesMapping.containsKey(layerPath)) {
                            layerFilesMapping.put(layerPath, path);
                            layerPhysicalFilesMapping.put(path, layer.getName());
                            matchFound = true;
                            if(meta.isStopAfterFirstMatch() || meta.isIgnoreSubtreeAfterMatch())
                                break;
                        }
                    }
                }
                if(meta.isStopAfterFirstMatch() && matchFound)
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
            if(Files.isDirectory(file)) {
                jsonObjectBuilder.add("IsDirectory", Boolean.TRUE);
            } else {
                String contents=null;
                try (FileInputStream inputStream = new FileInputStream(file.toString())) {
                    contents = IOUtils.toString(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(contents!=null){
                    jsonObjectBuilder.add("Contents", contents);
                }
                try {
                    /**
                     * Check if a file is a freemarker template - starts with [#ftl]
                     * if not, attempt to parse it as a json file
                     */
                    Object[] array = Files.lines(file).limit(1).toArray();
                    if(array.length > 0) {
                        String firstLine = Files.lines(file).limit(1).toArray()[0].toString();
                        if (StringUtils.startsWith(firstLine, "[#ftl]")) {
                            jsonObjectBuilder.add("IsTemplate", Boolean.TRUE);
                        } else {
                            JsonReader reader = null;
                            switch (extension.toLowerCase()){
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
                            if (reader!=null){
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
                    if(fileName.equals(path.getFileName().toString())){
                        return path;
                    }
                }
            }
        } catch (NullPointerException e){
            System.err.println(String.format("Cannot find path %s", dir));
        }
        return null;
    }

    protected String forceUnixStyle(final String path){
        String result = StringUtils.replaceEachRepeatedly(path, new String[]{"\\", "//"}, new String[]{"/", "/"});
        if(!"/".equalsIgnoreCase(result) && result.endsWith("/"))
            result = StringUtils.substringBeforeLast(result,"/");
        return result;
    }

    protected List<String> refineRegexList(final String startingDir, final List<String> regexList,
                                           boolean addStartingWildcard, boolean addEndingWildcard){
        List<String> result = new ArrayList<>();

        for (String regex:regexList){
            if(!regex.startsWith("^")){
                if (addStartingWildcard){
                    regex = ".*".concat(regex);
                }
                regex = StringUtils.join("^",getStartingDir(startingDir),regex);
            }
            if(!regex.endsWith("$")){
                if (addEndingWildcard){
                    regex = regex.concat(".*");
                }
                regex = regex.concat("$");
            }
            result.add(regex);
        }
        return result;
    }

    protected String getStartingDir(final String startingDir){
        if(StringUtils.endsWith(startingDir, "/")){
            return startingDir;
        } else {
            return startingDir.concat("/");
        }
    }

    protected JsonValue cleanUpJson(JsonValue jsonValue){
        JsonObjectBuilder jsonObjectBuilder = null;
        JsonArrayBuilder jsonArrayBuilder = null;
        if (jsonValue instanceof JsonObject){
            JsonObject jsonObject = (JsonObject)jsonValue;
            jsonObjectBuilder = Json.createObjectBuilder(jsonObject);
            for(String s:jsonObject.keySet()){
                if(jsonObject.get(s) == JsonValue.NULL){
                    jsonObjectBuilder.remove(s);
                } else {
                    jsonObjectBuilder.add(s, cleanUpJson(jsonObject.get(s)));
                }
            }
            return jsonObjectBuilder.build();
        } else if(jsonValue instanceof JsonArray){
            JsonArray jsonArray = (JsonArray)jsonValue;
            jsonArrayBuilder = Json.createArrayBuilder();
            Iterator<JsonValue> iterator = jsonArray.iterator();
            while (iterator.hasNext()){
                JsonValue jsonValue1 = iterator.next();
                if(jsonValue1.getValueType() != JsonValue.ValueType.NULL){
                    jsonArrayBuilder.add(cleanUpJson(jsonValue1));
                }
            }
            return jsonArrayBuilder.build();
        } else {
            return jsonValue;
        }
    }

    private List<Path> getFilesPerLayer(LayerMeta meta, Layer layer){
        List<Path> files = filesPerLayer.get(layer.getName());
        if(files == null) {
            // search starting point optimisation, see https://github.com/codeontap/gen3-freemarker-wrapper/issues/22
            Path startingDir = getDirectoryOnFileSystem(meta.getStartingPath(), layer.getPath(), layer.getFileSystemPath());
            FileFinder.Finder finder = new FileFinder.Finder("*", meta.isIgnoreDotDirectories(), meta.isIgnoreDotFiles());
            String relativeLayerPath = StringUtils.substringAfter(forceUnixStyle(layer.getPath()), forceUnixStyle(meta.getStartingPath()));
            int relativeLayerPathDepth = StringUtils.split(relativeLayerPath,"/").length;
            Integer depth = Integer.MAX_VALUE;
            if(meta.getMaxDepth()!=null){
                depth = meta.getMaxDepth();
            }
            // adjust max depth based on layer path related to the starting path
            // for example if starting path is set to /products
            // and maxDepth=3 and layer path is /products/almv2
            // depth needs to be decreased by 1
            // as the layer one layer deeper on the layer filesystem than on physical one
            depth -=relativeLayerPathDepth;
            if(depth <= 0)
                return files;
            try {
                Files.walkFileTree(startingDir, EnumSet.of(FileVisitOption.FOLLOW_LINKS), depth, finder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            files = finder.done();
            if(meta.getMinDepth()!=null){
                List<Path> list = new ArrayList<>();
                list.addAll(files);
                for (Path path:list){
                    String layerFilePath = StringUtils.replaceOnce(forceUnixStyle(path.toString()), forceUnixStyle(Paths.get(layer.getFileSystemPath()).toString()), forceUnixStyle(layer.getPath()));
                    String relativeLayerFilePath = StringUtils.substringAfter(layerFilePath, forceUnixStyle(meta.getStartingPath()));
                    int relativeLayerFilePathDepth = StringUtils.split(relativeLayerFilePath,"/").length;
                    // exclude files that do not meet min depth requirements
                    // relative layer path is checked here as well as for max depth
                    if(relativeLayerFilePathDepth < meta.getMinDepth()) {
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
     * @param startingPath
     * @param layerPath
     * @param fileSystemPath
     * @return
     */
    private Path getDirectoryOnFileSystem(String startingPath, String layerPath, String fileSystemPath){
        startingPath = forceUnixStyle(startingPath);
        fileSystemPath = forceUnixStyle(fileSystemPath);
        if (StringUtils.equalsIgnoreCase("/", layerPath)){
            fileSystemPath = fileSystemPath.concat("/");
        } else {
            layerPath = forceUnixStyle(layerPath);
        }
        String path;
        if(layerPath.startsWith(startingPath)){
            // if layer path includes the starting path,
            // the physical filesystem path is the starting point
            // physical filesystem path - /var/opt/codeontap/api
            // layer path - /products/api
            // starting path - /products
            // starting point - /var/opt/codeontap/api
            path = fileSystemPath;
        }
        else {
            // if layer path does not include the starting path,
            // relative layer path is added to the physical filesystem path to adjust the starting point
            // physical filesystem path - /var/opt/codeontap/api
            // layer path - /products/api
            // starting path - /products/api/config
            // starting point - /var/opt/codeontap/api/config
            if(startingPath.startsWith(layerPath)) {
                path = StringUtils.replaceOnce(startingPath, layerPath, fileSystemPath);
            } else {
                path = null;
            }
        }
        if(path!= null && Files.isDirectory(Paths.get(path))){
            return Paths.get(path);
        } else {
            return null;
        }
    }
}
