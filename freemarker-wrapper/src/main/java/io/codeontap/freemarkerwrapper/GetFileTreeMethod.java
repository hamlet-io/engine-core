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

        List<String> lookupDirs = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("lookupDirs")).getWrappedObject();
        List<String> CMDBNames = (List<String>) ((DefaultListAdapter) Environment.getCurrentEnvironment().getGlobalVariable("CMDBNames")).getWrappedObject();
        Map<String, String> cmdbPathMapping = (Map<String, String>) ((DefaultMapAdapter) Environment.getCurrentEnvironment().getGlobalVariable("cmdbPathMappings")).getWrappedObject();
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
        SimpleScalar regexScalar = null;
        boolean ignoreDotDirectories = Boolean.TRUE;
        boolean ignoreDotFiles = Boolean.TRUE;
        boolean includeCMDBInformation = Boolean.FALSE;
        boolean useCMDBPrefix = Boolean.FALSE;
        while (iterator.hasNext()){
            TemplateModel key = iterator.next();
            if ("Regex".equalsIgnoreCase(key.toString())){
                Object regex = options.get("Regex");
                if(regex instanceof TemplateSequenceModel)
                    regexSequence = (TemplateSequenceModel)regex;
                else if(regex instanceof SimpleScalar)
                    regexScalar = (SimpleScalar)regex;
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
            if(regexScalar == null) {
                regexList.add("*.*");
            } else {
                regexList.add(regexScalar.getAsString());
            }
        } else {
            for (int i=0; i < regexSequence.size();i++){
                regexList.add(regexSequence.get(i).toString());
            }
        }

        CMDBProcessor cmdbProcessor = new CMDBProcessor();
        Map<String, JsonObject> result = null;
        try {
            result = cmdbProcessor.getFileTree(lookupDirs, cmdbPathMapping, CMDBNames,
                        baseCMDB, startingPath, regexList,ignoreDotDirectories, ignoreDotFiles, includeCMDBInformation, useCMDBPrefix);
        } catch (RunFreeMarkerException e) {
            e.printStackTrace();
        }

        return new SimpleHash(result, Environment.getCurrentEnvironment().getConfiguration().getObjectWrapper());
    }
}
