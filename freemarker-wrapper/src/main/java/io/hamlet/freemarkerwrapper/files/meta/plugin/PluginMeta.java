package io.hamlet.freemarkerwrapper.files.meta.plugin;

import io.hamlet.freemarkerwrapper.files.meta.LayerMeta;

import java.util.List;

public class PluginMeta extends LayerMeta {

    protected List<String> layers;

    public List<String> getLayers() {
        return layers;
    }

    public void setLayers(List<String> layers) {
        this.layers = layers;
    }

    @Override
    public String getIncludeInformationOptionName() {
        return "IncludePluginInformation";
    }
}
