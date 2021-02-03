package io.hamlet.freemarkerwrapper.files.methods.rm.cmdb;

import freemarker.core.Environment;
import freemarker.template.*;
import io.hamlet.freemarkerwrapper.files.adapters.JsonStringAdapter;
import io.hamlet.freemarkerwrapper.files.meta.cmdb.CMDBMeta;
import io.hamlet.freemarkerwrapper.files.methods.rm.RmLayerMethod;
import io.hamlet.freemarkerwrapper.files.processors.cmdb.CMDBProcessor;
import io.hamlet.freemarkerwrapper.utils.FreemarkerUtil;

import java.util.List;
import java.util.Map;

public class RmCMDBMethod extends RmLayerMethod implements TemplateMethodModelEx {

    public TemplateModel exec(List args) throws TemplateModelException {
        if (args.size() != 2) {
            throw new TemplateModelException("Wrong arguments");
        }
        Object pathObj = args.get(0);
        String path = FreemarkerUtil.getOptionStringValue(pathObj);

        meta = new CMDBMeta();
        List<String> lookupDirs = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("lookupDirs")).getWrappedObject();
        List<String> CMDBNames = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("CMDBNames")).getWrappedObject();
        Map<String, String> cmdbPathMapping = (Map<String, String>) ((DefaultMapAdapter) Environment.getCurrentEnvironment().getGlobalVariable("cmdbPathMappings")).getWrappedObject();
        String baseCMDB = ((SimpleScalar) Environment.getCurrentEnvironment().getGlobalVariable("baseCMDB")).getAsString();
        TemplateHashModelEx options = (TemplateHashModelEx)args.get(1);
        TemplateModelIterator iterator = options.keys().iterator();
        boolean recurse = Boolean.FALSE;
        boolean force = Boolean.FALSE;
        boolean sync = Boolean.TRUE;
        while (iterator.hasNext()){
            TemplateModel key = iterator.next();
            if ("Recurse".equalsIgnoreCase(key.toString())){
                recurse = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            }
            else if ("Force".equalsIgnoreCase(key.toString())){
                force = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            }
            else if ("Synch".equalsIgnoreCase(key.toString())){
                sync = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            }
        }
        CMDBMeta cmdbMeta = (CMDBMeta)meta;
        cmdbMeta.setToPath(path);
        cmdbMeta.setStartingPath("/");
        cmdbMeta.setLookupDirs(lookupDirs);
        cmdbMeta.setCMDBs(cmdbPathMapping);
        cmdbMeta.setCMDBNamesList(CMDBNames);
        cmdbMeta.setBaseCMDB(baseCMDB);
        cmdbMeta.setRecurse(recurse);
        cmdbMeta.setForce(force);
        cmdbMeta.setSync(sync);

        layerProcessor = new CMDBProcessor();
        return super.process();
    }
}
