package io.hamlet.freemarkerwrapper;

import freemarker.template.TemplateModelException;

public class RunFreeMarkerException extends TemplateModelException {
    public RunFreeMarkerException(String message) {
        super(message);
    }
}
