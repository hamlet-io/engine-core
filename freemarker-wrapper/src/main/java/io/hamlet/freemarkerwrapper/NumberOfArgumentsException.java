package io.hamlet.freemarkerwrapper;

import freemarker.template.TemplateModelException;

public class NumberOfArgumentsException extends TemplateModelException {
    public NumberOfArgumentsException(int expected, int actual, String methodName) {
        super(String.format("Incorrect number of arguments for the method %s applied. Expected - %s, actual - %s.", methodName, expected, actual));
    }

    public NumberOfArgumentsException(int minExpected, int maxExpected, int actual, String methodName) {
        super(String.format("Incorrect number of arguments for the method %s applied. Min - %s, max - %s, actual - %s.", methodName, minExpected, maxExpected, actual));
    }
}
