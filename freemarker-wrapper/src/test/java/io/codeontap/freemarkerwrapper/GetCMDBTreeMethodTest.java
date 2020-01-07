package io.codeontap.freemarkerwrapper;

import freemarker.cache.FileTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GetFileTreeMethodTest {

    private static Version freemarkerVersion = Configuration.VERSION_2_3_28;
    private static Configuration cfg;
    private static Map<String, Object> input = null;


    {
        cfg = new Configuration(freemarkerVersion);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.UK);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setObjectWrapper(new JsonValueWrapper(cfg.getIncompatibleImprovements()));
    }

    @Test
    public void testCMDBPathMapping(){
        try {
            input = new HashMap<String, Object>();
            input.put("getFileTree", new GetFileTreeMethod());
            Map<String,String> cmdbPathMapping = new HashMap();
            cmdbPathMapping.put("accounts", "c:\\work\\psma\\cot\\accounts\\");
/*
            cmdbPathMapping.put("almv2", "/c/work/psma/cot/almv2/");
*/
            cmdbPathMapping.put("api", "c:\\work\\psma\\cot\\api\\");
            input.put("cmdbPathMappings", cmdbPathMapping);
            /*input.put("lookupDir", "/work/gs-psma/cot");*/
            input.put("lookupDirs", Arrays.asList(new String[]{""}));
            input.put("CMDBNames", Arrays.asList(new String[]{  "accounts","api"/*"almv2",*/}));
            input.put("baseCMDB","");
            cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
            Template freeMarkerTemplate = cfg.getTemplate("/work/codeontap/gen3-freemarker-wrapper/file.ftl");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

            Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
            freeMarkerTemplate.process(input, consoleWriter);
            System.out.println("--------------------------- OUTPUT ---------------------------");
            System.out.write(byteArrayOutputStream.toByteArray());

        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testCMDBPathMapping2(){
        try {
            input = new HashMap<String, Object>();
            input.put("getFileTree", new GetFileTreeMethod());
            Map<String,String> cmdbPathMapping = new HashMap();
            cmdbPathMapping.put("benchmarking", "/work/gs-fin/benchmarking-cmdb/");
            cmdbPathMapping.put("ecommerce", "/work/envris/cot/ecommerce/");
            cmdbPathMapping.put("accounts", "/work/envris/cot/accounts/");

            input.put("cmdbPathMapping", cmdbPathMapping);
            /*input.put("lookupDir", "/work/gs-psma/cot");*/
            input.put("lookupDir", "");
            input.put("CMDBNames", Arrays.asList(new String[]{"accounts"}));
            input.put("baseCMDB", "benchmarking");
            cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
            Template freeMarkerTemplate = cfg.getTemplate("/work/codeontap/gen3-freemarker-wrapper/file.ftl");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

            Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
            freeMarkerTemplate.process(input, consoleWriter);
            System.out.println("--------------------------- OUTPUT ---------------------------");
            System.out.write(byteArrayOutputStream.toByteArray());

        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testLookupDir(){
        try {
            input = new HashMap<String, Object>();
            input.put("getFileTree", new GetFileTreeMethod());
            Map<String,String> cmdbPathMapping = new HashMap();
            input.put("cmdbPathMappings", cmdbPathMapping);
            input.put("lookupDir", "/work/gs-psma/cot");
            input.put("CMDBNames", Arrays.asList(new String[]{"accounts", "almv2", "api"}));
            input.put("baseCMDB", "accounts");
            cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
            Template freeMarkerTemplate = cfg.getTemplate("/work/codeontap/gen3-freemarker-wrapper/file.ftl");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

            Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
            freeMarkerTemplate.process(input, consoleWriter);
            System.out.println("--------------------------- OUTPUT ---------------------------");
            System.out.write(byteArrayOutputStream.toByteArray());

        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testLookupDirFail(){
        try {
            input = new HashMap<String, Object>();
            input.put("getFileTree", new GetFileTreeMethod());
            Map<String,String> cmdbPathMapping = new HashMap();
            input.put("cmdbPathMapping", cmdbPathMapping);
            input.put("lookupDir", "/work/gs-psma/cot/");
            input.put("CMDBNames", Arrays.asList(new String[]{"accounts", "almv2", "api"}));
            input.put("baseCMDB", "accounts123");
            cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
            Template freeMarkerTemplate = cfg.getTemplate("/work/codeontap/gen3-freemarker-wrapper/file.ftl");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

            Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
            freeMarkerTemplate.process(input, consoleWriter);
            System.out.println("--------------------------- OUTPUT ---------------------------");
            System.out.write(byteArrayOutputStream.toByteArray());

        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }

    }

    @Test
    @Ignore
    public void testCMDBPathMappingWithCMDBPrefixes(){
        try {
            input = new HashMap<String, Object>();
            input.put("getFileTree", new GetFileTreeMethod());
            Map<String,String> cmdbPathMapping = new HashMap();
            cmdbPathMapping.put("cot_accounts", "/work/gs-psma/cot/accounts/");
            cmdbPathMapping.put("cot_almv2", "/work/gs-psma/cot/almv2/");
            cmdbPathMapping.put("cot_api", "/work/gs-psma/cot/api/");
            input.put("cmdbPathMapping", cmdbPathMapping);
            /*input.put("lookupDir", "/work/gs-psma/cot");*/
            input.put("lookupDir", "");
            input.put("CMDBNames", Arrays.asList(new String[]{"cot_accounts", "cot_almv2", "cot_api"}));
            input.put("baseCMDB", "cot_accounts");
            cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
            Template freeMarkerTemplate = cfg.getTemplate("/work/codeontap/gen3-freemarker-wrapper/file.ftl");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

            Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
            freeMarkerTemplate.process(input, consoleWriter);
            System.out.println("--------------------------- OUTPUT ---------------------------");
            System.out.write(byteArrayOutputStream.toByteArray());

        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }

    }

    @Test
    @Ignore
    public void testCMDBPrefixes(){
        try {
            input = new HashMap<String, Object>();
            input.put("getFileTree", new GetFileTreeMethod());
            Map<String,String> cmdbPathMapping = new HashMap();
            input.put("cmdbPathMapping", cmdbPathMapping);
            input.put("lookupDir", "/work/gs-psma/cot");
            input.put("CMDBNames", Arrays.asList(new String[]{"cot_accounts", "cot_almv2", "cot_api"}));
            input.put("baseCMDB", "cot_accounts");
            cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
            Template freeMarkerTemplate = cfg.getTemplate("/work/codeontap/gen3-freemarker-wrapper/file.ftl");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

            Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
            freeMarkerTemplate.process(input, consoleWriter);
            System.out.println("--------------------------- OUTPUT ---------------------------");
            System.out.write(byteArrayOutputStream.toByteArray());

        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }

    }
}
