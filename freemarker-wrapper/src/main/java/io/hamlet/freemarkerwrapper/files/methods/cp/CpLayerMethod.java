package io.hamlet.freemarkerwrapper.files.methods.cp;

import freemarker.core.Environment;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModel;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.LayerMeta;
import io.hamlet.freemarkerwrapper.files.processors.LayerProcessor;

import java.io.IOException;

public abstract class CpLayerMethod {

    protected LayerMeta meta;
    protected LayerProcessor layerProcessor;

    public TemplateModel process() {
        layerProcessor.setConfiguration(Environment.getCurrentEnvironment().getConfiguration());
        int result;
        try {
            result = layerProcessor.cpLayers(meta);
            if (result == 0 && meta.isSync()){
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
