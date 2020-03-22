package io.codeontap.freemarkerwrapper.files.methods.mkdir;

import freemarker.core.Environment;
import freemarker.template.*;
import io.codeontap.freemarkerwrapper.RunFreeMarkerException;
import io.codeontap.freemarkerwrapper.files.adapters.JsonStringAdapter;
import io.codeontap.freemarkerwrapper.files.meta.LayerMeta;
import io.codeontap.freemarkerwrapper.files.processors.LayerProcessor;

import javax.json.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        } catch (IOException e) {
            e.printStackTrace();
            result = 2;
        }
        return new SimpleNumber(result);
    }
}
