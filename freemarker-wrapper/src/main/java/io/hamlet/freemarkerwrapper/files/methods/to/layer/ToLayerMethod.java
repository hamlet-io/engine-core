package io.hamlet.freemarkerwrapper.files.methods.to.layer;

import freemarker.core.Environment;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModel;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.layer.LayerMeta;
import io.hamlet.freemarkerwrapper.files.methods.to.ToMethod;
import io.hamlet.freemarkerwrapper.files.processors.layer.LayerProcessor;

public abstract class ToLayerMethod extends ToMethod {

    public TemplateModel process() {

        processor.setConfiguration(Environment.getCurrentEnvironment().getConfiguration());
        TemplateModel result = super.process();
        if (result instanceof SimpleNumber) {
            if (((SimpleNumber) result).getAsNumber().intValue() == 0) {
                LayerMeta LayerMeta = (LayerMeta) meta;
                if (LayerMeta.isSync()) {
                    try {
                        ((LayerProcessor) processor).createLayerFileSystem(LayerMeta);
                    } catch (RunFreeMarkerException e) {
                        e.printStackTrace();
                        return new SimpleNumber(1);
                    }
                }
            }
        }
        return result;
    }
}
