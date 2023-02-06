package io.hamlet.freemarkerwrapper.files.processors.layer.plugin;


import freemarker.template.TemplateModelException;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.layers.Layer;
import io.hamlet.freemarkerwrapper.files.layers.plugin.PluginLayer;
import io.hamlet.freemarkerwrapper.files.meta.layer.LayerMeta;
import io.hamlet.freemarkerwrapper.files.meta.layer.plugin.PluginMeta;
import io.hamlet.freemarkerwrapper.files.processors.layer.LayerProcessor;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;

public class PluginProcessor extends LayerProcessor {

    public PluginProcessor() {
        fileSystemShareVariableName = "pluginFileSystem";
        layerMapShareVariableName = "pluginLayerMap";
    }

    public void createLayerFileSystem(LayerMeta meta) throws TemplateModelException {
        PluginMeta pluginMeta = (PluginMeta) meta;
        fileSystem = new TreeMap<>();

        for (String layer : pluginMeta.getLayers()) {
            if (!Files.isDirectory(Paths.get(layer))) {
                throw new RunFreeMarkerException(
                        String.format("Unable to read plugin layer path \"%s\"", layer));
            } else {
                PluginLayer pluginLayer = new PluginLayer(Paths.get(layer).getFileName().toString(), "/", layer);
                layerMap.put(pluginLayer.getName(), pluginLayer);
                fileSystem.put(pluginLayer.getName(), pluginLayer.getFileSystemPath());
            }
        }

        setSharedVariables();
    }

    @Override
    public void postProcessMeta(LayerMeta meta) {
        if (!layerMap.isEmpty()) {
            meta.setLayersNames(layerMap.keySet());
        }
    }

    @Override
    public JsonObjectBuilder buildInformation(JsonObjectBuilder jsonObjectBuilder, Layer layer, Path file) {
        jsonObjectBuilder.add("Plugin",
                Json.createObjectBuilder()
                        .add("Name", layer.getName())
                        .add("BasePath", layer.getPath())
                        .add("File", file.toString()));
        return jsonObjectBuilder;
    }


    @Override
    public JsonArrayBuilder buildLayers(LayerMeta meta) {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (Layer layer : layerMap.values()) {
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder
                    .add("Name", layer.getName())
                    .add("FileSystemPath", layer.getFileSystemPath());
            jsonArrayBuilder.add(objectBuilder.build());
        }
        return jsonArrayBuilder;
    }
}
