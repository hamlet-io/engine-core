package io.hamlet.freemarkerwrapper.files.meta.validate;

import io.hamlet.freemarkerwrapper.files.meta.Meta;

public class JsonMeta extends Meta{
    private String document;
    private String schema;

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
