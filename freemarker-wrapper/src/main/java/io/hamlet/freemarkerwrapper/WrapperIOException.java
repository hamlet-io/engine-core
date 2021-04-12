package io.hamlet.freemarkerwrapper;

import freemarker.template.TemplateModelException;

import java.io.IOException;

public class WrapperIOException extends TemplateModelException {
    public WrapperIOException (IOException e) {
        super(e);
    }
}
