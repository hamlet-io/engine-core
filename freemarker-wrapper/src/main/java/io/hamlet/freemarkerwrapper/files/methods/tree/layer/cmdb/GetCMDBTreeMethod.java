package io.hamlet.freemarkerwrapper.files.methods.tree.layer.cmdb;

import freemarker.core.Environment;
import freemarker.template.*;
import io.hamlet.freemarkerwrapper.files.meta.layer.cmdb.CMDBMeta;
import io.hamlet.freemarkerwrapper.files.methods.tree.layer.GetLayerTreeMethod;
import io.hamlet.freemarkerwrapper.files.processors.layer.cmdb.CMDBProcessor;

import java.util.List;
import java.util.Map;

public class GetCMDBTreeMethod extends GetLayerTreeMethod implements TemplateMethodModelEx {

    public static String METHOD_NAME = "getCMDBTree";

    public GetCMDBTreeMethod() {
        super(2, METHOD_NAME);
    }

    @Override
    protected void init() {
        meta = new CMDBMeta();
        processor = new CMDBProcessor();
    }

    @Override
    public void parseArguments(List args) throws TemplateModelException {
        super.parseArguments(args);

        List<String> lookupDirs = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("lookupDirs")).getWrappedObject();
        List<String> CMDBNames = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("CMDBNames")).getWrappedObject();
        Map<String, String> cmdbPathMapping = (Map<String, String>) ((DefaultMapAdapter) Environment.getCurrentEnvironment().getGlobalVariable("cmdbPathMappings")).getWrappedObject();
        String baseCMDB = ((SimpleScalar) Environment.getCurrentEnvironment().getGlobalVariable("baseCMDB")).getAsString();

        //TODO: check if we need to support it and implement UseCMDBPrefix option
        boolean useCMDBPrefix = Boolean.FALSE;
        CMDBMeta cmdbMeta = ((CMDBMeta) meta);
        cmdbMeta.setUseCMDBPrefix(useCMDBPrefix);
        cmdbMeta.setLookupDirs(lookupDirs);
        cmdbMeta.setCMDBs(cmdbPathMapping);
        cmdbMeta.setCMDBNamesList(CMDBNames);
        cmdbMeta.setBaseCMDB(baseCMDB);

    }
}
