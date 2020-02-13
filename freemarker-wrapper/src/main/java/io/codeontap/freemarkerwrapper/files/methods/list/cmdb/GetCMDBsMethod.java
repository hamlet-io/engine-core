package io.codeontap.freemarkerwrapper.files.methods.list.cmdb;

import freemarker.core.Environment;
import freemarker.template.*;
import io.codeontap.freemarkerwrapper.files.meta.cmdb.CMDBMeta;
import io.codeontap.freemarkerwrapper.files.methods.list.GetLayerListMethod;
import io.codeontap.freemarkerwrapper.files.processors.cmdb.CMDBProcessor;

import java.util.List;
import java.util.Map;

public class GetCMDBsMethod extends GetLayerListMethod implements TemplateMethodModelEx {

    public TemplateModel exec(List args) throws TemplateModelException {
        meta = new CMDBMeta();
        List<String> lookupDirs = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("lookupDirs")).getWrappedObject();
        List<String> CMDBNames = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("CMDBNames")).getWrappedObject();
        Map<String, String> cmdbPathMapping = (Map<String, String>) ((DefaultMapAdapter) Environment.getCurrentEnvironment().getGlobalVariable("cmdbPathMappings")).getWrappedObject();
        String baseCMDB = ((SimpleScalar) Environment.getCurrentEnvironment().getGlobalVariable("baseCMDB")).getAsString();
        TemplateHashModelEx options = (TemplateHashModelEx)args.get(0);
        TemplateModelIterator iterator = options.keys().iterator();
        boolean useCMDBPrefix = Boolean.FALSE;
        boolean activeOnly = Boolean.FALSE;
        while (iterator.hasNext()){
            TemplateModel key = iterator.next();
            if ("UseCMDBPrefix".equalsIgnoreCase(key.toString())){
                useCMDBPrefix = ((TemplateBooleanModel) options.get("UseCMDBPrefix")).getAsBoolean();
            }
            else if ("ActiveOnly".equalsIgnoreCase(key.toString())){
                activeOnly = ((TemplateBooleanModel) options.get("ActiveOnly")).getAsBoolean();
            }
        }
        CMDBMeta cmdbMeta = (CMDBMeta)meta;
        cmdbMeta.setLookupDirs(lookupDirs);
        cmdbMeta.setCMDBs(cmdbPathMapping);
        cmdbMeta.setCMDBNamesList(CMDBNames);
        cmdbMeta.setBaseCMDB(baseCMDB);
        cmdbMeta.setUseCMDBPrefix(useCMDBPrefix);
        cmdbMeta.setActiveOnly(activeOnly);

        layerProcessor = new CMDBProcessor();
        return super.process();
    }
}
