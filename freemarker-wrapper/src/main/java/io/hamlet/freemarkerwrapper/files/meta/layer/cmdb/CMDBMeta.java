package io.hamlet.freemarkerwrapper.files.meta.layer.cmdb;

import io.hamlet.freemarkerwrapper.files.meta.layer.LayerMeta;

import java.util.List;
import java.util.Map;


public class CMDBMeta extends LayerMeta {
    private List<String> lookupDirs;
    private Map<String, String> CMDBs;
    private List<String> CMDBNamesList;
    private String baseCMDB;
    private boolean useCMDBPrefix;
    private boolean activeOnly;
    private boolean append;
    private boolean recurse;
    private String fromPath;

    private String format;
    private String formatting;
    private Integer indent;
    private boolean preserve;
    private boolean force;
    private boolean parents;

    private String toPath;

    public List<String> getLookupDirs() {
        return lookupDirs;
    }

    public void setLookupDirs(List<String> lookupDirs) {
        this.lookupDirs = lookupDirs;
    }

    public Map<String, String> getCMDBs() {
        return CMDBs;
    }

    public void setCMDBs(Map<String, String> CMDBs) {
        this.CMDBs = CMDBs;
    }

    public String getBaseCMDB() {
        return baseCMDB;
    }

    public void setBaseCMDB(String baseCMDB) {
        this.baseCMDB = baseCMDB;
    }

    public boolean isUseCMDBPrefix() {
        return useCMDBPrefix;
    }

    public void setUseCMDBPrefix(boolean useCMDBPrefix) {
        this.useCMDBPrefix = useCMDBPrefix;
    }

    public List<String> getCMDBNamesList() {
        return CMDBNamesList;
    }

    public void setCMDBNamesList(List<String> CMDBNamesList) {
        this.CMDBNamesList = CMDBNamesList;
    }

    public boolean isActiveOnly() {
        return activeOnly;
    }

    public void setActiveOnly(boolean activeOnly) {
        this.activeOnly = activeOnly;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormatting() {
        return formatting;
    }

    public void setFormatting(String formatting) {
        this.formatting = formatting;
    }

    public Integer getIndent() {
        return indent;
    }

    public void setIndent(Integer indent) {
        this.indent = indent;
    }

    public String getToPath() {
        return toPath;
    }

    public void setToPath(String toPath) {
        this.toPath = toPath;
    }

    public boolean isRecurse() {
        return recurse;
    }

    public void setRecurse(boolean recurse) {
        this.recurse = recurse;
    }

    public boolean isPreserve() {
        return preserve;
    }

    public void setPreserve(boolean preserve) {
        this.preserve = preserve;
    }


    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public boolean isParents() {
        return parents;
    }

    public void setParents(boolean parents) {
        this.parents = parents;
    }

    public String getFromPath() {
        return fromPath;
    }

    public void setFromPath(String fromPath) {
        this.fromPath = fromPath;
    }


    @Override
    public String getIncludeInformationOptionName() {
        return "IncludeCMDBInformation";
    }
}
