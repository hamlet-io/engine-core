package io.codeontap.freemarkerwrapper.files.methods.tree;

import freemarker.core.Environment;
import freemarker.template.*;
import io.codeontap.freemarkerwrapper.RunFreeMarkerException;
import io.codeontap.freemarkerwrapper.files.adapters.JsonStringAdapter;
import io.codeontap.freemarkerwrapper.files.meta.LayerMeta;
import io.codeontap.freemarkerwrapper.files.processors.LayerProcessor;

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
        Object startingPathObj = args.get(0);
        String startingPath = null;
        if (startingPathObj instanceof SimpleScalar){
            startingPath = startingPathObj.toString();
        } else if (startingPathObj instanceof JsonStringAdapter){
            startingPath = ((JsonStringAdapter) startingPathObj).getAsString();
        }

        options = (TemplateHashModelEx)args.get(1);
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

        while (iterator.hasNext()){
            TemplateModel key = iterator.next();
            if ("Regex".equalsIgnoreCase(key.toString())){
                Object regex = options.get(key.toString());
                if(regex instanceof TemplateSequenceModel)
                    regexSequence = (TemplateSequenceModel)regex;
                else if(regex instanceof SimpleScalar)
                    regexScalar = (SimpleScalar)regex;
            } else if ("IgnoreDotDirectories".equalsIgnoreCase(key.toString())){
                ignoreDotDirectories = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            } else if ("IgnoreDotFiles".equalsIgnoreCase(key.toString())){
                ignoreDotFiles = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            } else if ("AddStartingWildcard".equalsIgnoreCase(key.toString())){
                addStartingWildcard = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            } else if ("AddEndingWildcard".equalsIgnoreCase(key.toString())){
                addEndingWildcard = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            } else if ("StopAfterFirstMatch".equalsIgnoreCase(key.toString())){
                stopAfterFirstMatch = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            } else if ("IgnoreSubtreeAfterMatch".equalsIgnoreCase(key.toString())){
                ignoreSubtreeAfterMatch = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            } else if ("MinDepth".equalsIgnoreCase(key.toString())){
                minDepth = ((TemplateNumberModel) options.get(key.toString())).getAsNumber();
            } else if ("MaxDepth".equalsIgnoreCase(key.toString())){
                maxDepth = ((TemplateNumberModel) options.get(key.toString())).getAsNumber();
            } else if (meta.getIncludeInformationOptionName().equalsIgnoreCase(key.toString())) {
                includeInformation = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            } else if ("CaseSensitive".equalsIgnoreCase(key.toString())){
                caseSensitive = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            }
        }
        List<String> regexList = new ArrayList<>();
        if(regexSequence == null || regexSequence.size() == 0){
            if(regexScalar == null) {
                regexList.add("^.*$");
            } else {
                regexList.add(regexScalar.getAsString());
            }
        } else {
            for (int i=0; i < regexSequence.size();i++){
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
        if (minDepth!=null){
            meta.setMinDepth(minDepth.intValue());
        }
        if (maxDepth!=null){
            meta.setMaxDepth(maxDepth.intValue());
        }
        meta.setCaseSensitive(caseSensitive);
    }

    public TemplateModel process() {
        Set<JsonObject> result = null;
        try {
            result = layerProcessor.getLayerTree(meta);
        } catch (RunFreeMarkerException e) {
            e.printStackTrace();
        }
        return new SimpleSequence(result, Environment.getCurrentEnvironment().getConfiguration().getObjectWrapper());
    }
}
