package io.hamlet.freemarkerwrapper;

import freemarker.template.TemplateModelException;

import java.io.IOException;

public class WrapperCloneNotSupportedException extends TemplateModelException {
    public WrapperCloneNotSupportedException(CloneNotSupportedException e) {
        super(e);
    }
}
