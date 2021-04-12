package io.hamlet.freemarkerwrapper.files.methods.list.layer;

import freemarker.core.Environment;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.layer.LayerMeta;
import io.hamlet.freemarkerwrapper.files.methods.LayerMethod;
import io.hamlet.freemarkerwrapper.files.processors.layer.LayerProcessor;

import javax.json.JsonArray;
import java.io.IOException;

public abstract class GetLayerListMethod extends LayerMethod {

    public GetLayerListMethod(int numberOfArguments, String methodName) {
        super(numberOfArguments, methodName);
    }

    public TemplateModel process() throws TemplateModelException, IOException {
        JsonArray result = ((LayerProcessor)processor).getLayers((LayerMeta) meta);
        return new SimpleSequence(result, Environment.getCurrentEnvironment().getConfiguration().getObjectWrapper());
    }
}
