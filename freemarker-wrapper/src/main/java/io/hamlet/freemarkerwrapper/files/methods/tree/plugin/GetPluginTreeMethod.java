package io.hamlet.freemarkerwrapper.files.methods.tree.plugin;

import freemarker.core.Environment;
import freemarker.template.*;
import io.hamlet.freemarkerwrapper.files.meta.plugin.PluginMeta;
import io.hamlet.freemarkerwrapper.files.methods.tree.GetLayerTreeMethod;
import io.hamlet.freemarkerwrapper.files.processors.plugin.PluginProcessor;

import java.util.List;

public class GetPluginTreeMethod extends GetLayerTreeMethod implements TemplateMethodModelEx {


    public TemplateModel exec(List args) throws TemplateModelException {
        meta = new PluginMeta();
        super.parseArguments(args);

        List<String> pluginLayers = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("pluginLayers")).getWrappedObject();
        ((PluginMeta)meta).setLayers(pluginLayers);

        layerProcessor = new PluginProcessor();
        return super.process();
    }
}
