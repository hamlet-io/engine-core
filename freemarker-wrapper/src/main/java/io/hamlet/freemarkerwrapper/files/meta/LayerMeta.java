package io.hamlet.freemarkerwrapper.files.meta;

import java.util.List;
import java.util.Set;

public abstract class LayerMeta implements Cloneable{
    private Set<String> layersNames;
    private String startingPath;
    private String fromPath;
    private String toPath;
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

    private boolean parents;
    private boolean sync;
    private boolean recurse;
    private boolean preserve;
    private boolean force;
    private boolean append;
    private boolean ignoreFiles;
    private boolean ignoreDirectories;

    private String format;
    private String formatting;
    private Integer indent;

    private Object content;

    public Object clone() throws
            CloneNotSupportedException
    {
        return super.clone();
    }

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


    public boolean isParents() {
        return parents;
    }

    public void setParents(boolean parents) {
        this.parents = parents;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
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

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public String getFromPath() {
        return fromPath;
    }

    public void setFromPath(String fromPath) {
        this.fromPath = fromPath;
    }

    public String getToPath() {
        return toPath;
    }

    public void setToPath(String toPath) {
        this.toPath = toPath;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public boolean isIgnoreFiles() {
        return ignoreFiles;
    }

    public void setIgnoreFiles(boolean ignoreFiles) {
        this.ignoreFiles = ignoreFiles;
    }

    public boolean isIgnoreDirectories() {
        return ignoreDirectories;
    }

    public void setIgnoreDirectories(boolean ignoreDirectories) {
        this.ignoreDirectories = ignoreDirectories;
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
}
