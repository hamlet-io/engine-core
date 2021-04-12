package io.hamlet.freemarkerwrapper.files.methods.set.status;

import freemarker.template.*;
import io.hamlet.freemarkerwrapper.ParameterValueException;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.status.StatusMeta;
import io.hamlet.freemarkerwrapper.files.methods.WrapperMethod;
import io.hamlet.freemarkerwrapper.files.processors.status.StatusProcessor;
import io.hamlet.freemarkerwrapper.utils.FreemarkerUtil;

import java.util.List;

public class SetExitStatusMethod extends WrapperMethod implements TemplateMethodModelEx {

    public static String METHOD_NAME = "setExitStatus";

    public SetExitStatusMethod() {
        super(1, METHOD_NAME);
    }

    public TemplateModel process() throws ParameterValueException {
        return new SimpleNumber(((StatusProcessor)processor).setStatusMethod((StatusMeta) meta));
    }

    @Override
    protected void init() {
        meta = new StatusMeta();
        processor = new StatusProcessor();
    }

    @Override
    protected void parseArguments(List args) throws TemplateModelException {
        Object statusObj = FreemarkerUtil.ftlVarToCoreJavaObject((TemplateModel) args.get(0));
        ((StatusMeta)meta).setStatus(Integer.valueOf(statusObj.toString()));
    }
}
