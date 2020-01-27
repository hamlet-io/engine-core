package io.codeontap.freemarkerwrapper.files.processors;

import io.codeontap.freemarkerwrapper.files.FileFinder;
import io.codeontap.freemarkerwrapper.files.layers.Layer;
import io.codeontap.freemarkerwrapper.RunFreeMarkerException;
import io.codeontap.freemarkerwrapper.files.meta.LayerMeta;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.json.*;
import javax.json.stream.JsonParsingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: add staringPath processing
 */
public abstract class LayerProcessor {

    protected Map<String, String> fileSystem;
    protected Map<String, Layer> layerMap = new LinkedHashMap<>();
    private Map<String, Set<Path>> filesPerLayer = new LinkedHashMap<>();

    public abstract void createLayerFileSystem(LayerMeta meta) throws RunFreeMarkerException;
    public abstract JsonObjectBuilder buildInformation(JsonObjectBuilder jsonObjectBuilder, Layer layer, Path file);
    public abstract JsonArrayBuilder buildLayers(LayerMeta meta);

    public JsonArray getLayers(LayerMeta meta) throws RunFreeMarkerException {
        createLayerFileSystem(meta);
        return buildLayers(meta).build();
    }
    public Set<JsonObject> getLayerTree(LayerMeta meta) throws RunFreeMarkerException{
        return getLayerTree(meta, true);
    }
    public Set<JsonObject> getLayerTree(LayerMeta meta, boolean firstResultOnly) throws RunFreeMarkerException{
        Set<JsonObject> output = new LinkedHashSet<>();

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
            if(layer.getPath().startsWith(meta.getStartingPath())){
                skipLayer = false;
            } else if(meta.getStartingPath().startsWith(layer.getPath())){
                /**
                 * Don't skip a layer if a starting path starts with its path and the resolved path exists
                 * layer path - /products/api
                 * starting path - /products/api/config
                 */
                String path = StringUtils.replaceOnce(meta.getStartingPath(),layer.getPath(), layer.getFileSystemPath());
                if (Files.isDirectory(Paths.get(path))){
                   skipLayer = false;
                }
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
                        layerFilesMapping.put(layerPath, path);
                        layerPhysicalFilesMapping.put(path, layer.getName());
                        matchFound = true;
                    }
                }
                if(firstResultOnly && matchFound)
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
            jsonObjectBuilder.add("File", key);
            jsonObjectBuilder.add("Path", forceUnixStyle(path));
            jsonObjectBuilder.add("Filename", file.getFileName().toString());
            jsonObjectBuilder.add("Extension", StringUtils.substringAfterLast(file.getFileName().toString(), "."));
            if(!Files.isDirectory(file)) {
                try (FileInputStream inputStream = new FileInputStream(file.toString())) {
                    jsonObjectBuilder.add("Contents", IOUtils.toString(inputStream));
                } catch (IOException e) {
                    e.printStackTrace();
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
                            jsonObjectBuilder.add("Include", String.format("#include \"%s\"", forceUnixStyle(path)));
                        } else if(file.toString().toLowerCase().endsWith(".json")){
                            JsonStructure result = null;
                            try (FileInputStream inputStream = new FileInputStream(file.toString())) {
                                JsonReader reader = Json.createReader(inputStream);
                                JsonStructure jsonStructure = reader.read();
                                reader.close();
                                jsonObjectBuilder.add("ContentsAsJSON", cleanUpJson(jsonStructure));
                            } catch (JsonParsingException e) {
                                //do nothing
                                System.err.println(String.format("Unable to parse json file %s. Error details: %s", file, e.getMessage()));
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
        if(result.endsWith("/"))
            result = StringUtils.substringBeforeLast(result,"/");
        return result;
    }

    protected List<String> refineRegexList(final String startingDir, final List<String> regexList,
                                           boolean addStartingWildcard, boolean addEndingWildcard){
        List<String> result = new ArrayList<>();

        for (String regex:regexList){
            if(!regex.startsWith("^") && addStartingWildcard){
                regex = getStartingDir(startingDir).concat(".*".concat(regex));
            }
            if(!regex.endsWith("$") && addEndingWildcard){
                regex = regex.concat(".*");
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

    private Set<Path> getFilesPerLayer(LayerMeta meta, Layer layer){
        Set<Path> files = filesPerLayer.get(layer.getName());
        if(files == null) {
            Path startingDir = Paths.get(layer.getFileSystemPath());
            FileFinder.Finder finder = new FileFinder.Finder("*", meta.isIgnoreDotDirectories(), meta.isIgnoreDotFiles());
            try {
                Files.walkFileTree(startingDir, finder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            files = finder.done();
            filesPerLayer.put(layer.getName(), files);

        }
        return files;
    }
}
