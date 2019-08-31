package io.codeontap.freemarkerwrapper;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;

import javax.json.JsonObject;
import javax.json.JsonString;

public class JsonStringWrapper extends DefaultObjectWrapper {

    public JsonStringWrapper(Version incompatibleImprovements) {
        super(incompatibleImprovements);
    }

    @Override
    protected TemplateModel handleUnknownType(final Object obj) throws TemplateModelException {
        if (obj instanceof JsonString) {
            return new JsonStringAdapter((JsonString) obj, this);
        } else if(obj instanceof JsonObject){
            return handleUnknownType(obj);
        }

        return super.handleUnknownType(obj);
    }

}