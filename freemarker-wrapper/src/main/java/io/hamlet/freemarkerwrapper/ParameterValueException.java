package io.hamlet.freemarkerwrapper;

import freemarker.template.TemplateModelException;

public class ParameterValueException extends TemplateModelException {
    public ParameterValueException(String parameterName,String parameterValue,String allowedValues,String defaultValue) {
        super(String.format("Unexpected value of %s parameter: %s. Allowed values: %s. Default value: %s.", parameterName, parameterValue, allowedValues, defaultValue));
    }

    public ParameterValueException(String parameterName,String parameterValue,String allowedValues) {
        super(String.format("Unexpected value of %s parameter: %s. Allowed values: %s.", parameterName, parameterValue, allowedValues));
    }
}
