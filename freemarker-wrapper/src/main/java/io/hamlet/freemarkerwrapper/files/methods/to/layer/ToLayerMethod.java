package io.hamlet.freemarkerwrapper.files.methods.to.layer;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.methods.LayerMethod;
import io.hamlet.freemarkerwrapper.files.processors.layer.LayerProcessor;

import java.io.IOException;

public abstract class ToLayerMethod extends LayerMethod {

    public ToLayerMethod(int numberOfArguments, String methodName) {
        super(numberOfArguments, methodName);
    }

    public TemplateModel process() throws TemplateModelException, IOException, CloneNotSupportedException {
        int result = ((LayerProcessor) processor).toMethod(meta);
        if (result ==  0) {
            syncFileSystem();
        }
        return new SimpleNumber(result);
    }
}
