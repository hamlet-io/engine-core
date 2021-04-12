package io.hamlet.freemarkerwrapper.files.methods.list.layer.plugin;

import freemarker.core.Environment;
import freemarker.template.DefaultListAdapter;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.hamlet.freemarkerwrapper.files.meta.layer.plugin.PluginMeta;
import io.hamlet.freemarkerwrapper.files.methods.list.layer.GetLayerListMethod;
import io.hamlet.freemarkerwrapper.files.processors.layer.plugin.PluginProcessor;

import java.util.List;

public class GetPluginLayersMethod extends GetLayerListMethod implements TemplateMethodModelEx {
    public static String METHOD_NAME = "getPluginLayers";
    public GetPluginLayersMethod() {
        super(1, METHOD_NAME);
    }

    @Override
    protected void init() {
        meta = new PluginMeta();
        processor = new PluginProcessor();
    }

    @Override
    protected void parseArguments(List args) throws TemplateModelException {
        List<String> pluginLayers = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("pluginLayers")).getWrappedObject();
        ((PluginMeta) meta).setLayers(pluginLayers);
    }
}
