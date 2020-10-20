package io.hamlet.freemarkerwrapper.files.methods.mkdir;

import freemarker.core.Environment;
import freemarker.template.*;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.LayerMeta;
import io.hamlet.freemarkerwrapper.files.processors.LayerProcessor;

public abstract class MkdirLayerMethod {

    protected LayerMeta meta;
    protected LayerProcessor layerProcessor;

    public TemplateModel process() {
        layerProcessor.setConfiguration(Environment.getCurrentEnvironment().getConfiguration());
        int result;
        try {
            result = layerProcessor.mkdirLayers(meta);
            if (result == 0 && meta.isSync()){
                layerProcessor.createLayerFileSystem(meta);
            }
        } catch (RunFreeMarkerException e) {
            e.printStackTrace();
            result = 1;
        }
        return new SimpleNumber(result);
    }
}
