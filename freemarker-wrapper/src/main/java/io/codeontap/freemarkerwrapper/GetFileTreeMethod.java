package io.codeontap.freemarkerwrapper;

import freemarker.core.Environment;
import freemarker.template.*;

import javax.json.JsonObject;
import java.util.*;

public class GetFileTreeMethod implements TemplateMethodModelEx {

    public TemplateModel exec(List args) throws TemplateModelException {
        if (args.size() != 2) {
            throw new TemplateModelException("Wrong arguments");
        }

        String lookupDir = Environment.getCurrentEnvironment().getGlobalVariable("lookupDir").toString();
        List<String> CMDBNames = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("CMDBNames")).getWrappedObject();
        Map<String, String> cmdbPathMapping = (Map<String, String>) ((DefaultMapAdapter) Environment.getCurrentEnvironment().getGlobalVariable("cmdbPathMapping")).getWrappedObject();
        String baseCMDB = ((SimpleScalar) Environment.getCurrentEnvironment().getGlobalVariable("baseCMDB")).getAsString();
        Object startingPathObj = args.get(0);
        String startingPath = null;
        if (startingPathObj instanceof SimpleScalar){
            startingPath = startingPathObj.toString();
        }else if (startingPathObj instanceof JsonStringAdapter){
            startingPath = ((JsonStringAdapter) startingPathObj).getAsString();
        }
        TemplateHashModelEx options = (TemplateHashModelEx)args.get(1);
        TemplateModelIterator iterator = options.keys().iterator();
        TemplateSequenceModel regexSequence = null;
        boolean ignoreDotDirectories = Boolean.TRUE;
        boolean ignoreDotFiles = Boolean.TRUE;
        boolean includeCMDBInformation = Boolean.FALSE;
        boolean useCMDBPrefix = Boolean.FALSE;
        while (iterator.hasNext()){
            TemplateModel key = iterator.next();
            if ("Regex".equalsIgnoreCase(key.toString())){
                regexSequence = (TemplateSequenceModel)options.get("Regex");
            } else if ("IgnoreDotDirectories".equalsIgnoreCase(key.toString())){
                ignoreDotDirectories = ((TemplateBooleanModel) options.get("IgnoreDotDirectories")).getAsBoolean();
            } else if ("IgnoreDotFiles".equalsIgnoreCase(key.toString())){
                ignoreDotFiles = ((TemplateBooleanModel) options.get("IgnoreDotFiles")).getAsBoolean();

            } else if ("IncludeCMDBInformation".equalsIgnoreCase(key.toString())){
                includeCMDBInformation = ((TemplateBooleanModel) options.get("IncludeCMDBInformation")).getAsBoolean();

            } else if ("UseCMDBPrefix".equalsIgnoreCase(key.toString())){
                useCMDBPrefix = ((TemplateBooleanModel) options.get("UseCMDBPrefix")).getAsBoolean();
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
        Map<String, JsonObject> result = null;
        try {
            result = cmdbProcessor.getFileTree(lookupDir, cmdbPathMapping, CMDBNames,
                        baseCMDB, startingPath, regexList,ignoreDotDirectories, ignoreDotFiles, includeCMDBInformation, useCMDBPrefix);
        } catch (RunFreeMarkerException e) {
            e.printStackTrace();
        }

        return new SimpleHash(result, Environment.getCurrentEnvironment().getConfiguration().getObjectWrapper());
    }
}
