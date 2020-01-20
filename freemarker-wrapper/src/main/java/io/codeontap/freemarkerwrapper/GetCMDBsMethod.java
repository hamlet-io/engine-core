package io.codeontap.freemarkerwrapper;

import freemarker.core.Environment;
import freemarker.template.*;

import javax.json.JsonArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetCMDBsMethod implements TemplateMethodModelEx {

    public TemplateModel exec(List args) throws TemplateModelException {

        List<String> lookupDirs = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("lookupDirs")).getWrappedObject();
        List<String> CMDBNames = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("CMDBNames")).getWrappedObject();
        Map<String, String> cmdbPathMapping = (Map<String, String>) ((DefaultMapAdapter) Environment.getCurrentEnvironment().getGlobalVariable("cmdbPathMappings")).getWrappedObject();
        String baseCMDB = ((SimpleScalar) Environment.getCurrentEnvironment().getGlobalVariable("baseCMDB")).getAsString();
        TemplateHashModelEx options = (TemplateHashModelEx)args.get(0);
        TemplateModelIterator iterator = options.keys().iterator();
        TemplateSequenceModel regexSequence = null;
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
        List<String> regexList = new ArrayList<>();
        if(regexSequence == null || regexSequence.size() == 0){
            regexList.add("*.*");
        } else {
            for (int i=0; i < regexSequence.size();i++){
                regexList.add(regexSequence.get(i).toString());
            }
        }

        CMDBProcessor cmdbProcessor = new CMDBProcessor();
        JsonArray result = null;
        try {
            result = cmdbProcessor.getCMDBs(lookupDirs, cmdbPathMapping, CMDBNames,
                        baseCMDB, useCMDBPrefix, activeOnly);
        } catch (RunFreeMarkerException e) {
            e.printStackTrace();
        }

        return new SimpleSequence(result, Environment.getCurrentEnvironment().getConfiguration().getObjectWrapper());
    }
}
