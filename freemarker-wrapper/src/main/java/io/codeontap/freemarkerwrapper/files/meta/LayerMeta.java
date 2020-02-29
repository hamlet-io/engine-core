package io.codeontap.freemarkerwrapper.files.meta;

import java.util.List;
import java.util.Set;

public abstract class LayerMeta {
    private Set<String> layersNames;
    private String startingPath;
    private List<String> regexList;

    private boolean ignoreDotDirectories;
    private boolean ignoreDotFiles;
    private boolean includeInformation;
    private boolean addStartingWildcard;
    private boolean addEndingWildcard;
    private boolean stopAfterFirstMatch;
    private boolean ignoreSubtreeAfterMatch;

    private Integer minDepth;
    private Integer maxDepth;

    private boolean caseSensitive;
    private String filenameGlob;

    public String getStartingPath() {
        return startingPath;
    }

    public void setStartingPath(String startingPath) {
        this.startingPath = startingPath;
    }

    public List<String> getRegexList() {
        return regexList;
    }

    public void setRegexList(List<String> regexList) {
        this.regexList = regexList;
    }

    public boolean isIgnoreDotDirectories() {
        return ignoreDotDirectories;
    }

    public void setIgnoreDotDirectories(boolean ignoreDotDirectories) {
        this.ignoreDotDirectories = ignoreDotDirectories;
    }

    public boolean isIgnoreDotFiles() {
        return ignoreDotFiles;
    }

    public void setIgnoreDotFiles(boolean ignoreDotFiles) {
        this.ignoreDotFiles = ignoreDotFiles;
    }

    public boolean isIncludeInformation() {
        return includeInformation;
    }

    public void setIncludeInformation(boolean includeInformation) {
        this.includeInformation = includeInformation;
    }

    public Set<String> getLayersNames() {
        return layersNames;
    }

    public void setLayersNames(Set<String> layersNames) {
        this.layersNames = layersNames;
    }

    public boolean isAddStartingWildcard() {
        return addStartingWildcard;
    }

    public void setAddStartingWildcard(boolean addStartingWildcard) {
        this.addStartingWildcard = addStartingWildcard;
    }

    public boolean isAddEndingWildcard() {
        return addEndingWildcard;
    }

    public void setAddEndingWildcard(boolean addEndingWildcard) {
        this.addEndingWildcard = addEndingWildcard;
    }

    public Integer getMinDepth() {
        return minDepth;
    }

    public void setMinDepth(Integer minDepth) {
        this.minDepth = minDepth;
    }

    public Integer getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
    }

    public boolean isStopAfterFirstMatch() {
        return stopAfterFirstMatch;
    }

    public void setStopAfterFirstMatch(boolean stopAfterFirstMatch) {
        this.stopAfterFirstMatch = stopAfterFirstMatch;
    }

    public boolean isIgnoreSubtreeAfterMatch() {
        return ignoreSubtreeAfterMatch;
    }

    public void setIgnoreSubtreeAfterMatch(boolean ignoreSubtreeAfterMatch) {
        this.ignoreSubtreeAfterMatch = ignoreSubtreeAfterMatch;
    }

    public abstract String getIncludeInformationOptionName();

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public String getFilenameGlob() {
        return filenameGlob;
    }

    public void setFilenameGlob(String filenameGlob) {
        this.filenameGlob = filenameGlob;
    }
}
