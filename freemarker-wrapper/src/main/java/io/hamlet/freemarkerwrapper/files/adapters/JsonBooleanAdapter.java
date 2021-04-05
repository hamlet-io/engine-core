package io.hamlet.freemarkerwrapper.files.adapters;

import freemarker.template.*;

import javax.json.JsonValue;

public class JsonBooleanAdapter extends WrappingTemplateModel implements TemplateBooleanModel,
        AdapterTemplateModel {

    private final JsonValue jsonObject;

    public JsonBooleanAdapter(JsonValue jsonObject, ObjectWrapper ow) {
        super(ow);  // coming from WrappingTemplateModel
        this.jsonObject = jsonObject;
    }

    @Override  // coming from AdapterTemplateModel
    public Object getAdaptedObject(Class hint) {
        return jsonObject;
    }

    @Override
    public boolean getAsBoolean() throws TemplateModelException {
        switch (jsonObject.getValueType()) {
            case TRUE:
                return Boolean.TRUE;
            case FALSE:
                return Boolean.FALSE;
        }
        return false;
    }
}
