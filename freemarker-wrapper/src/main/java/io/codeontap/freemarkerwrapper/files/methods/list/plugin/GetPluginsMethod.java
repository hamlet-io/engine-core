package io.codeontap.freemarkerwrapper.files.methods.list.plugin;

import freemarker.core.Environment;
import freemarker.template.*;
import io.codeontap.freemarkerwrapper.files.meta.plugin.PluginMeta;
import io.codeontap.freemarkerwrapper.files.processors.plugin.PluginProcessor;
import io.codeontap.freemarkerwrapper.RunFreeMarkerException;

import javax.json.JsonArray;
import java.util.ArrayList;
import java.util.List;

public class GetPluginsMethod implements TemplateMethodModelEx {

    public TemplateModel exec(List args) throws TemplateModelException {

        List<String> pluginLayers = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("pluginLayers")).getWrappedObject();
        TemplateHashModelEx options = (TemplateHashModelEx)args.get(0);
        TemplateModelIterator iterator = options.keys().iterator();
        TemplateSequenceModel regexSequence = null;
        while (iterator.hasNext()){

        }
        List<String> regexList = new ArrayList<>();
        if(regexSequence == null || regexSequence.size() == 0){
            regexList.add("*.*");
        } else {
            for (int i=0; i < regexSequence.size();i++){
                regexList.add(regexSequence.get(i).toString());
            }
        }

        PluginProcessor pluginProcessor = new PluginProcessor();
        JsonArray result = null;
        try {
            PluginMeta meta = new PluginMeta();
            meta.setLayers(pluginLayers);
            result = pluginProcessor.getLayers(meta);
        } catch (RunFreeMarkerException e) {
            e.printStackTrace();
        }

        return new SimpleSequence(result, Environment.getCurrentEnvironment().getConfiguration().getObjectWrapper());
    }
}
