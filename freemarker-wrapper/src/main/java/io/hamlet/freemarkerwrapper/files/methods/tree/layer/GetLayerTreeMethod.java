package io.hamlet.freemarkerwrapper.files.methods.tree.layer;

import freemarker.core.Environment;
import freemarker.template.*;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.layer.LayerMeta;
import io.hamlet.freemarkerwrapper.files.processors.layer.LayerProcessor;
import io.hamlet.freemarkerwrapper.utils.FreemarkerUtil;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class GetLayerTreeMethod {

    protected LayerMeta meta;
    protected TemplateHashModelEx options;
    protected LayerProcessor layerProcessor;

    public void parseArguments(List args) throws TemplateModelException {
        if (args.size() != 2) {
            throw new TemplateModelException("Wrong arguments");
        }
        String startingPath = FreemarkerUtil.getOptionStringValue(args.get(0));

        options = (TemplateHashModelEx) args.get(1);
        TemplateModelIterator iterator = options.keys().iterator();
        TemplateSequenceModel regexSequence = null;
        SimpleScalar regexScalar = null;
        boolean ignoreDotDirectories = Boolean.TRUE;
        boolean ignoreDotFiles = Boolean.TRUE;
        boolean addStartingWildcard = Boolean.TRUE;
        boolean addEndingWildcard = Boolean.TRUE;
        boolean stopAfterFirstMatch = Boolean.FALSE;
        boolean ignoreSubtreeAfterMatch = Boolean.FALSE;
        Number minDepth = null;
        Number maxDepth = null;
        boolean includeInformation = Boolean.FALSE;
        boolean caseSensitive = Boolean.FALSE;
        String filenameGlob = "*";
        boolean ignoreDirectories = Boolean.FALSE;
        boolean ignoreFiles = Boolean.FALSE;

        while (iterator.hasNext()) {
            TemplateModel keyModel = iterator.next();
            String key = keyModel.toString();
            Object keyObj = options.get(key);
            if ("Regex".equalsIgnoreCase(key)) {
                if (keyObj instanceof TemplateSequenceModel)
                    regexSequence = (TemplateSequenceModel) keyObj;
                else if (keyObj instanceof SimpleScalar)
                    regexScalar = (SimpleScalar) keyObj;
            } else if ("IgnoreDotDirectories".equalsIgnoreCase(key)) {
                ignoreDotDirectories = FreemarkerUtil.getOptionBooleanValue(keyObj);
            } else if ("IgnoreDotFiles".equalsIgnoreCase(key)) {
                ignoreDotFiles = FreemarkerUtil.getOptionBooleanValue(keyObj);
            } else if ("AddStartingWildcard".equalsIgnoreCase(key)) {
                addStartingWildcard = FreemarkerUtil.getOptionBooleanValue(keyObj);
            } else if ("AddEndingWildcard".equalsIgnoreCase(key)) {
                addEndingWildcard = FreemarkerUtil.getOptionBooleanValue(keyObj);
            } else if ("StopAfterFirstMatch".equalsIgnoreCase(key)) {
                stopAfterFirstMatch = FreemarkerUtil.getOptionBooleanValue(keyObj);
            } else if ("IgnoreSubtreeAfterMatch".equalsIgnoreCase(key)) {
                ignoreSubtreeAfterMatch = FreemarkerUtil.getOptionBooleanValue(keyObj);
            } else if ("MinDepth".equalsIgnoreCase(key)) {
                minDepth = FreemarkerUtil.getOptionNumberValue(keyObj);
            } else if ("MaxDepth".equalsIgnoreCase(key)) {
                maxDepth = FreemarkerUtil.getOptionNumberValue(keyObj);
            } else if (meta.getIncludeInformationOptionName().equalsIgnoreCase(key)) {
                includeInformation = FreemarkerUtil.getOptionBooleanValue(keyObj);
            } else if ("CaseSensitive".equalsIgnoreCase(key)) {
                caseSensitive = FreemarkerUtil.getOptionBooleanValue(keyObj);
            } else if ("FilenameGlob".equalsIgnoreCase(key)) {
                filenameGlob = FreemarkerUtil.getOptionStringValue(keyObj);
            } else if ("IgnoreDirectories".equalsIgnoreCase(key)) {
                ignoreDirectories = FreemarkerUtil.getOptionBooleanValue(keyObj);
            } else if ("IgnoreFiles".equalsIgnoreCase(key)) {
                ignoreFiles = FreemarkerUtil.getOptionBooleanValue(keyObj);
            }
        }
        List<String> regexList = new ArrayList<>();
        if (regexSequence == null || regexSequence.size() == 0) {
            if (regexScalar == null) {
                regexList.add("^.*$");
            } else {
                regexList.add(regexScalar.getAsString());
            }
        } else {
            for (int i = 0; i < regexSequence.size(); i++) {
                regexList.add(regexSequence.get(i).toString());
            }
        }
        meta.setStartingPath(startingPath);

        meta.setRegexList(regexList);
        meta.setIgnoreDotDirectories(ignoreDotDirectories);
        meta.setIgnoreDotFiles(ignoreDotFiles);
        meta.setIncludeInformation(includeInformation);
        meta.setAddStartingWildcard(addStartingWildcard);
        meta.setAddEndingWildcard(addEndingWildcard);
        meta.setStopAfterFirstMatch(stopAfterFirstMatch);
        meta.setIgnoreSubtreeAfterMatch(ignoreSubtreeAfterMatch);
        if (minDepth != null) {
            meta.setMinDepth(minDepth.intValue());
        }
        if (maxDepth != null) {
            meta.setMaxDepth(maxDepth.intValue());
        }
        meta.setCaseSensitive(caseSensitive);
        meta.setFilenameGlob(filenameGlob);
        meta.setIgnoreDirectories(ignoreDirectories);
        meta.setIgnoreFiles(ignoreFiles);
    }

    public TemplateModel process() {
        layerProcessor.setConfiguration(Environment.getCurrentEnvironment().getConfiguration());
        Set<JsonObject> result = null;
        try {
            result = layerProcessor.getLayerTree(meta);
        } catch (RunFreeMarkerException e) {
            e.printStackTrace();
        }
        return new SimpleSequence(result, Environment.getCurrentEnvironment().getConfiguration().getObjectWrapper());
    }
}
