package io.hamlet.freemarkerwrapper.files.methods.cp.layer.cmdb;

import freemarker.core.Environment;
import freemarker.template.*;
import io.hamlet.freemarkerwrapper.NumberOfArgumentsException;
import io.hamlet.freemarkerwrapper.files.meta.layer.cmdb.CMDBMeta;
import io.hamlet.freemarkerwrapper.files.methods.cp.layer.CpLayerMethod;
import io.hamlet.freemarkerwrapper.files.processors.layer.cmdb.CMDBProcessor;
import io.hamlet.freemarkerwrapper.utils.FreemarkerUtil;

import java.util.List;
import java.util.Map;

public class CpCMDBMethod extends CpLayerMethod {

    public static String METHOD_NAME = "cpCMDB";

    public CpCMDBMethod(){
        super(3, METHOD_NAME);
    }

    @Override
    protected void init() {
        meta = new CMDBMeta();
        processor = new CMDBProcessor();
    }

    @Override
    protected void parseArguments(List args) throws TemplateModelException {
        String copyFromPath = FreemarkerUtil.getOptionStringValue(args.get(0));
        String copyToPath = FreemarkerUtil.getOptionStringValue(args.get(1));

        List<String> lookupDirs = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("lookupDirs")).getWrappedObject();
        List<String> CMDBNames = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("CMDBNames")).getWrappedObject();
        Map<String, String> cmdbPathMapping = (Map<String, String>) ((DefaultMapAdapter) Environment.getCurrentEnvironment().getGlobalVariable("cmdbPathMappings")).getWrappedObject();
        String baseCMDB = ((SimpleScalar) Environment.getCurrentEnvironment().getGlobalVariable("baseCMDB")).getAsString();
        TemplateHashModelEx options = (TemplateHashModelEx) args.get(2);
        TemplateModelIterator iterator = options.keys().iterator();
        boolean recurse = Boolean.FALSE;
        boolean preserve = Boolean.FALSE;
        boolean sync = Boolean.TRUE;
        while (iterator.hasNext()) {
            TemplateModel keyModel = iterator.next();
            String key = keyModel.toString();
            Object keyObj = options.get(key);
            if ("Recurse".equalsIgnoreCase(key)) {
                recurse = FreemarkerUtil.getOptionBooleanValue(keyObj);
            } else if ("Preserve".equalsIgnoreCase(key)) {
                preserve = FreemarkerUtil.getOptionBooleanValue(keyObj);
            } else if ("Synch".equalsIgnoreCase(key)) {
                sync = FreemarkerUtil.getOptionBooleanValue(keyObj);
            }
        }
        CMDBMeta cmdbMeta = (CMDBMeta) meta;
        cmdbMeta.setFromPath(copyFromPath);
        cmdbMeta.setToPath(copyToPath);
        cmdbMeta.setLookupDirs(lookupDirs);
        cmdbMeta.setCMDBs(cmdbPathMapping);
        cmdbMeta.setCMDBNamesList(CMDBNames);
        cmdbMeta.setBaseCMDB(baseCMDB);
        cmdbMeta.setRecurse(recurse);
        cmdbMeta.setPreserve(preserve);
        cmdbMeta.setSync(sync);
    }
}
