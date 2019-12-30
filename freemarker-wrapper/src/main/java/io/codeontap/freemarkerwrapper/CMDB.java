package io.codeontap.freemarkerwrapper;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

public class CMDB {
    String name;
    String path;
    String fileSystemPath;
    boolean base = false;
    boolean active = true;
    String parentCMDB;
    Set<String> children = new HashSet<>();

    public CMDB(String name, String fileSystemPath) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof String)) return false;

        CMDB cmdb = (CMDB) o;

        return new EqualsBuilder()
                .append(isBase(), cmdb.isBase())
                .append(isActive(), cmdb.isActive())
                .append(getName(), cmdb.getName())
                .append(getPath(), cmdb.getPath())
                .append(getFileSystemPath(), cmdb.getFileSystemPath())
                .append(getParentCMDB(), cmdb.getParentCMDB())
                .append(getChildren(), cmdb.getChildren())
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
