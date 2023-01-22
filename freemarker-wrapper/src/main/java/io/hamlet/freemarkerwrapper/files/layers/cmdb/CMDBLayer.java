package io.hamlet.freemarkerwrapper.files.layers.cmdb;

import io.hamlet.freemarkerwrapper.files.layers.Layer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import jakarta.json.JsonObject;
import java.util.HashSet;
import java.util.Set;

public class CMDBLayer extends Layer {
    private boolean base = false;
    private boolean active = false;
    private String parentCMDB;
    private Set<String> children = new HashSet<>();
    private JsonObject content;

    public CMDBLayer(String name, String path, String fileSystemPath) {
        super(name, path, fileSystemPath);
    }

    public CMDBLayer(String name, String fileSystemPath) {
        super(name, null, fileSystemPath);
    }

    public boolean isBase() {
        return base;
    }

    public void setBase(boolean base) {
        this.base = base;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getParentCMDB() {
        return parentCMDB;
    }

    public void setParentCMDB(String parentCMDB) {
        this.parentCMDB = parentCMDB;
    }

    public Set<String> getChildren() {
        return children;
    }

    public void setChildren(Set<String> children) {
        this.children = children;
    }

    public JsonObject getContent() {
        return content;
    }

    public void setContent(JsonObject content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof String)) return false;

        CMDBLayer cmdbLayer = (CMDBLayer) o;

        return new EqualsBuilder()
                .append(isBase(), cmdbLayer.isBase())
                .append(isActive(), cmdbLayer.isActive())
                .append(getName(), cmdbLayer.getName())
                .append(getPath(), cmdbLayer.getPath())
                .append(getFileSystemPath(), cmdbLayer.getFileSystemPath())
                .append(getParentCMDB(), cmdbLayer.getParentCMDB())
                .append(getChildren(), cmdbLayer.getChildren())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(getPath())
                .append(getFileSystemPath())
                .append(isBase())
                .append(isActive())
                .append(getParentCMDB())
                .append(getChildren())
                .toHashCode();
    }
}
