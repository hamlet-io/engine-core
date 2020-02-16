package io.codeontap.freemarkerwrapper.files.processors.cmdb;

import io.codeontap.freemarkerwrapper.files.FileFinder;
import io.codeontap.freemarkerwrapper.files.layers.Layer;
import io.codeontap.freemarkerwrapper.files.layers.cmdb.CMDBLayer;
import io.codeontap.freemarkerwrapper.files.meta.cmdb.CMDBMeta;
import io.codeontap.freemarkerwrapper.RunFreeMarkerException;
import io.codeontap.freemarkerwrapper.files.meta.LayerMeta;
import io.codeontap.freemarkerwrapper.files.processors.LayerProcessor;
import org.apache.commons.lang3.StringUtils;

import javax.json.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CMDBProcessor extends LayerProcessor {


    public void createLayerFileSystem(LayerMeta meta) throws RunFreeMarkerException {
        CMDBMeta cmdbMeta = (CMDBMeta)meta;
        Set<String> cmdbNames = new LinkedHashSet<>();

        if(!cmdbMeta.getCMDBNamesList().isEmpty()){
            if(StringUtils.isNotEmpty(cmdbMeta.getBaseCMDB()) && !cmdbMeta.getCMDBNamesList().contains(cmdbMeta.getBaseCMDB()))
                cmdbNames.add(cmdbMeta.getBaseCMDB());
            for (String name:cmdbMeta.getCMDBNamesList()){
                cmdbNames.add(name);
            }

        }
        cmdbMeta.setLayersNames(cmdbNames);

            /*
         * return an empty hash if no -g option applied
         */
        if(cmdbMeta.getCMDBs().isEmpty() && cmdbMeta.getLookupDirs().isEmpty()){
            return;
        }

        /*
         * When -g value is provided as a single path.
         * The second form identifies a directory whose subtree is scanned for .cmdb files,
         * with the containing directory being treated as a CMDB whose name is that of the containing directory.
         */
        if(!cmdbMeta.getLookupDirs().isEmpty()) {
            for (String lookupDir:cmdbMeta.getLookupDirs()) {
                if (StringUtils.isNotEmpty(lookupDir)) {
                    if (!Files.isDirectory(Paths.get(lookupDir))) {
                        throw new RunFreeMarkerException(
                                String.format("Unable to read path \"%s\" for CMDB lookup", lookupDir));
                    }
                    FileFinder.Finder cmdbFilefinder = new FileFinder.Finder(".cmdb", true, false);
                    try {
                        Files.walkFileTree(Paths.get(lookupDir), cmdbFilefinder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    for (Path cmdbFile : cmdbFilefinder.done()) {
                        String cmdbName = cmdbFile.getParent().getFileName().toString();
                        String cmdbPath = cmdbFile.getParent().toString();
                        String CMDBPrefix = cmdbMeta.isUseCMDBPrefix() ? cmdbFile.getParent().getParent().getFileName().toString().concat("_") : "";
                        cmdbMeta.getCMDBs().put(CMDBPrefix.concat(cmdbName), cmdbPath);
                    }
                }
            }
        }

        for (String cmdbName : cmdbMeta.getCMDBs().keySet()) {
            String cmdbPath = cmdbMeta.getCMDBs().get(cmdbName);
            if(!Files.isDirectory(Paths.get(cmdbPath))) {
                throw new RunFreeMarkerException(
                        String.format("Unable to read path \"%s\" for CMDB \"%s\"", cmdbPath, cmdbName));
            }else {
                CMDBLayer cmdbLayer = new CMDBLayer(cmdbName, cmdbPath);
                layerMap.put(cmdbLayer.getName(), cmdbLayer);
            }
        }

        for (String cmdbName : cmdbMeta.getCMDBs().keySet()) {
            CMDBLayer cmdbLayer = (CMDBLayer)layerMap.get(cmdbName);
            Path CMDBPath = null;
            try {
                CMDBPath = readJSONFileUsingDirectoryStream(cmdbMeta.getCMDBs().get(cmdbName), ".cmdb");
            } catch (IOException e) {
                throw new RunFreeMarkerException(e.getMessage());
            }
            JsonObject jsonObject = null;
            /**
             * if cmdb file exist - read layers from it
             */
            if(CMDBPath!=null) {
                JsonReader jsonReader = null;
                try {
                    jsonReader = Json.createReader(new FileReader(CMDBPath.toFile()));
                } catch (FileNotFoundException e) {
                    throw new RunFreeMarkerException(e.getMessage());
                }
                jsonObject = jsonReader.readObject();
            }

            JsonArray layers = Json.createArrayBuilder().build();
            if (jsonObject!= null) {
                cmdbLayer.setContent(jsonObject);
                if (jsonObject.containsKey("Layers")) {
                    layers = jsonObject.getJsonArray("Layers").asJsonArray();
                }
            }

            if(StringUtils.isNotEmpty(cmdbMeta.getBaseCMDB()) && StringUtils.equalsIgnoreCase(cmdbMeta.getBaseCMDB(), cmdbName)){
                cmdbLayer.setBase(true);
                cmdbLayer.setActive(true);
            } else if (cmdbMeta.getLayersNames().isEmpty() || cmdbMeta.getLayersNames().contains(cmdbName)){
                cmdbLayer.setActive(true);
            }

            for (int i = 0; i < layers.size(); i++) {
                JsonObject layerObject = layers.getJsonObject(i);
                String layerName = layerObject.getString("Name");
                String basePath = layerObject.getString("BasePath");
                String CMDBPrefix = cmdbMeta.isUseCMDBPrefix()?CMDBPath.getParent().getParent().getFileName().toString().concat("_"):"";

                if (StringUtils.equalsIgnoreCase(cmdbName, layerName)){
                    cmdbLayer.setPath(basePath);
                } else {
                    Set<String> children = cmdbLayer.getChildren();
                    children.add(layerName);
                    cmdbLayer.setChildren(children);
                    if(layerMap.containsKey(layerName)){
                        ((CMDBLayer)layerMap.get(layerName)).setParentCMDB(cmdbName);
                    } else {
                        throw new RunFreeMarkerException(String.format("CMDB \"%s\" is missing from the detected CMDBs \"%s\"", layerName, cmdbMeta.getCMDBs()));
                    }
                }
                CMDBLayer layer = (CMDBLayer)layerMap.get(layerName);
                if (!StringUtils.equalsIgnoreCase(cmdbName, layerName)){
                    layer.setPath(basePath);
                    layer.setParentCMDB(cmdbName);
                    if (cmdbMeta.getLayersNames().isEmpty() || cmdbMeta.getLayersNames().contains(layerName)){
                        layer.setActive(true);
                    }
                    layer.getChildren().add(layerName);
                }
            }
        }
        for (Layer layer:layerMap.values()){
            updatePath((CMDBLayer)layer);
        }


        /**
         * when -b option is not specified, the default base CMDB is tenant
         * doesn't expect CMDBPrefixes to be used
         * TODO: add a catch code
         */
        if (StringUtils.isNotEmpty(cmdbMeta.getBaseCMDB()) && !cmdbMeta.getCMDBs().containsKey(cmdbMeta.getBaseCMDB())) {
            throw new RunFreeMarkerException(String.format("Base CMDB \"%s\" is missing from the detected CMDBs \"%s\"",
                    cmdbMeta.getBaseCMDB(), cmdbMeta.getCMDBs()));
        }

        fileSystem = processCMDBFileSystem(cmdbMeta.getBaseCMDB(), buildCMDBFileSystem(
                cmdbMeta.getBaseCMDB(), cmdbMeta.getCMDBs(), cmdbMeta.isUseCMDBPrefix(), cmdbMeta.getLayersNames(), true));

        if(cmdbMeta.getLayersNames().isEmpty()){
            for (Layer layer:layerMap.values()){
                CMDBLayer cmdbLayer = (CMDBLayer)layer;
                if(cmdbLayer.isActive()){
                    cmdbMeta.getLayersNames().add(cmdbLayer.getName());
                }
            }
        }
    }

    @Override
    public JsonObjectBuilder buildInformation(JsonObjectBuilder jsonObjectBuilder, Layer layer, Path file) {
        jsonObjectBuilder.add("CMDB",
                Json.createObjectBuilder()
                        .add("Name", layer.getName())
                        .add("BasePath", layer.getPath())
                        .add("File", file.toString())
                        .add("ContentsAsJSON", ((CMDBLayer)layer).getContent()).build());
        return jsonObjectBuilder;
    }

    @Override
    public JsonArrayBuilder buildLayers(LayerMeta meta) {
        CMDBMeta cmdbMeta = (CMDBMeta) meta;
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (Layer layer : layerMap.values()) {
            CMDBLayer cmdbLayer = (CMDBLayer)layer;
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            if(cmdbMeta.isActiveOnly() && !cmdbLayer.isActive())
                continue;
            objectBuilder
                    .add("Name", cmdbLayer.getName())
                    .add("CMDBPath",StringUtils.defaultIfEmpty(cmdbLayer.getPath(), ""))
                    .add("FileSystemPath", cmdbLayer.getFileSystemPath())
                    .add("Base", cmdbLayer.isBase())
                    .add("Active", cmdbLayer.isActive())
                    .add("ParentCMDB",StringUtils.defaultIfEmpty(cmdbLayer.getParentCMDB(), ""))
                    .add("ContentsAsJSON", cmdbLayer.getContent());

            jsonArrayBuilder.add(objectBuilder.build());
        }
        return jsonArrayBuilder;
    }

    private void updatePath(CMDBLayer cmdbLayer) {
        if (StringUtils.isEmpty(cmdbLayer.getPath())){
            if (cmdbLayer.isBase()){
                cmdbLayer.setPath("/");
            } else {
                cmdbLayer.setPath("/default/".concat(cmdbLayer.getName()));
            }
        } else if(!StringUtils.startsWith(cmdbLayer.getPath(),"/")) {
            if(StringUtils.isNotEmpty(cmdbLayer.getParentCMDB())){
                CMDBLayer parent = (CMDBLayer) layerMap.get(cmdbLayer.getParentCMDB());
                String path = null;
                if(StringUtils.isEmpty(parent.getPath()) || !StringUtils.startsWith(parent.getPath(),"/")){
                    updatePath(parent);
                }
                path = parent.getPath().concat("/").concat(cmdbLayer.getPath());
                cmdbLayer.setPath(forceUnixStyle(path));
            }
        }
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

}
