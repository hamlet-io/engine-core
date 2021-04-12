package io.hamlet.freemarkerwrapper.files.processors.status;

import io.hamlet.freemarkerwrapper.ParameterValueException;
import io.hamlet.freemarkerwrapper.files.meta.status.StatusMeta;
import io.hamlet.freemarkerwrapper.files.processors.Processor;

public class StatusProcessor extends Processor {

    public static String existStatusVariableName = "exitStatus";

    public int setStatusMethod(StatusMeta meta) throws ParameterValueException {

        int status = meta.getStatus();
        if (status > 109 && status < 256) {
            System.setProperty(existStatusVariableName, String.valueOf(status));
        } else {
            throw new ParameterValueException("setExitStatus", Integer.toString(status), "110-199, 210-254");
        }
        return 0;
    }

}
