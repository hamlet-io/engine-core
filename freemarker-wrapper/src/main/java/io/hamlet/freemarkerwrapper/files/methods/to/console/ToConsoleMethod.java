package io.hamlet.freemarkerwrapper.files.methods.to.console;

import freemarker.template.*;
import io.hamlet.freemarkerwrapper.files.meta.console.ConsoleMeta;
import io.hamlet.freemarkerwrapper.files.methods.to.ToMethod;
import io.hamlet.freemarkerwrapper.files.processors.console.ConsoleProcessor;
import io.hamlet.freemarkerwrapper.utils.FreemarkerUtil;

import java.util.List;

public class ToConsoleMethod extends ToMethod implements TemplateMethodModelEx {

    public TemplateModel exec(List args) throws TemplateModelException {
        if (args.size() > 2 || args.size() <1) {
            throw new TemplateModelException("Wrong arguments");
        }

        Object contentObj = FreemarkerUtil.ftlVarToCoreJavaObject((TemplateModel) args.get(0));

        meta = new ConsoleMeta();
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
        processor = new ConsoleProcessor();
        return super.process();
    }
}
