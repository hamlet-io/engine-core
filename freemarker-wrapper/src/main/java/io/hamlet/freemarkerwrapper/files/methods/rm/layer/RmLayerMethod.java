package io.hamlet.freemarkerwrapper.files.methods.rm.layer;

import freemarker.core.Environment;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModel;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.layer.LayerMeta;
import io.hamlet.freemarkerwrapper.files.processors.layer.LayerProcessor;

import java.io.IOException;

public abstract class RmLayerMethod {

    protected LayerMeta meta;
    protected LayerProcessor layerProcessor;

    public TemplateModel process() {
        layerProcessor.setConfiguration(Environment.getCurrentEnvironment().getConfiguration());
        int result;
        try {
            result = layerProcessor.rmLayers(meta);
            if (result == 0 && meta.isSync()) {
                layerProcessor.createLayerFileSystem(meta);
            }
        } catch (RunFreeMarkerException e) {
            e.printStackTrace();
            result = 1;
        } catch (IOException e) {
            e.printStackTrace();
            result = 1;
        }
        return new SimpleNumber(result);
    }
}
