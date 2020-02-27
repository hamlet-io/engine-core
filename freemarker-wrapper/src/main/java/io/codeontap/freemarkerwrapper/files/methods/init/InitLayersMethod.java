package io.codeontap.freemarkerwrapper.files.methods.init;

import freemarker.core.Environment;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.codeontap.freemarkerwrapper.RunFreeMarkerException;
import io.codeontap.freemarkerwrapper.files.meta.LayerMeta;
import io.codeontap.freemarkerwrapper.files.processors.LayerProcessor;

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
