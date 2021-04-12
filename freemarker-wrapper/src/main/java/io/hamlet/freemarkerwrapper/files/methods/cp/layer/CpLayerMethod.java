package io.hamlet.freemarkerwrapper.files.methods.cp.layer;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.WrapperTemplateExceptionHandler;
import io.hamlet.freemarkerwrapper.files.methods.LayerMethod;
import io.hamlet.freemarkerwrapper.files.processors.layer.LayerProcessor;

import java.io.IOException;

public abstract class CpLayerMethod extends LayerMethod {

    public CpLayerMethod(int numberOfArguments, String methodName) {
        super(numberOfArguments, methodName);
    }

    public TemplateModel process() throws TemplateModelException, IOException, CloneNotSupportedException {
        int result = ((LayerProcessor)processor).cpLayers(meta);
        if (result == 0) {
            syncFileSystem();
        }
        return new SimpleNumber(result);
    }
}
