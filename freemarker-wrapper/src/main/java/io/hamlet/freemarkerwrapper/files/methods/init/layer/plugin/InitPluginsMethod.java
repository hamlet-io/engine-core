package io.hamlet.freemarkerwrapper.files.methods.init.layer.plugin;

import freemarker.core.Environment;
import freemarker.template.DefaultListAdapter;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.hamlet.freemarkerwrapper.files.meta.layer.plugin.PluginMeta;
import io.hamlet.freemarkerwrapper.files.methods.init.layer.InitLayersMethod;
import io.hamlet.freemarkerwrapper.files.processors.layer.plugin.PluginProcessor;

import java.util.List;

public class InitPluginsMethod extends InitLayersMethod implements TemplateMethodModelEx {

    public TemplateModel exec(List args) throws TemplateModelException {
        meta = new PluginMeta();
        List<String> pluginLayers = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("pluginLayers")).getWrappedObject();
        ((PluginMeta) meta).setLayers(pluginLayers);
        layerProcessor = new PluginProcessor();
        return super.process();
    }
}
