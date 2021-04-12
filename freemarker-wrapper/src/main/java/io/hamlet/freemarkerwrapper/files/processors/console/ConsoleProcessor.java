package io.hamlet.freemarkerwrapper.files.processors.console;

import io.hamlet.freemarkerwrapper.ParameterValueException;
import io.hamlet.freemarkerwrapper.files.meta.Meta;
import io.hamlet.freemarkerwrapper.files.meta.console.ConsoleMeta;
import io.hamlet.freemarkerwrapper.files.processors.Processor;
import org.apache.commons.lang3.StringUtils;

public class ConsoleProcessor extends Processor {

    public int toMethod(Meta meta) throws ParameterValueException {
        ConsoleMeta consoleMeta = (ConsoleMeta) meta;
        String sendTo = consoleMeta.getSendTo();
        Object content = consoleMeta.getContent();
        if (StringUtils.equalsIgnoreCase("stdout", sendTo)) {
            System.out.print(content.toString());
        } else if (StringUtils.equalsIgnoreCase("stderr", sendTo)) {
            System.err.print(content.toString());
        } else {
            throw new ParameterValueException( "SendTo", sendTo, "stdout, stderr", "stdout");
        }
        return 0;
    }

}
