package io.codeontap.freemarkerwrapper;

import com.sun.deploy.util.OrderedHashSet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.json.*;
import javax.json.stream.JsonParsingException;
import java.io.*;
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
public class CMDBProcessor {


    private Map<String, String> cmdbFileSystem;
    private Map<String, CMDB> cmdbMap = new LinkedHashMap<>();


    public String getCMDBs(List<String> lookupDirs, Map<String, String> CMDBs, final List<String> CMDBNamesList, String baseCMDB, boolean useCMDBPrefix) throws RunFreeMarkerException {
        Set<String> cmdbNames = new LinkedHashSet<>();

        if(!CMDBNamesList.isEmpty()){
            if(StringUtils.isNotEmpty(baseCMDB))
                cmdbNames.add(baseCMDB);
            cmdbNames.addAll(CMDBNamesList);
        }

        try {
            createCMDBFileSystem(lookupDirs,CMDBs, cmdbNames, baseCMDB, useCMDBPrefix);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (CMDB cmdb : cmdbMap.values()) {
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder
                    .add("Name", cmdb.getName())
                    .add("CMDBPath",StringUtils.defaultIfEmpty(cmdb.getPath(), ""))
                    .add("FileSystemPath",cmdb.getFileSystemPath())
                    .add("Base",cmdb.isBase())
                    .add("Active",cmdb.isActive())
                    .add("ParentCMDB",StringUtils.defaultIfEmpty(cmdb.getParentCMDB(), ""));
            jsonArrayBuilder.add(objectBuilder.build());

        }
        return jsonArrayBuilder.build().toString();
    }
    private void createCMDBFileSystem(List<String> lookupDirs, Map<String, String> CMDBs, Set<String> cmdbNames, String baseCMDB, boolean useCMDBPrefix) throws RunFreeMarkerException, IOException {
        /*
         * return an empty hash if no -g option applied
         */
        if(CMDBs.isEmpty() && lookupDirs.isEmpty()){
            return;
        }

        /*
         * When -g value is provided as a single path.
         * The second form identifies a directory whose subtree is scanned for .cmdb files,
         * with the containing directory being treated as a CMDB whose name is that of the containing directory.
         */
        if(!lookupDirs.isEmpty()) {
            for (String lookupDir:lookupDirs) {
                if (StringUtils.isNotEmpty(lookupDir)) {
                    if (!Files.isDirectory(Paths.get(lookupDir))) {
                        throw new RunFreeMarkerException(
                                String.format("Unable to read path \"%s\" for CMDB lookup", lookupDir));
                    }
                    FileFinder.Finder cmdbFilefinder = new FileFinder.Finder(".cmdb", false, false);
                    try {
                        Files.walkFileTree(Paths.get(lookupDir), cmdbFilefinder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    for (Path cmdbFile : cmdbFilefinder.done()) {
                        String cmdbName = cmdbFile.getParent().getFileName().toString();
                        String cmdbPath = cmdbFile.getParent().toString();
                        String CMDBPrefix = useCMDBPrefix ? cmdbFile.getParent().getParent().getFileName().toString().concat("_") : "";
                        CMDBs.put(CMDBPrefix.concat(cmdbName), cmdbPath);
                    }
                }
            }
        }
        cmdbNames.addAll(CMDBs.keySet());
        for (String cmdbName : cmdbNames) {
            String cmdbPath = CMDBs.get(cmdbName);
            if(!Files.isDirectory(Paths.get(cmdbPath))) {
                throw new RunFreeMarkerException(
                        String.format("Unable to read path \"%s\" for CMDB \"%s\"", cmdbPath, cmdbName));
            }else {
                CMDB cmdb = new CMDB(cmdbName,cmdbPath);
                cmdbMap.put(cmdb.getName(), cmdb);
            }
        }

        for (String cmdbName : CMDBs.keySet()) {
            CMDB cmdb = cmdbMap.get(cmdbName);
            Path CMDBPath = readJSONFileUsingDirectoryStream(CMDBs.get(cmdbName), ".cmdb");
            JsonObject jsonObject = null;
            /**
             * if cmdb file exist - read layers from it
             */
            if(CMDBPath!=null) {
                JsonReader jsonReader = Json.createReader(new FileReader(CMDBPath.toFile()));
                jsonObject = jsonReader.readObject();
            }

            JsonArray layers = Json.createArrayBuilder().build();
            if (jsonObject!= null) {
                cmdb.setContent(jsonObject);
                if (jsonObject.containsKey("Layers")) {
                    layers = jsonObject.getJsonArray("Layers").asJsonArray();
                }
            }

            if(StringUtils.isNotEmpty(baseCMDB) && StringUtils.equalsIgnoreCase(baseCMDB, cmdbName)){
                cmdb.setBase(true);
                cmdb.setActive(true);
            } else if (cmdbNames.isEmpty() || cmdbNames.contains(cmdbName)){
                cmdb.setActive(true);
            }
            /**
             * if there are no layers defined in a base cmdb, add all detected cmdb as layers with a default base path
             */
            /*if (layers.isEmpty()){
                JsonArrayBuilder layersBuilder = Json.createArrayBuilder();
                for(String name:CMDBs.keySet()){
                    if (name.equalsIgnoreCase(baseCMDB))
                        continue;
                    JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
                    objectBuilder.
                            add("Name", name).
                            add("BasePath", "/default/".concat(name));
                    layersBuilder.add(objectBuilder.build());
                }
                layers = layersBuilder.build();
            }*/
            for (int i = 0; i < layers.size(); i++) {
                JsonObject layer = layers.getJsonObject(i);
                String layerName = layer.getString("Name");
                String basePath = layer.getString("BasePath");
                String CMDBPrefix = useCMDBPrefix?CMDBPath.getParent().getParent().getFileName().toString().concat("_"):"";

                if (StringUtils.equalsIgnoreCase(cmdbName, layerName)){
                    cmdb.setPath(basePath);
                } else {
                    Set<String> children = cmdb.getChildren();
                    children.add(layerName);
                    cmdb.setChildren(children);
                    if(cmdbMap.containsKey(layerName)){
                        cmdbMap.get(layerName).setParentCMDB(cmdbName);
                    } else {
                        throw new RunFreeMarkerException(String.format("CMDB \"%s\" is missing from the detected CMDBs \"%s\"", layerName, CMDBs));
                    }
                }
                CMDB layerCMDB = cmdbMap.get(layerName);
                if (!StringUtils.equalsIgnoreCase(cmdbName, layerName)){
                    layerCMDB.setPath(basePath);
                    layerCMDB.setParentCMDB(cmdbName);
                    if (cmdbNames.isEmpty() || cmdbNames.contains(layerName)){
                        layerCMDB.setActive(true);
                    }
                    cmdb.getChildren().add(layerName);
                }
            }
        }
        for (CMDB cmdb:cmdbMap.values()){
            updatePath(cmdb);
        }


        /**
         * when -b option is not specified, the default base CMDB is tenant
         * doesn't expect CMDBPrefixes to be used
         * TODO: add a catch code
         */
        /*if (StringUtils.isEmpty(baseCMDB)) {
            baseCMDB = "tenant";
        }*/
        if (StringUtils.isNotEmpty(baseCMDB) && !CMDBs.containsKey(baseCMDB)) {
            throw new RunFreeMarkerException(String.format("Base CMDB \"%s\" is missing from the detected CMDBs \"%s\"", baseCMDB, CMDBs));
        }

        cmdbFileSystem = processCMDBFileSystem(baseCMDB, buildCMDBFileSystem(baseCMDB, CMDBs, useCMDBPrefix, cmdbNames, true));

    }

    private void updatePath(CMDB cmdb){
        if (StringUtils.isEmpty(cmdb.getPath())){
            if (cmdb.isBase()){
                cmdb.setPath("/");
            } else {
                cmdb.setPath("/default/".concat(cmdb.getName()));
            }
        } else if(!StringUtils.startsWith(cmdb.getPath(),"/")) {
            if(StringUtils.isNotEmpty(cmdb.getParentCMDB())){
                CMDB parent = cmdbMap.get(cmdb.getParentCMDB());
                String path = null;
                if(StringUtils.isEmpty(parent.getPath()) || !StringUtils.startsWith(parent.getPath(),"/")){
                    updatePath(parent);
                }
                path = parent.getPath().concat("/").concat(cmdb.getPath());
                cmdb.setPath(forceUnixStyle(path));
            }
        }
    }

    public Set<JsonObject> getFileTree(List<String> lookupDirs, Map<String, String> CMDBs, final List<String> CMDBNamesList, String baseCMDB, String startingPath, List<String> regexLsit,
                                               boolean ignoreDotDirectories, boolean ignoreDotFiles, boolean includeCMDBInformation, boolean useCMDBPrefix) throws RunFreeMarkerException {

        Set<JsonObject> output = new LinkedHashSet<>();
        Set<String> cmdbNames = new LinkedHashSet<>();

        if(!CMDBNamesList.isEmpty()){
            if(StringUtils.isNotEmpty(baseCMDB) && !CMDBNamesList.contains(baseCMDB))
            cmdbNames.add(baseCMDB);
            cmdbNames.addAll(CMDBNamesList);
        }

        try {
            createCMDBFileSystem(lookupDirs, CMDBs, cmdbNames, baseCMDB, useCMDBPrefix);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(cmdbFileSystem == null){
            return output;
        }

        Map<String, Path> files = new LinkedHashMap<>();
        Map<String, String> cmdbFilesMapping = new LinkedHashMap<>();
        Map<String, String> cmdbPhysicalFilesMapping = new LinkedHashMap<>();

        List<String> refinedRegexList = refineRegexList(startingPath, regexLsit);

        for (String cmdbName : cmdbNames) {
            CMDB cmdb = cmdbMap.get(cmdbName);
            if(!cmdb.isActive()){
                    continue;
            }
            /*if (!CMDBs.containsKey(CMDBName)) {
                throw new RunFreeMarkerException(String.format("CMDB Name \"%s\" was not found in the detected CMDBs \"%s\"", CMDBName, CMDBs));
            }*/
            // get physical starting dir for the current CMDB
            Path startingDir = Paths.get(cmdb.getFileSystemPath());
            FileFinder.Finder finder = new FileFinder.Finder("*", ignoreDotDirectories, ignoreDotFiles);
            try {
                Files.walkFileTree(startingDir, finder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Path file : finder.done()) {
                String path = file.toString();
                /*if(Files.isDirectory(file)){
                    System.out.println(path);
                }*/
                String cmdbPath = forceUnixStyle(StringUtils.replaceOnce(path, startingDir.toString(), cmdb.getPath()));
                for (String regex:refinedRegexList){
                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(cmdbPath);
                    if(m.matches()){
                        cmdbFilesMapping.put(cmdbPath, path);
                        cmdbPhysicalFilesMapping.put(path, cmdb.getName());
                    }
                }
            }
        }

        for (String file : cmdbFilesMapping.keySet()) {
            files.put(forceUnixStyle(file), Paths.get(cmdbFilesMapping.get(file)));
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
                        } else {
                            try (FileInputStream inputStream = new FileInputStream(file.toString())) {
                                JsonReader reader = Json.createReader(inputStream);
                                JsonObject jsonObject = reader.readObject();
                                reader.close();
                                jsonObjectBuilder.add("ContentsAsJSON", jsonObject);
                            } catch (JsonParsingException e) {
                                //do nothing
                            }
                        }
                    }
                } catch (UncheckedIOException e) {
                    //
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (includeCMDBInformation) {
                String cmdbName = cmdbPhysicalFilesMapping.get(file.toString());
                String cmdbBasePath = cmdbMap.get(cmdbName).getPath();
                jsonObjectBuilder.add("CMDB",
                        Json.createObjectBuilder()
                                .add("Name", cmdbName)
                                .add("BasePath", cmdbBasePath)
                                .add("File", file.toString())
                                .add("ContentsAsJSON", cmdbMap.get(cmdbName).getContent()).build());
            }
            output.add(jsonObjectBuilder.build());
        }
        return output;
    }

    private Set<String> listFilesUsingDirectoryStream(String dir) throws IOException {
        Set<String> fileList = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    fileList.add(path.getFileName()
                            .toString());
                }
            }
        }
        return fileList;
    }

    private Path readJSONFileUsingDirectoryStream(String dir, String fileName) throws IOException {
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


    /**
     * Builds a CMDBFileSystem
     * TODO: add support of relative/absolute path
     * at the moment all paths are absolute
     * @param baseCMDB the name of the base CMDB
     * @param CMDBs
     * @param useCMDBPrefix
     * @return
     */
    private Map<String, String> buildCMDBFileSystem(final String baseCMDB, final Map<String, String> CMDBs, boolean useCMDBPrefix, final Set<String> CMDBNames, boolean baseProcessing){
        Map<String, String> cmdbFileSystem = new TreeMap<>();
        try {
            Path CMDBPath = null;
            if(StringUtils.isNotEmpty(baseCMDB)){
                CMDBPath = readJSONFileUsingDirectoryStream(CMDBs.get(baseCMDB), ".cmdb");
            }
            JsonObject jsonObject = null;
            /**
             * if cmdb file exist - read layers from it
             */
            if(CMDBPath!=null) {
                JsonReader jsonReader = Json.createReader(new FileReader(CMDBPath.toFile()));
                jsonObject = jsonReader.readObject();
            }

            JsonArray layers = Json.createArrayBuilder().build();
            if (jsonObject!= null && jsonObject.containsKey("Layers")) {
                layers = jsonObject.getJsonArray("Layers").asJsonArray();
            }

            /**
             * if there are no layers defined in a base cmdb, add all detected cmdb as layers with a default base path
             */
            if (layers.isEmpty() && baseProcessing){
                JsonArrayBuilder layersBuilder = Json.createArrayBuilder();
                for(String name:CMDBs.keySet()){
                    if (name.equalsIgnoreCase(baseCMDB))
                        continue;
                    if(!CMDBNames.isEmpty() && !CMDBNames.contains(name))
                        continue;
                    JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
                    objectBuilder.
                            add("Name", name).
                            add("BasePath", "/default/".concat(name));
                    layersBuilder.add(objectBuilder.build());
                }
                layers = layersBuilder.build();
            }

            for (int i = 0; i < layers.size(); i++) {
                JsonObject layer = layers.getJsonObject(i);
                String layerName = layer.getString("Name");
                //if -c option was applied, check if a CMDB layer should be included into the CMDB file system
                if(!CMDBNames.isEmpty() && !CMDBNames.contains(layerName))
                    continue;
                String basePath = layer.getString("BasePath");
                String CMDBPrefix = useCMDBPrefix?CMDBPath.getParent().getParent().getFileName().toString().concat("_"):"";
                cmdbFileSystem.put(CMDBPrefix.concat(layerName), basePath);
                if(!baseCMDB.equalsIgnoreCase(layerName)) {
                    cmdbFileSystem.putAll(buildCMDBFileSystem(layerName, CMDBs, useCMDBPrefix, CMDBNames, false));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cmdbFileSystem;
    }

    private Map<String, String> processCMDBFileSystem(String baseCMDBName, final Map<String, String> cmdbFileSystem){
        Map<String, String> result = new TreeMap<>();
        String root = cmdbFileSystem.containsKey(baseCMDBName)?cmdbFileSystem.get(baseCMDBName):"/default/";
        for (String CMDBName:cmdbFileSystem.keySet()){
            String basePath = cmdbFileSystem.get(CMDBName);
            if(!basePath.startsWith("/")){
                basePath = root.concat(basePath);
            }
            result.put(CMDBName, basePath);
        }
        if(StringUtils.isNotEmpty(baseCMDBName)) {
            result.put(baseCMDBName, root);
        }
        return result;
    }

    private String forceUnixStyle(final String path){
        String result = StringUtils.replaceEachRepeatedly(path, new String[]{"\\", "//"}, new String[]{"/", "/"});
        if(result.endsWith("/"))
            result = StringUtils.substringBeforeLast(result,"/");
        return result;
    }

    private List<String> refineRegexList(final String startingDir, final List<String> regexList){
        List<String> result = new ArrayList<>();

        for (String regex:regexList){
            if(regex.startsWith("^")){
            } else {
                regex = getStartingDir(startingDir).concat(".*".concat(regex));
            }
            if(regex.endsWith("$")){
            } else {
                regex = regex.concat(".*");
            }
            result.add(regex);
        }
        return result;
    }

    private String getStartingDir(final String startingDir){
        if(StringUtils.endsWith(startingDir, "/")){
            return startingDir;
        } else {
            return startingDir.concat("/");
        }
    }
}
