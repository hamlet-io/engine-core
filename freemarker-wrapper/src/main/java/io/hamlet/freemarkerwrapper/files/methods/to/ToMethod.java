package io.hamlet.freemarkerwrapper.files.methods.to;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModel;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.Meta;
import io.hamlet.freemarkerwrapper.files.processors.Processor;

import java.io.IOException;

public abstract class ToMethod {

    protected Meta meta;
    protected Processor processor;

    public TemplateModel process() {
        int result;
        try {
            result = processor.toMethod(meta);
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
