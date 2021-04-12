package io.hamlet.freemarkerwrapper.files.methods.tree.layer;

import freemarker.core.Environment;
import freemarker.template.*;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.meta.layer.LayerMeta;
import io.hamlet.freemarkerwrapper.files.methods.LayerMethod;
import io.hamlet.freemarkerwrapper.files.methods.WrapperMethod;
import io.hamlet.freemarkerwrapper.files.processors.layer.LayerProcessor;
import io.hamlet.freemarkerwrapper.utils.FreemarkerUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class GetLayerTreeMethod extends LayerMethod {

    public GetLayerTreeMethod(int numberOfArguments, String methodName) {
        super(numberOfArguments, methodName);
    }

    @Override
    public void parseArguments(List args) throws TemplateModelException {
        LayerMeta layerMeta = (LayerMeta) meta;
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
            } else if (layerMeta.getIncludeInformationOptionName().equalsIgnoreCase(key)) {
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
        layerMeta.setStartingPath(startingPath);

        layerMeta.setRegexList(regexList);
        layerMeta.setIgnoreDotDirectories(ignoreDotDirectories);
        layerMeta.setIgnoreDotFiles(ignoreDotFiles);
        layerMeta.setIncludeInformation(includeInformation);
        layerMeta.setAddStartingWildcard(addStartingWildcard);
        layerMeta.setAddEndingWildcard(addEndingWildcard);
        layerMeta.setStopAfterFirstMatch(stopAfterFirstMatch);
        layerMeta.setIgnoreSubtreeAfterMatch(ignoreSubtreeAfterMatch);
        if (minDepth != null) {
            layerMeta.setMinDepth(minDepth.intValue());
        }
        if (maxDepth != null) {
            layerMeta.setMaxDepth(maxDepth.intValue());
        }
        layerMeta.setCaseSensitive(caseSensitive);
        layerMeta.setFilenameGlob(filenameGlob);
        layerMeta.setIgnoreDirectories(ignoreDirectories);
        layerMeta.setIgnoreFiles(ignoreFiles);
    }

    public TemplateModel process() throws TemplateModelException, IOException, CloneNotSupportedException {
        return new SimpleSequence(((LayerProcessor)processor).getLayerTree((LayerMeta) meta), Environment.getCurrentEnvironment().getConfiguration().getObjectWrapper());
    }
}
