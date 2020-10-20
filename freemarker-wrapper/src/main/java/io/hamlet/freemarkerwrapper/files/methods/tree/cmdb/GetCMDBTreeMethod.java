package io.hamlet.freemarkerwrapper.files.methods.tree.cmdb;

import freemarker.core.Environment;
import freemarker.template.*;
import io.hamlet.freemarkerwrapper.files.meta.cmdb.CMDBMeta;
import io.hamlet.freemarkerwrapper.files.methods.tree.GetLayerTreeMethod;
import io.hamlet.freemarkerwrapper.files.processors.cmdb.CMDBProcessor;

import java.util.*;

public class GetCMDBTreeMethod extends GetLayerTreeMethod implements TemplateMethodModelEx {

    public TemplateModel exec(List args) throws TemplateModelException {
        meta = new CMDBMeta();
        super.parseArguments(args);

        List<String> lookupDirs = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("lookupDirs")).getWrappedObject();
        List<String> CMDBNames = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("CMDBNames")).getWrappedObject();
        Map<String, String> cmdbPathMapping = (Map<String, String>) ((DefaultMapAdapter) Environment.getCurrentEnvironment().getGlobalVariable("cmdbPathMappings")).getWrappedObject();
        String baseCMDB = ((SimpleScalar) Environment.getCurrentEnvironment().getGlobalVariable("baseCMDB")).getAsString();

        //TODO: check if we need to support it and implement UseCMDBPrefix option
        boolean useCMDBPrefix = Boolean.FALSE;
        CMDBMeta cmdbMeta = ((CMDBMeta)meta);
        cmdbMeta.setUseCMDBPrefix(useCMDBPrefix);
        cmdbMeta.setLookupDirs(lookupDirs);
        cmdbMeta.setCMDBs(cmdbPathMapping);
        cmdbMeta.setCMDBNamesList(CMDBNames);
        cmdbMeta.setBaseCMDB(baseCMDB);

        layerProcessor = new CMDBProcessor();
        return super.process();
    }
}
