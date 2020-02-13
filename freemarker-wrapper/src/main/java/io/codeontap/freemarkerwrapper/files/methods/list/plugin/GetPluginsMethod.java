package io.codeontap.freemarkerwrapper.files.methods.list.plugin;

import freemarker.core.Environment;
import freemarker.template.*;
import io.codeontap.freemarkerwrapper.files.meta.plugin.PluginMeta;
import io.codeontap.freemarkerwrapper.files.methods.list.GetLayerListMethod;
import io.codeontap.freemarkerwrapper.files.methods.tree.GetLayerTreeMethod;
import io.codeontap.freemarkerwrapper.files.processors.plugin.PluginProcessor;
import io.codeontap.freemarkerwrapper.RunFreeMarkerException;

import javax.json.JsonArray;
import java.util.ArrayList;
import java.util.List;

public class GetPluginsMethod extends GetLayerListMethod implements TemplateMethodModelEx {

    public TemplateModel exec(List args) throws TemplateModelException {
        meta = new PluginMeta();
        List<String> pluginLayers = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("pluginLayers")).getWrappedObject();
        ((PluginMeta)meta).setLayers(pluginLayers);
        layerProcessor = new PluginProcessor();
        return super.process();
    }
}
