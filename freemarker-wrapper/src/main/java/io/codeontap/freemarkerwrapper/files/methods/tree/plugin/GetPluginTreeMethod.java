package io.codeontap.freemarkerwrapper.files.methods.tree.plugin;

import freemarker.core.Environment;
import freemarker.template.*;
import io.codeontap.freemarkerwrapper.files.adapters.JsonStringAdapter;
import io.codeontap.freemarkerwrapper.files.meta.plugin.PluginMeta;
import io.codeontap.freemarkerwrapper.files.processors.plugin.PluginProcessor;
import io.codeontap.freemarkerwrapper.RunFreeMarkerException;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GetPluginTreeMethod implements TemplateMethodModelEx {

    public TemplateModel exec(List args) throws TemplateModelException {
        if (args.size() != 2) {
            throw new TemplateModelException("Wrong arguments");
        }

        List<String> pluginLayers = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("pluginLayers")).getWrappedObject();
        Object startingPathObj = args.get(0);
        String startingPath = null;
        if (startingPathObj instanceof SimpleScalar){
            startingPath = startingPathObj.toString();
        }else if (startingPathObj instanceof JsonStringAdapter){
            startingPath = ((JsonStringAdapter) startingPathObj).getAsString();
        }
        TemplateHashModelEx options = (TemplateHashModelEx)args.get(1);
        TemplateModelIterator iterator = options.keys().iterator();
        TemplateSequenceModel regexSequence = null;
        SimpleScalar regexScalar = null;
        boolean ignoreDotDirectories = Boolean.TRUE;
        boolean ignoreDotFiles = Boolean.TRUE;
        boolean includePluginInformation = Boolean.FALSE;
        boolean addStartingWildcard = Boolean.TRUE;
        boolean addEndingWildcard = Boolean.TRUE;

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
            } else if ("IncludePluginInformation".equalsIgnoreCase(key.toString())){
                includePluginInformation = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            } else if ("AddStartingWildcard".equalsIgnoreCase(key.toString())){
                addStartingWildcard = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            } else if ("AddEndingWildcard".equalsIgnoreCase(key.toString())){
                addEndingWildcard = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
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

        PluginProcessor pluginProcessor = new PluginProcessor();
        Set<JsonObject> result = null;
        try {
            PluginMeta meta = new PluginMeta();
            meta.setLayers(pluginLayers);
            meta.setStartingPath(startingPath);
            meta.setRegexList(regexList);
            meta.setIgnoreDotDirectories(ignoreDotDirectories);
            meta.setIgnoreDotFiles(ignoreDotFiles);
            meta.setIncludeInformation(includePluginInformation);
            meta.setAddStartingWildcard(addStartingWildcard);
            meta.setAddEndingWildcard(addEndingWildcard);
            result = pluginProcessor.getLayerTree(meta);
        } catch (RunFreeMarkerException e) {
            e.printStackTrace();
        }

        return new SimpleSequence(result, Environment.getCurrentEnvironment().getConfiguration().getObjectWrapper());
    }
}
