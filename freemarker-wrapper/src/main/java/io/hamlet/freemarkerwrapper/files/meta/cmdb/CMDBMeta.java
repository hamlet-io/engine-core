package io.hamlet.freemarkerwrapper.files.meta.cmdb;

import io.hamlet.freemarkerwrapper.files.meta.LayerMeta;

import java.util.List;
import java.util.Map;


public class CMDBMeta extends LayerMeta {
    private List<String> lookupDirs;
    private Map<String, String> CMDBs;
    private List<String> CMDBNamesList;
    private String baseCMDB;
    private boolean useCMDBPrefix;
    private boolean activeOnly;

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

    @Override
    public String getIncludeInformationOptionName() {
        return "IncludeCMDBInformation";
    }
}
