package io.hamlet.freemarkerwrapper.files.methods;

import freemarker.template.TemplateModelException;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.layer.LayerMeta;
import io.hamlet.freemarkerwrapper.files.meta.layer.plugin.PluginMeta;
import io.hamlet.freemarkerwrapper.files.processors.layer.LayerProcessor;
import io.hamlet.freemarkerwrapper.files.processors.layer.plugin.PluginProcessor;

import java.io.IOException;

public abstract class LayerMethod extends WrapperMethod{

    public LayerMethod(int numberOfArguments, String methodName) {
        super(numberOfArguments, methodName);
    }

    public void syncFileSystem() throws TemplateModelException, IOException {
        if (((LayerMeta)meta).isSync()) {
            ((LayerProcessor)processor).createLayerFileSystem((LayerMeta) meta);
        }
    }

}
