package io.codeontap.freemarkerwrapper;

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
    public Map<String, JsonObject> getFileTree(List<String> lookupDirs, Map<String, String> CMDBs, final List<String> CMDBNamesList, String baseCMDB, String startingPath, List<String> regexLsit,
                                               boolean ignoreDotDirectories, boolean ignoreDotFiles, boolean includeCMDBInformation, boolean useCMDBPrefix) throws RunFreeMarkerException {
        Map<String, JsonObject> output = new HashMap<String, JsonObject>();
        Map<String, Path> files = new TreeMap<>();
        Set<String> CMDBNames = new TreeSet<>();
        CMDBNames.addAll(CMDBNamesList);

        /*
         * return an empty hash if no -g option applied
         */
        if(CMDBs.isEmpty() && lookupDirs.isEmpty()){
            return output;
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
        } else {
            for (String CMDBName : CMDBs.keySet()) {
                String CMDBPath = CMDBs.get(CMDBName);
                if(!Files.isDirectory(Paths.get(CMDBPath))) {
                    throw new RunFreeMarkerException(
                            String.format("Unable to read path \"%s\" for CMDB \"%s\"", CMDBPath, CMDBName));
                }
            }
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

        Map<String, String> cmdbFileSystem = processCMDBFileSystem(baseCMDB, buildCMDBFileSystem(baseCMDB, CMDBs, useCMDBPrefix, CMDBNames, true));

        Map<String, String> cmdbFilesMapping = new TreeMap<>();
        Map<String, String> cmdbPhysicalFilesMapping = new TreeMap<>();

        List<String> refinedRegexList = refineRegexList(startingPath, regexLsit);

        for (String CMDBName : cmdbFileSystem.keySet()) {
            if (!CMDBs.containsKey(CMDBName)) {
                throw new RunFreeMarkerException(String.format("CMDB Name \"%s\" was not found in the detected CMDBs \"%s\"", CMDBName, CMDBs));
            }
            // get physical starting dir for the current CMDB
            Path startingDir = Paths.get(CMDBs.get(CMDBName));
            FileFinder.Finder finder = new FileFinder.Finder("*.*", ignoreDotDirectories, ignoreDotFiles);
            try {
                Files.walkFileTree(startingDir, finder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Path file : finder.done()) {
                String path = file.toString();
                String cmdbBasePath = cmdbFileSystem.get(CMDBName);
                String cmdbPath = forceUnixStyle(StringUtils.replaceOnce(path, startingDir.toString(), cmdbBasePath));
                for (String regex:refinedRegexList){
                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(cmdbPath);
                    if(m.matches()){
                        cmdbFilesMapping.put(cmdbPath, path);
                        cmdbPhysicalFilesMapping.put(path, CMDBName);
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
            jsonObjectBuilder.add("Path", forceUnixStyle(path));
            jsonObjectBuilder.add("Filename", file.getFileName().toString());
            jsonObjectBuilder.add("Extension", StringUtils.substringAfterLast(file.getFileName().toString(), "."));
            try (FileInputStream inputStream = new FileInputStream(file.toString())) {
                jsonObjectBuilder.add("Contents", IOUtils.toString(inputStream));
            } catch (IOException e) {
                e.printStackTrace();
            }
            /**
             * Check if a file is a freemarker template - starts with [#ftl]
             * if not, attempt to parse it as a json file
             */
            try {
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
                            System.out.println(String.format("The content of file %s is not a valid JSON and won't be parsed.", forceUnixStyle(key)));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (includeCMDBInformation) {
                String cmdbName = cmdbPhysicalFilesMapping.get(file.toString());
                String cmdbBasePath = cmdbFileSystem.get(cmdbName);
                jsonObjectBuilder.add("CMDB",
                        Json.createObjectBuilder().add("Name", cmdbName).add("BasePath", cmdbBasePath).add("File", file.toString()).build());
            }
            output.put(forceUnixStyle(key), jsonObjectBuilder.build());
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
            System.err.println(String.format("Cannot fin path %s", dir));
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
