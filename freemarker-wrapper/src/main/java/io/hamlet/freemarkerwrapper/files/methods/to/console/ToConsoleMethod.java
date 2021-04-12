package io.hamlet.freemarkerwrapper.files.methods.to.console;

import freemarker.template.*;
import io.hamlet.freemarkerwrapper.ParameterValueException;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.console.ConsoleMeta;
import io.hamlet.freemarkerwrapper.files.methods.WrapperMethod;
import io.hamlet.freemarkerwrapper.files.processors.console.ConsoleProcessor;
import io.hamlet.freemarkerwrapper.utils.FreemarkerUtil;

import java.util.List;

public class ToConsoleMethod extends WrapperMethod implements TemplateMethodModelEx {

    public static String METHOD_NAME = "toConsole";

    public ToConsoleMethod() {
        super(METHOD_NAME, 1,2);
    }

    @Override
    protected void init() {
        meta = new ConsoleMeta();
        processor = new ConsoleProcessor();
    }

    @Override
    protected void parseArguments(List args) throws TemplateModelException {
        Object contentObj = FreemarkerUtil.ftlVarToCoreJavaObject((TemplateModel) args.get(0));

        String sendTo = "stdout";
        if(args.size() == 2) {
            TemplateHashModelEx options = (TemplateHashModelEx) args.get(1);
            TemplateModelIterator iterator = options.keys().iterator();
            while (iterator.hasNext()) {
                TemplateModel keyModel = iterator.next();
                String key = keyModel.toString();
                Object keyObj = options.get(key);
                if ("SendTo".equalsIgnoreCase(key)) {
                    sendTo = FreemarkerUtil.getOptionStringValue(keyObj);
                }
            }
        }
        ConsoleMeta consoleMeta = (ConsoleMeta) meta;
        consoleMeta.setSendTo(sendTo);
        consoleMeta.setContent(contentObj);
    }

    public TemplateModel process() throws ParameterValueException {
        int result = ((ConsoleProcessor)processor).toMethod(meta);
        return new SimpleNumber(result);
    }

}
