package io.hamlet.freemarkerwrapper.files.methods.init.layer;

import freemarker.core.Environment;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.layer.LayerMeta;
import io.hamlet.freemarkerwrapper.files.processors.layer.LayerProcessor;

import javax.json.JsonArray;

public abstract class InitLayersMethod {

    protected LayerMeta meta;
    protected LayerProcessor layerProcessor;

    public TemplateModel process() {
        layerProcessor.setConfiguration(Environment.getCurrentEnvironment().getConfiguration());
        JsonArray result = null;
        try {
            layerProcessor.initLayers(meta);
        } catch (RunFreeMarkerException e) {
            e.printStackTrace();
        }
        return new SimpleSequence(JsonArray.EMPTY_JSON_ARRAY, Environment.getCurrentEnvironment().getConfiguration().getObjectWrapper());
    }
}
