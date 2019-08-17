package io.codeontap.freemarkerwrapper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.json.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CMDBProcessor {
    public Map<String, JsonObject> getFileTree (String lookupDir, Map<String,String> CMDBs, List<String> CMDBNames, String baseCMDB, String startingPath, List<String> regex,
                            boolean ignoreDotDirectories, boolean ignoreDotFiles, boolean includeCMDBInformation, boolean useCMDBPrefix) throws RunFreeMarkerException{
        Map<String, JsonObject> output = new HashMap<String, JsonObject>();
        Map<String, Path> files = new TreeMap<>();
        String CMDBPrefix = Paths.get(lookupDir).getFileName().toString().concat("_");
        if(!useCMDBPrefix){
            if(StringUtils.isNotEmpty(baseCMDB) && baseCMDB.startsWith(CMDBPrefix)){
                useCMDBPrefix = Boolean.TRUE;
            }
            else for (String CMDBName : CMDBNames){
                if (CMDBName.startsWith(CMDBPrefix)){
                    useCMDBPrefix = Boolean.TRUE;
                }
            }
        }
        if(!useCMDBPrefix){
            CMDBPrefix = "";
        }

        if(StringUtils.isNotEmpty(lookupDir)){
            FileFinder.Finder cmdbFilefinder = new FileFinder.Finder(".cmdb", false, false);
            try {
                Files.walkFileTree(Paths.get(lookupDir), cmdbFilefinder );
            } catch (IOException e){
                e.printStackTrace();
            }
            for (Path cmdbFile:cmdbFilefinder.done() ){
                String cmdbName = cmdbFile.getParent().getFileName().toString();
                String cmdbPath = cmdbFile.getParent().toString();
                CMDBs.put(CMDBPrefix.concat(cmdbName), cmdbPath);
            }
        }
        if(CMDBNames.isEmpty()) {
            //TODO: use Set instead of list
            CMDBNames = new ArrayList<>();
            CMDBNames.addAll(CMDBs.keySet());
        }

        if(StringUtils.isEmpty(baseCMDB)){
            baseCMDB = CMDBPrefix.concat("tenant");
        }
        if (!CMDBs.containsKey(baseCMDB)){
            throw new RunFreeMarkerException(String.format("Base CMDB \"%s\" is missing from the detected CMDBs \"%s\"", baseCMDB, CMDBs));
        }

        Map<String, String> cmdbFileSystem = new TreeMap<>();

        cmdbFileSystem.put(baseCMDB, "/");
        /*for (String CMDB: CMDBs.keySet()) {*/
            JsonReader jsonReader = null;
            try {
                String path = CMDBs.get(baseCMDB);
                if(path.endsWith("/") || path.endsWith("\\")){
                    path = path.concat(".cmdb");
                } else {
                    path = path.concat("/.cmdb");
                }
                jsonReader = Json.createReader(new FileReader(path));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            JsonObject jsonObject = jsonReader.readObject();
            if(jsonObject.containsKey("Layers")) {
                JsonArray layers = jsonObject.getJsonArray("Layers").asJsonArray();
                for (int i = 0; i < layers.size(); i++) {
                    JsonObject layer = layers.getJsonObject(i);
                    String layerName = layer.getString("Name");
                    String basePath = layer.getString("BasePath");
/*                    if (layerName.equalsIgnoreCase(baseCMDB)) {
                        if(!basePath.startsWith("/"))
                            //TODO: is the root of CMDB filesystem always `/` ?
                            // If it is not, how to find out what it is?
                            // base path of the base CMDB?
                            cmdbFileSystem.put(baseCMDB, "/".concat(basePath));
                    }*/
                    if(!basePath.startsWith("/")) basePath = "/".concat(basePath);
                    cmdbFileSystem.put(CMDBPrefix.concat(layerName), basePath);
                }
            }
        /*}*/

        Map<String, String> cmdbBasePathMapping = new TreeMap<>();
        for (String CMDB : CMDBNames){
            if(cmdbFileSystem.containsKey(CMDB)){
                cmdbBasePathMapping.put(cmdbFileSystem.get(CMDB), CMDBs.get(CMDB));
            } else {
                throw new RunFreeMarkerException(String.format("Specified CMDB name \"%s\" is missing in the CMDB filesystem \"%s\".", CMDB, cmdbFileSystem));
            }
        }

        Map<String, String> cmdbFilesMapping = new TreeMap<>();
        Map<String, String> cmdbPhysicalFilesMapping = new TreeMap<>();

        for (String CMDBName: CMDBNames) {
            Path startingDir = Paths.get(CMDBs.get(CMDBName));
            FileFinder.Finder finder = new FileFinder.Finder("*.*", ignoreDotDirectories, ignoreDotFiles);
            try{
                Files.walkFileTree(startingDir, finder);
            } catch (IOException e){
                e.printStackTrace();
            }
            for(Path file:finder.done()){
                String path = file.toString();
                String cmdbBasePath = cmdbFileSystem.get(CMDBName);
                String cmdbPath = path.replaceFirst(startingDir.toString(), cmdbBasePath).replaceFirst("//","/");
                cmdbFilesMapping.put(cmdbPath, path);
                cmdbPhysicalFilesMapping.put(path, CMDBName);
            }
        }

/*
        for (String CMDB: CMDBNames) {
*/
            if(!startingPath.startsWith("/")) startingPath = "/".concat(startingPath);


            for (String pattern : regex) {
                /*FileFinder.Finder finder = new FileFinder.Finder(pattern, ignoreDotDirectories, ignoreDotFiles);
                try{
                    Files.walkFileTree(startingDir, finder);
                } catch (IOException e){
                    e.printStackTrace();
                }
                files.addAll(finder.done());*/
                Pattern p = Pattern.compile(new StringBuilder().append("^")
                        .append(startingPath).append(".*").append(pattern).append("$").toString());
                for(String file:cmdbFilesMapping.keySet()){
                    Matcher m = p.matcher(file);
                    if(m.matches()){
                        files.put(file, Paths.get(cmdbFilesMapping.get(file)));
                    }
                }

            }
            for (String key : files.keySet()) {
                Path file = files.get(key);
                JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
                jsonObjectBuilder.add("Path", key);
                jsonObjectBuilder.add("Filename", file.getFileName().toString());
                jsonObjectBuilder.add("Extension", StringUtils.substringAfterLast(file.getFileName().toString(), "."));
                try (FileInputStream inputStream = new FileInputStream(file.toString())) {
                    jsonObjectBuilder.add("Contents", IOUtils.toString(inputStream));
                } catch (IOException e){
                    e.printStackTrace();
                }
                if(includeCMDBInformation) {
                    String cmdbName = cmdbPhysicalFilesMapping.get(file.toString());
                    String cmdbBasePath = cmdbFileSystem.get(cmdbName);
                    jsonObjectBuilder.add("CMDB",
                            Json.createObjectBuilder().add("Name", cmdbName).add("BasePath", cmdbBasePath).add("File", file.toString()).build());
                }
                output.put(file.toString(), jsonObjectBuilder.build());
            }
        /*}*/
        return output;
    }
}
