package io.hamlet.freemarkerwrapper.files.processors.layer.cmdb;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import freemarker.template.TemplateModelException;
import io.hamlet.freemarkerwrapper.ParameterValueException;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.FileFinder;
import io.hamlet.freemarkerwrapper.files.layers.Layer;
import io.hamlet.freemarkerwrapper.files.layers.cmdb.CMDBLayer;
import io.hamlet.freemarkerwrapper.files.meta.Meta;
import io.hamlet.freemarkerwrapper.files.meta.layer.LayerMeta;
import io.hamlet.freemarkerwrapper.files.meta.layer.cmdb.CMDBMeta;
import io.hamlet.freemarkerwrapper.files.processors.layer.LayerProcessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.json.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class CMDBProcessor extends LayerProcessor {

    public static String FILE_SYSTEM_CMDB = "cmdbFileSystem";
    public static String LAYER_MAP_CMDB = "cmdbLayerMap";

    public CMDBProcessor() {
        fileSystemShareVariableName = FILE_SYSTEM_CMDB;
        layerMapShareVariableName = LAYER_MAP_CMDB;
    }

    public void createLayerFileSystem(LayerMeta meta) throws TemplateModelException, IOException {
        CMDBMeta cmdbMeta = (CMDBMeta) meta;
        Set<String> cmdbNames = new LinkedHashSet<>();

        if (!cmdbMeta.getCMDBNamesList().isEmpty()) {
            if (StringUtils.isNotEmpty(cmdbMeta.getBaseCMDB()) && !cmdbMeta.getCMDBNamesList().contains(cmdbMeta.getBaseCMDB()))
                cmdbNames.add(cmdbMeta.getBaseCMDB());
            for (String name : cmdbMeta.getCMDBNamesList()) {
                cmdbNames.add(name);
            }

        }
        cmdbMeta.setLayersNames(cmdbNames);

        /*
         * return an empty hash if no -g option applied
         */
        if (cmdbMeta.getCMDBs().isEmpty() && cmdbMeta.getLookupDirs().isEmpty()) {
            return;
        }

        /*
         * When -g value is provided as a single path.
         * The second form identifies a directory whose subtree is scanned for .cmdb files,
         * with the containing directory being treated as a CMDB whose name is that of the containing directory.
         */
        if (!cmdbMeta.getLookupDirs().isEmpty()) {
            for (String lookupDir : cmdbMeta.getLookupDirs()) {
                if (StringUtils.isNotEmpty(lookupDir)) {
                    if (!Files.isDirectory(Paths.get(lookupDir))) {
                        throw new RunFreeMarkerException(
                                String.format("Unable to read path \"%s\" for CMDB lookup", lookupDir));
                    }
                    FileFinder.Finder cmdbFileFinder = new FileFinder.Finder(".cmdb", true, false);
                    Files.walkFileTree(Paths.get(lookupDir), cmdbFileFinder);
                    for (Path cmdbFile : cmdbFileFinder.done()) {
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
            if (!Files.isDirectory(Paths.get(cmdbPath))) {
                throw new RunFreeMarkerException(
                        String.format("Unable to read path \"%s\" for CMDB \"%s\"", cmdbPath, cmdbName));
            } else {
                CMDBLayer cmdbLayer = new CMDBLayer(cmdbName, cmdbPath);
                layerMap.put(cmdbLayer.getName(), cmdbLayer);
            }
        }

        for (String cmdbName : cmdbMeta.getCMDBs().keySet()) {
            CMDBLayer cmdbLayer = (CMDBLayer) layerMap.get(cmdbName);
            Path CMDBPath = readJSONFileUsingDirectoryStream(cmdbMeta.getCMDBs().get(cmdbName), ".cmdb");
            JsonObject jsonObject = null;
            /**
             * if cmdb file exist - read layers from it
             */
            if (CMDBPath != null) {
                JsonReader jsonReader = Json.createReader(new FileReader(CMDBPath.toFile()));
                jsonObject = jsonReader.readObject();
            }

            JsonArray layers = Json.createArrayBuilder().build();
            if (jsonObject != null) {
                cmdbLayer.setContent(jsonObject);
                if (jsonObject.containsKey("Layers")) {
                    layers = jsonObject.getJsonArray("Layers").asJsonArray();
                }
            }

            if (StringUtils.isNotEmpty(cmdbMeta.getBaseCMDB()) && StringUtils.equalsIgnoreCase(cmdbMeta.getBaseCMDB(), cmdbName)) {
                cmdbLayer.setBase(true);
                cmdbLayer.setActive(true);
            } else if (cmdbMeta.getLayersNames().isEmpty() || cmdbMeta.getLayersNames().contains(cmdbName)) {
                cmdbLayer.setActive(true);
            }

            for (int i = 0; i < layers.size(); i++) {
                JsonObject layerObject = layers.getJsonObject(i);
                String layerName = layerObject.getString("Name");
                String basePath = layerObject.getString("BasePath");
                String CMDBPrefix = cmdbMeta.isUseCMDBPrefix() ? CMDBPath.getParent().getParent().getFileName().toString().concat("_") : "";

                if (StringUtils.equalsIgnoreCase(cmdbName, layerName)) {
                    cmdbLayer.setPath(basePath);
                } else {
                    Set<String> children = cmdbLayer.getChildren();
                    children.add(layerName);
                    cmdbLayer.setChildren(children);
                    if (layerMap.containsKey(layerName)) {
                        ((CMDBLayer) layerMap.get(layerName)).setParentCMDB(cmdbName);
                    } else {
                        throw new RunFreeMarkerException(String.format("CMDB \"%s\" is missing from the detected CMDBs \"%s\"", layerName, cmdbMeta.getCMDBs()));
                    }
                }
                CMDBLayer layer = (CMDBLayer) layerMap.get(layerName);
                if (!StringUtils.equalsIgnoreCase(cmdbName, layerName)) {
                    layer.setPath(basePath);
                    layer.setParentCMDB(cmdbName);
                    if (cmdbMeta.getLayersNames().isEmpty() || cmdbMeta.getLayersNames().contains(layerName)) {
                        layer.setActive(true);
                    }
                    layer.getChildren().add(layerName);
                }
            }
        }
        for (Layer layer : layerMap.values()) {
            updatePath((CMDBLayer) layer);
        }


        /**
         * when -b option is not specified, the default base CMDB is tenant
         * doesn't expect CMDBPrefixes to be used
         */
        if (StringUtils.isNotEmpty(cmdbMeta.getBaseCMDB()) && !cmdbMeta.getCMDBs().containsKey(cmdbMeta.getBaseCMDB())) {
            throw new RunFreeMarkerException(String.format("Base CMDB \"%s\" is missing from the detected CMDBs \"%s\"",
                    cmdbMeta.getBaseCMDB(), cmdbMeta.getCMDBs()));
        }

        fileSystem = processCMDBFileSystem(cmdbMeta.getBaseCMDB(), buildCMDBFileSystem(
                cmdbMeta.getBaseCMDB(), cmdbMeta.getCMDBs(), cmdbMeta.isUseCMDBPrefix(), cmdbMeta.getLayersNames(), true));

        setSharedVariables();
    }

    @Override
    public void postProcessMeta(LayerMeta meta) {
        CMDBMeta cmdbMeta = (CMDBMeta) meta;
        Set<String> cmdbNames = new LinkedHashSet<>();
        if (!cmdbMeta.getCMDBNamesList().isEmpty()) {
            if (StringUtils.isNotEmpty(cmdbMeta.getBaseCMDB()) && !cmdbMeta.getCMDBNamesList().contains(cmdbMeta.getBaseCMDB()))
                cmdbNames.add(cmdbMeta.getBaseCMDB());
            for (String name : cmdbMeta.getCMDBNamesList()) {
                cmdbNames.add(name);
            }

        }
        cmdbMeta.setLayersNames(cmdbNames);
        if (cmdbMeta.getLayersNames().isEmpty()) {
            for (Layer layer : layerMap.values()) {
                CMDBLayer cmdbLayer = (CMDBLayer) layer;
                if (cmdbLayer.isActive()) {
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
                        .add("ContentsAsJSON", ((CMDBLayer) layer).getContent()).build());
        return jsonObjectBuilder;
    }

    @Override
    public JsonArrayBuilder buildLayers(LayerMeta meta) {
        CMDBMeta cmdbMeta = (CMDBMeta) meta;
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (Layer layer : layerMap.values()) {
            CMDBLayer cmdbLayer = (CMDBLayer) layer;
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            if (cmdbMeta.isActiveOnly() && !cmdbLayer.isActive())
                continue;
            objectBuilder
                    .add("Name", cmdbLayer.getName())
                    .add("CMDBPath", StringUtils.defaultIfEmpty(cmdbLayer.getPath(), ""))
                    .add("FileSystemPath", cmdbLayer.getFileSystemPath())
                    .add("Base", cmdbLayer.isBase())
                    .add("Active", cmdbLayer.isActive())
                    .add("ParentCMDB", StringUtils.defaultIfEmpty(cmdbLayer.getParentCMDB(), ""))
                    .add("ContentsAsJSON", cmdbLayer.getContent());

            jsonArrayBuilder.add(objectBuilder.build());
        }
        return jsonArrayBuilder;
    }

    public int rmLayers(LayerMeta meta) throws TemplateModelException, IOException {
        super.rmLayers(meta);
        CMDBMeta cmdbMeta = (CMDBMeta) meta;
        Path sourceDirectory = getExistingDirectory(cmdbMeta.getLayersNames(), cmdbMeta.getToPath());
        if (sourceDirectory != null) {
            File file = sourceDirectory.toFile();
            if (cmdbMeta.isForce()) {
                FileUtils.deleteQuietly(file);
            } else {
                if (cmdbMeta.isRecurse())
                    FileUtils.deleteDirectory(file);
                else if (file.delete()) {
                    return 0;
                } else return 1;
            }
        } else {
            cmdbMeta.setMaxDepth(1);
            String sourceStartingPath = StringUtils.substringBeforeLast(cmdbMeta.getToPath(), "/");
            String sourceFilenameGlob = StringUtils.substringAfterLast(cmdbMeta.getToPath(), "/");
            cmdbMeta.setStartingPath(sourceStartingPath);
            cmdbMeta.setFilenameGlob(sourceFilenameGlob);
            List<Path> files = getFilesPerLayerMeta(cmdbMeta);
            if (files != null) {
                for (Path file : files) {
                    if (cmdbMeta.isForce()) {
                        FileUtils.deleteQuietly(file.toFile());
                    } else if (file.toFile().delete()) {
                        return 0;
                    } else return 1;
                }
            }
        }

        return 0;
    }

    @Override
    public int toMethod(Meta meta) throws TemplateModelException, IOException, CloneNotSupportedException {
        super.toMethod(meta);
        CMDBMeta cmdbMeta = (CMDBMeta) meta;
        Object content = cmdbMeta.getContent();

        byte[] result;
        if ("json".equalsIgnoreCase(cmdbMeta.getFormat())) {
            ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
            if (StringUtils.equalsIgnoreCase("pretty", cmdbMeta.getFormatting())) {
                DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
                final String indent = String.join("", Collections.nCopies(cmdbMeta.getIndent(), " "));
                DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter(indent, DefaultIndenter.SYS_LF);
                prettyPrinter.indentObjectsWith(indenter);
                prettyPrinter.indentArraysWith(indenter);
                result = objectMapper.writer(prettyPrinter).writeValueAsBytes(content);
            } else {
                if (StringUtils.equalsIgnoreCase("compressed", cmdbMeta.getFormatting())) {
                    result = objectMapper.writer().writeValueAsBytes(content);
                } else
                    throw new ParameterValueException("Formatting", cmdbMeta.getFormatting(), "pretty, compressed", "compressed");
            }
        } else if ("yml".equalsIgnoreCase(cmdbMeta.getFormat()) || "yaml".equalsIgnoreCase(cmdbMeta.getFormat())) {
            ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
            result = objectMapper.writer().writeValueAsBytes(content);
        } else {
            result = content.toString().getBytes();
        }

        Path destinationFile = getDestinationPath(cmdbMeta, "singleFile");
        if (cmdbMeta.isAppend()) {
            Files.write(destinationFile, result, StandardOpenOption.APPEND);
        } else {
            Files.write(destinationFile, result);
        }
        return 0;
    }

    public int cpLayers(Meta meta) throws TemplateModelException, IOException, CloneNotSupportedException {
        super.cpLayers(meta);
        CMDBMeta cmdbMeta = (CMDBMeta) meta;

        CopyOption copyAttributes = null;
        if (cmdbMeta.isPreserve()) {
            copyAttributes = StandardCopyOption.COPY_ATTRIBUTES;
        }

        Path sourceDirectory = getExistingDirectory(cmdbMeta.getLayersNames(), cmdbMeta.getFromPath());
        if (sourceDirectory != null) {
            Path destinationDirectory = getDestinationPath(cmdbMeta, "directory");
            if (destinationDirectory != null) {
                int maxDepth = 1;
                if (cmdbMeta.isRecurse())
                    maxDepth = Integer.MAX_VALUE;
                for (Object sourceObject : Files.walk(sourceDirectory, maxDepth).toArray()) {
                    Path source = (Path) sourceObject;
                    Path destinationFile = destinationDirectory.resolve(sourceDirectory.relativize(source));
                    if (!destinationFile.equals(destinationDirectory)) {
                        if (cmdbMeta.isPreserve()) {
                            copy(source, destinationFile, StandardCopyOption.COPY_ATTRIBUTES);
                        } else {
                            copy(source, destinationFile, null);
                        }
                    }
                }
            }
        } else {
            String sourceStartingPath = StringUtils.substringBeforeLast(cmdbMeta.getFromPath(), "/");
            String sourceFilenameGlob = StringUtils.substringAfterLast(cmdbMeta.getFromPath(), "/");
            cmdbMeta.setStartingPath(sourceStartingPath);
            cmdbMeta.setFilenameGlob(sourceFilenameGlob);
            List<Path> files = getFilesPerLayerMeta(cmdbMeta);
            if (files != null) {
                if (files.size() == 1) {
                    Path destinationPath = getDestinationPath(cmdbMeta, "singleFile");
                    if (destinationPath != null) {
                        if (Files.isDirectory(destinationPath)) {
                            destinationPath = Paths.get(destinationPath.toString().concat("/").concat(files.get(0).getFileName().toString()));
                        }
                        copy(files.get(0), destinationPath, copyAttributes);
                        return 0;
                    }
                } else {
                    Path destinationDirectory = getDestinationPath(cmdbMeta, "fileNameGlob");
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

    public int mkdirLayers(LayerMeta meta) throws TemplateModelException, IOException {
        super.mkdirLayers(meta);
        CMDBMeta cmdbMeta = (CMDBMeta) meta;
        Path pathToCreate = Paths.get(cmdbMeta.getStartingPath());
        Path pathToScan = Paths.get(cmdbMeta.getStartingPath());
        while (pathToScan != null) {
            for (String layerName : cmdbMeta.getLayersNames()) {
                Layer layer = layerMap.get(layerName);
                Path result = getDirectoryOnFileSystem(pathToScan.toString(), layer.getPath(), layer.getFileSystemPath());
                if (result != null) {
                    if (pathToCreate.equals(pathToScan))
                        return 0;
                    String layerPath = StringUtils.substringAfter(layer.getPath(), forceUnixStyle(pathToScan.toString()));
                    if ("".equalsIgnoreCase(layerPath) && !"/".equalsIgnoreCase(pathToScan.toString()) && !"\\".equalsIgnoreCase(pathToScan.toString())) {
                        layerPath = forceUnixStyle(pathToScan.toString());
                    }

                    String commonPath = StringUtils.substringBeforeLast(layer.getPath(), layerPath);
                    String newPath = StringUtils.substringAfter(forceUnixStyle(pathToCreate.toString()), layerPath);
                    if (commonPath.equalsIgnoreCase("/") && newPath.equalsIgnoreCase("")) {
                        newPath = forceUnixStyle(pathToCreate.toString());
                    }
                    Path fsNewPath = Paths.get(getStartingDir(result.toString()).concat(newPath));
                    File newDirectory = new File(fsNewPath.toString());
                    if (cmdbMeta.isParents()) {
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

    private void updatePath(CMDBLayer cmdbLayer) {
        if (StringUtils.isEmpty(cmdbLayer.getPath())) {
            if (cmdbLayer.isBase()) {
                cmdbLayer.setPath("/");
            } else {
                cmdbLayer.setPath("/default/".concat(cmdbLayer.getName()));
            }
        } else if (!StringUtils.startsWith(cmdbLayer.getPath(), "/")) {
            if (StringUtils.isNotEmpty(cmdbLayer.getParentCMDB())) {
                CMDBLayer parent = (CMDBLayer) layerMap.get(cmdbLayer.getParentCMDB());
                String path = null;
                if (StringUtils.isEmpty(parent.getPath()) || !StringUtils.startsWith(parent.getPath(), "/")) {
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
     *
     * @param baseCMDB      the name of the base CMDB
     * @param CMDBs
     * @param useCMDBPrefix
     * @return
     */
    private Map<String, String> buildCMDBFileSystem(final String baseCMDB, final Map<String, String> CMDBs, boolean useCMDBPrefix, final Set<String> CMDBNames, boolean baseProcessing) throws IOException{
        Map<String, String> cmdbFileSystem = new TreeMap<>();
        Path CMDBPath = null;
        if (StringUtils.isNotEmpty(baseCMDB)) {
            CMDBPath = readJSONFileUsingDirectoryStream(CMDBs.get(baseCMDB), ".cmdb");
        }
        JsonObject jsonObject = null;
        /**
         * if cmdb file exist - read layers from it
         */
        if (CMDBPath != null) {
            JsonReader jsonReader = Json.createReader(new FileReader(CMDBPath.toFile()));
            jsonObject = jsonReader.readObject();
        }

        JsonArray layers = Json.createArrayBuilder().build();
        if (jsonObject != null && jsonObject.containsKey("Layers")) {
            layers = jsonObject.getJsonArray("Layers").asJsonArray();
        }

        /**
         * if there are no layers defined in a base cmdb, add all detected cmdb as layers with a default base path
         */
        if (layers.isEmpty() && baseProcessing) {
            JsonArrayBuilder layersBuilder = Json.createArrayBuilder();
            for (String name : CMDBs.keySet()) {
                if (name.equalsIgnoreCase(baseCMDB))
                    continue;
                if (!CMDBNames.isEmpty() && !CMDBNames.contains(name))
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
            if (!CMDBNames.isEmpty() && !CMDBNames.contains(layerName))
                continue;
            String basePath = layer.getString("BasePath");
            String CMDBPrefix = useCMDBPrefix ? CMDBPath.getParent().getParent().getFileName().toString().concat("_") : "";
            cmdbFileSystem.put(CMDBPrefix.concat(layerName), basePath);
            if (!baseCMDB.equalsIgnoreCase(layerName)) {
                cmdbFileSystem.putAll(buildCMDBFileSystem(layerName, CMDBs, useCMDBPrefix, CMDBNames, false));
            }
        }
        return cmdbFileSystem;
    }

    private Map<String, String> processCMDBFileSystem(String baseCMDBName, final Map<String, String> cmdbFileSystem) {
        Map<String, String> result = new TreeMap<>();
        String root = cmdbFileSystem.containsKey(baseCMDBName) ? cmdbFileSystem.get(baseCMDBName) : "/default/";
        for (String CMDBName : cmdbFileSystem.keySet()) {
            String basePath = cmdbFileSystem.get(CMDBName);
            if (!basePath.startsWith("/")) {
                basePath = root.concat(basePath);
            }
            result.put(CMDBName, basePath);
        }
        if (StringUtils.isNotEmpty(baseCMDBName)) {
            result.put(baseCMDBName, root);
        }
        return result;
    }

    protected Path getDestinationPath(CMDBMeta sourceMeta, String sourceType) throws TemplateModelException, IOException, CloneNotSupportedException {
        if ("directory".equalsIgnoreCase(sourceType)) {
            //destination can be an existing or a new directory
            //check if destination directory exists
            Path destinationDirectory = null;
            destinationDirectory = getExistingDirectory(sourceMeta.getLayersNames(), sourceMeta.getToPath());
            //if destination directory doesn't exist, create it
            if (destinationDirectory == null) {
                CMDBMeta destinationMeta = (CMDBMeta) sourceMeta.clone();
                destinationMeta.setStartingPath(sourceMeta.getToPath());
                destinationMeta.setParents(Boolean.FALSE);
                mkdirLayers(destinationMeta);
                destinationDirectory = getExistingDirectory(sourceMeta.getLayersNames(), sourceMeta.getToPath());
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
