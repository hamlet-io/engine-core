package io.hamlet.freemarkerwrapper.files.methods.mkdir.layer;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.layer.LayerMeta;
import io.hamlet.freemarkerwrapper.files.methods.LayerMethod;
import io.hamlet.freemarkerwrapper.files.processors.layer.LayerProcessor;

import java.io.IOException;

public abstract class MkdirLayerMethod extends LayerMethod {

    public MkdirLayerMethod(int numberOfArguments, String methodName) {
        super(numberOfArguments, methodName);
    }

    public TemplateModel process() throws TemplateModelException, IOException {
        int result = ((LayerProcessor)processor).mkdirLayers((LayerMeta) meta);
        if (result == 0) {
            syncFileSystem();
        }
        return new SimpleNumber(result);
    }
}
