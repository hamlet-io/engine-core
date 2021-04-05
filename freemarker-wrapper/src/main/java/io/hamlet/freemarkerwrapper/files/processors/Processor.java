package io.hamlet.freemarkerwrapper.files.processors;

import freemarker.template.Configuration;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.Meta;

import java.io.IOException;

public abstract class Processor {

    protected Configuration configuration;

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public abstract int toMethod(Meta meta) throws RunFreeMarkerException, IOException;

}
