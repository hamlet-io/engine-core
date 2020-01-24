package io.codeontap.freemarkerwrapper.files.processors.plugin;


import io.codeontap.freemarkerwrapper.files.layers.Layer;
import io.codeontap.freemarkerwrapper.files.meta.plugin.PluginMeta;
import io.codeontap.freemarkerwrapper.RunFreeMarkerException;
import io.codeontap.freemarkerwrapper.files.meta.LayerMeta;
import io.codeontap.freemarkerwrapper.files.layers.plugin.PluginLayer;
import io.codeontap.freemarkerwrapper.files.processors.LayerProcessor;

import javax.json.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * TODO: add staringPath processing
 */
public class PluginProcessor extends LayerProcessor {
    @Override
    public void createLayerFileSystem(LayerMeta meta) throws RunFreeMarkerException {
        PluginMeta pluginMeta = (PluginMeta) meta;
        fileSystem = new HashMap<>();
        for (String layer : pluginMeta.getLayers()) {
            if(!Files.isDirectory(Paths.get(layer))) {
                throw new RunFreeMarkerException(
                        String.format("Unable to read plugin layer path \"%s\"", layer));
            } else {
                PluginLayer pluginLayer = new PluginLayer(Paths.get(layer).getFileName().toString(), "/", layer);
                layerMap.put(pluginLayer.getName(), pluginLayer);
                fileSystem.put(pluginLayer.getName(), pluginLayer.getFileSystemPath());
            }
        }

        if (layerMap.isEmpty()) {
            fileSystem = null;
        } else {
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
                    .add("FileSystemPath",layer.getFileSystemPath());;
            jsonArrayBuilder.add(objectBuilder.build());
        }
        return jsonArrayBuilder;
    }
}