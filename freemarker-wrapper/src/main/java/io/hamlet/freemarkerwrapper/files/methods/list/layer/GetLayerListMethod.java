package io.hamlet.freemarkerwrapper.files.methods.list.layer;

import freemarker.core.Environment;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.layer.LayerMeta;
import io.hamlet.freemarkerwrapper.files.processors.layer.LayerProcessor;

import javax.json.JsonArray;

public abstract class GetLayerListMethod {

    protected LayerMeta meta;
    protected LayerProcessor layerProcessor;

    public TemplateModel process() {
        layerProcessor.setConfiguration(Environment.getCurrentEnvironment().getConfiguration());
        JsonArray result = null;
        try {
            result = layerProcessor.getLayers(meta);
        } catch (RunFreeMarkerException e) {
            e.printStackTrace();
        }
        return new SimpleSequence(result, Environment.getCurrentEnvironment().getConfiguration().getObjectWrapper());
    }
}
