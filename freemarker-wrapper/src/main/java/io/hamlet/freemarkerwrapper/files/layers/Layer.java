package io.hamlet.freemarkerwrapper.files.layers;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class Layer {
    protected String name;
    protected String path;
    protected String fileSystemPath;
    protected boolean skip;

    public Layer(String name, String path, String fileSystemPath) {
        this.name = name;
        this.path = path;
        this.fileSystemPath = fileSystemPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileSystemPath() {
        return fileSystemPath;
    }

    public void setFileSystemPath(String fileSystemPath) {
        this.fileSystemPath = fileSystemPath;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Layer)) return false;

        Layer that = (Layer) o;

        return new EqualsBuilder()
                .append(getName(), that.getName())
                .append(getPath(), that.getPath())
                .append(getFileSystemPath(), that.getFileSystemPath())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(getPath())
                .append(getFileSystemPath())
                .toHashCode();
    }
}
