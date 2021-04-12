package io.hamlet.freemarkerwrapper.files.methods.tree.layer.plugin;

import freemarker.core.Environment;
import freemarker.template.DefaultListAdapter;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.hamlet.freemarkerwrapper.files.meta.layer.plugin.PluginMeta;
import io.hamlet.freemarkerwrapper.files.methods.tree.layer.GetLayerTreeMethod;
import io.hamlet.freemarkerwrapper.files.processors.layer.plugin.PluginProcessor;

import java.util.List;

public class GetPluginTreeMethod extends GetLayerTreeMethod implements TemplateMethodModelEx {

    public static String METHOD_NAME = "getPluginTree";

    public GetPluginTreeMethod() {
        super(2, METHOD_NAME);
    }

    @Override
    protected void init() {
        meta = new PluginMeta();
        processor = new PluginProcessor();
    }

    @Override
    public void parseArguments(List args) throws TemplateModelException {
        super.parseArguments(args);
        List<String> pluginLayers = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("pluginLayers")).getWrappedObject();
        ((PluginMeta) meta).setLayers(pluginLayers);
    }
}
