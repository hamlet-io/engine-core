package io.codeontap.freemarkerwrapper.files.methods.cp.cmdb;

import freemarker.core.Environment;
import freemarker.template.*;
import io.codeontap.freemarkerwrapper.files.adapters.JsonStringAdapter;
import io.codeontap.freemarkerwrapper.files.meta.cmdb.CMDBMeta;
import io.codeontap.freemarkerwrapper.files.methods.cp.CpLayerMethod;
import io.codeontap.freemarkerwrapper.files.methods.mkdir.MkdirLayerMethod;
import io.codeontap.freemarkerwrapper.files.processors.cmdb.CMDBProcessor;

import java.util.List;
import java.util.Map;

public class CpCMDBMethod extends CpLayerMethod implements TemplateMethodModelEx {

    public TemplateModel exec(List args) throws TemplateModelException {
        if (args.size() != 3) {
            throw new TemplateModelException("Wrong arguments");
        }
        Object copyFromPathObj = args.get(0);
        String copyFromPath = null;
        if (copyFromPathObj instanceof SimpleScalar){
            copyFromPath = copyFromPathObj.toString();
        } else if (copyFromPathObj instanceof JsonStringAdapter){
            copyFromPath = ((JsonStringAdapter) copyFromPathObj).getAsString();
        }

        Object copyToPathObj = args.get(1);
        String copyToPath = null;
        if (copyToPathObj instanceof SimpleScalar){
            copyToPath = copyToPathObj.toString();
        } else if (copyToPathObj instanceof JsonStringAdapter){
            copyToPath = ((JsonStringAdapter) copyToPathObj).getAsString();
        }

        meta = new CMDBMeta();
        List<String> lookupDirs = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("lookupDirs")).getWrappedObject();
        List<String> CMDBNames = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("CMDBNames")).getWrappedObject();
        Map<String, String> cmdbPathMapping = (Map<String, String>) ((DefaultMapAdapter) Environment.getCurrentEnvironment().getGlobalVariable("cmdbPathMappings")).getWrappedObject();
        String baseCMDB = ((SimpleScalar) Environment.getCurrentEnvironment().getGlobalVariable("baseCMDB")).getAsString();
        TemplateHashModelEx options = (TemplateHashModelEx)args.get(2);
        TemplateModelIterator iterator = options.keys().iterator();
        boolean recurse = Boolean.FALSE;
        boolean preserve = Boolean.FALSE;
        boolean sync = Boolean.TRUE;
        while (iterator.hasNext()){
            TemplateModel key = iterator.next();
            if ("Recurse".equalsIgnoreCase(key.toString())){
                recurse = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            }
            else if ("Preserve".equalsIgnoreCase(key.toString())){
                preserve = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            }
            else if ("Synch".equalsIgnoreCase(key.toString())){
                sync = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            }
        }
        CMDBMeta cmdbMeta = (CMDBMeta)meta;
        cmdbMeta.setFromPath(copyFromPath);
        cmdbMeta.setToPath(copyToPath);
        cmdbMeta.setLookupDirs(lookupDirs);
        cmdbMeta.setCMDBs(cmdbPathMapping);
        cmdbMeta.setCMDBNamesList(CMDBNames);
        cmdbMeta.setBaseCMDB(baseCMDB);
        cmdbMeta.setRecurse(recurse);
        cmdbMeta.setPreserve(preserve);
        cmdbMeta.setSync(sync);

        layerProcessor = new CMDBProcessor();
        return super.process();
    }
}
