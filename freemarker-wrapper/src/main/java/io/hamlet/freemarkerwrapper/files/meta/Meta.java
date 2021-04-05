package io.hamlet.freemarkerwrapper.files.meta;

public abstract class Meta implements Cloneable {
    private Object content;

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public Object clone() throws
            CloneNotSupportedException {
        return super.clone();
    }

}
