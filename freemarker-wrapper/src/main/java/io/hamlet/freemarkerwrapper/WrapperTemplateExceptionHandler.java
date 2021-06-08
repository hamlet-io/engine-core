package io.hamlet.freemarkerwrapper;

import freemarker.core.Environment;
import freemarker.core.StopException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.PrintWriter;
import java.io.Writer;

public class WrapperTemplateExceptionHandler implements TemplateExceptionHandler {

    public static final int STOP_EXCEPTION_EXIT_CODE=100;
    public static final int TEMPLATE_EXCEPTION_EXIT_CODE=101;
    public static final int EXCEPTION_EXIT_CODE=102;

    public static final int IO_EXCEPTION_EXIT_CODE=200;
    public static final int CLI_PARSE_EXCEPTION_EXIT_CODE=201;
    public static final int NUMBER_OF_ARGUMENTS_EXCEPTION_EXIT_CODE=202;
    public static final int PARAMETER_VALUE_EXCEPTION_EXIT_CODE=203;
    public static final int CLONE_NOT_SUPPORTED_EXCEPTION_EXIT_CODE=204;
    public static final int RUN_FREEMARKER_EXCEPTION_EXIT_CODE=209;

    @Override
    public void handleTemplateException(TemplateException te, Environment env, Writer out) {
        te.printStackTrace(System.err, true, true, true);
        if (te instanceof NumberOfArgumentsException) {
            System.exit(NUMBER_OF_ARGUMENTS_EXCEPTION_EXIT_CODE);
        } else if (te instanceof ParameterValueException) {
            System.exit(PARAMETER_VALUE_EXCEPTION_EXIT_CODE);
        } else if (te instanceof WrapperCloneNotSupportedException) {
            System.exit(CLONE_NOT_SUPPORTED_EXCEPTION_EXIT_CODE);
        } else if (te instanceof WrapperIOException) {
            System.exit(IO_EXCEPTION_EXIT_CODE);
        } else if (te instanceof RunFreeMarkerException) {
            System.exit(RUN_FREEMARKER_EXCEPTION_EXIT_CODE);
        }
        System.exit(TEMPLATE_EXCEPTION_EXIT_CODE);
    }
}
