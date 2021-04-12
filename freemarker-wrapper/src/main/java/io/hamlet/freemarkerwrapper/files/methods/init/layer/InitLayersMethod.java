package io.hamlet.freemarkerwrapper.files.methods.init.layer;

import freemarker.core.Environment;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.layer.LayerMeta;
import io.hamlet.freemarkerwrapper.files.methods.LayerMethod;
import io.hamlet.freemarkerwrapper.files.methods.WrapperMethod;
import io.hamlet.freemarkerwrapper.files.processors.layer.LayerProcessor;

import javax.json.JsonArray;
import java.io.IOException;

public abstract class InitLayersMethod extends LayerMethod {

    public InitLayersMethod(int numberOfArguments, String methodName) {
        super(numberOfArguments, methodName);
    }

    public TemplateModel process() throws TemplateModelException, IOException {
        ((LayerProcessor)processor).initLayers((LayerMeta) meta);
        return new SimpleSequence(JsonArray.EMPTY_JSON_ARRAY, Environment.getCurrentEnvironment().getConfiguration().getObjectWrapper());
    }
}
