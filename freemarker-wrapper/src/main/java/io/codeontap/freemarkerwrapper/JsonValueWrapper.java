package io.codeontap.freemarkerwrapper;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

public class JsonValueWrapper extends DefaultObjectWrapper {

    public JsonValueWrapper(Version incompatibleImprovements) {
        super(incompatibleImprovements);
    }

    @Override
    protected TemplateModel handleUnknownType(final Object obj) throws TemplateModelException {
        if(obj instanceof JsonValue){
            switch (((JsonValue) obj).getValueType()){
                case FALSE:
                case TRUE:
                    return new JsonBooleanAdapter((JsonValue) obj, this);
                case NUMBER:
                    return new JsonNumberAdapter((JsonNumber) obj, this);
                case STRING:
                    return new JsonStringAdapter((JsonString) obj, this);
                case NULL:
                case ARRAY:
                case OBJECT:
                    return super.handleUnknownType(obj);
            }
        }
        return super.handleUnknownType(obj);
    }

}