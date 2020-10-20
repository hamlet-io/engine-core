package io.hamlet.freemarkerwrapper.files.adapters;

import freemarker.template.*;

import javax.json.JsonNumber;

public class JsonNumberAdapter extends WrappingTemplateModel implements TemplateNumberModel,
        AdapterTemplateModel {

    private final JsonNumber jsonObject;

    public JsonNumberAdapter(JsonNumber jsonObject, ObjectWrapper ow) {
        super(ow);  // coming from WrappingTemplateModel
        this.jsonObject = jsonObject;
    }

    @Override  // coming from AdapterTemplateModel
    public Object getAdaptedObject(Class hint) {
        return jsonObject;
    }

    @Override
    public Number getAsNumber() throws TemplateModelException {
        return jsonObject.numberValue();
    }
}
