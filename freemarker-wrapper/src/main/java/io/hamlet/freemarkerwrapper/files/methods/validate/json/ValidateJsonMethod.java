package io.hamlet.freemarkerwrapper.files.methods.validate.json;

import freemarker.core.Environment;
import freemarker.template.*;
import io.hamlet.freemarkerwrapper.files.meta.validate.JsonMeta;
import io.hamlet.freemarkerwrapper.files.methods.WrapperMethod;
import io.hamlet.freemarkerwrapper.files.processors.validate.ValidateProcessor;

import javax.json.JsonValue;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ValidateJsonMethod extends WrapperMethod implements TemplateMethodModelEx {
    public static String METHOD_NAME = "validateJson";

    public ValidateJsonMethod() {
        super(METHOD_NAME, 2,3);
    }

    @Override
    public TemplateModel process() throws TemplateModelException, IOException, CloneNotSupportedException {
        Map result = ((ValidateProcessor)processor).toMethod(meta);
        return new SimpleHash(result, Environment.getCurrentEnvironment().getConfiguration().getObjectWrapper());
    }

    @Override
    protected void init() {
        meta = new JsonMeta();
        processor = new ValidateProcessor();
    }

    @Override
    protected void parseArguments(List args) throws TemplateModelException {
        JsonMeta jsonMeta = (JsonMeta) meta;
        Object documentObject = args.get(0);
        Object schemaObject = args.get(1);
        if(documentObject instanceof TemplateModel){
            jsonMeta.setDocument(documentObject.toString());
        }
        if(schemaObject instanceof TemplateModel){
            jsonMeta.setSchema(schemaObject.toString());
        }
    }
}
