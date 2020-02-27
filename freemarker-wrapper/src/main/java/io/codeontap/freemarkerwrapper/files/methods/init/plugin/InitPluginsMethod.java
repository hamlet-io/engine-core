package io.codeontap.freemarkerwrapper.files.methods.init.plugin;

import freemarker.core.Environment;
import freemarker.template.DefaultListAdapter;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.codeontap.freemarkerwrapper.files.meta.plugin.PluginMeta;
import io.codeontap.freemarkerwrapper.files.methods.init.InitLayersMethod;
import io.codeontap.freemarkerwrapper.files.methods.list.GetLayerListMethod;
import io.codeontap.freemarkerwrapper.files.processors.plugin.PluginProcessor;

import java.util.List;

public class InitPluginsMethod extends InitLayersMethod implements TemplateMethodModelEx {

    public TemplateModel exec(List args) throws TemplateModelException {
        meta = new PluginMeta();
        List<String> pluginLayers = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("pluginLayers")).getWrappedObject();
        ((PluginMeta)meta).setLayers(pluginLayers);
        layerProcessor = new PluginProcessor();
        return super.process();
    }
}
