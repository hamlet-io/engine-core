package io.hamlet.freemarkerwrapper.files.methods.to.cmdb;

import freemarker.core.Environment;
import freemarker.template.*;
import io.hamlet.freemarkerwrapper.files.adapters.JsonStringAdapter;
import io.hamlet.freemarkerwrapper.files.meta.cmdb.CMDBMeta;
import io.hamlet.freemarkerwrapper.files.methods.to.ToLayerMethod;
import io.hamlet.freemarkerwrapper.files.processors.cmdb.CMDBProcessor;
import io.hamlet.freemarkerwrapper.utils.FreemarkerUtil;

import java.util.List;
import java.util.Map;

public class ToCMDBMethod extends ToLayerMethod implements TemplateMethodModelEx {

    public TemplateModel exec(List args) throws TemplateModelException {
        if (args.size() != 3) {
            throw new TemplateModelException("Wrong arguments");
        }
        Object pathObj = args.get(0);
        String path = null;
        if (pathObj instanceof SimpleScalar){
            path = pathObj.toString();
        } else if (pathObj instanceof JsonStringAdapter){
            path = ((JsonStringAdapter) pathObj).getAsString();
        }

        Object contentObj  = FreemarkerUtil.ftlVarToCoreJavaObject((TemplateModel)args.get(1));

        meta = new CMDBMeta();
        List<String> lookupDirs = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("lookupDirs")).getWrappedObject();
        List<String> CMDBNames = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("CMDBNames")).getWrappedObject();
        Map<String, String> cmdbPathMapping = (Map<String, String>) ((DefaultMapAdapter) Environment.getCurrentEnvironment().getGlobalVariable("cmdbPathMappings")).getWrappedObject();
        String baseCMDB = ((SimpleScalar) Environment.getCurrentEnvironment().getGlobalVariable("baseCMDB")).getAsString();
        TemplateHashModelEx options = (TemplateHashModelEx)args.get(2);
        TemplateModelIterator iterator = options.keys().iterator();
        boolean append = Boolean.FALSE;
        boolean sync = Boolean.TRUE;
        String format = null;
        while (iterator.hasNext()){
            TemplateModel key = iterator.next();
            if ("Append".equalsIgnoreCase(key.toString())){
                append = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            }
            else if ("Synch".equalsIgnoreCase(key.toString())){
                sync = ((TemplateBooleanModel) options.get(key.toString())).getAsBoolean();
            }
            else if ("Format".equalsIgnoreCase(key.toString())){
                format = ((SimpleScalar) options.get(key.toString())).getAsString();
            }
        }
        CMDBMeta cmdbMeta = (CMDBMeta)meta;
        cmdbMeta.setToPath(path);
        cmdbMeta.setLookupDirs(lookupDirs);
        cmdbMeta.setCMDBs(cmdbPathMapping);
        cmdbMeta.setCMDBNamesList(CMDBNames);
        cmdbMeta.setBaseCMDB(baseCMDB);
        cmdbMeta.setAppend(append);
        cmdbMeta.setSync(sync);
        cmdbMeta.setFormat(format);
        cmdbMeta.setContent(contentObj);

        layerProcessor = new CMDBProcessor();
        return super.process();
    }
}
