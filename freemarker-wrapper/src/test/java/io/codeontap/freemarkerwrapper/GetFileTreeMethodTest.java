package io.codeontap.freemarkerwrapper;

import freemarker.cache.FileTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.*;
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
    }
    @Test
    public void test(){


        try {
            input = new HashMap<String, Object>();
            input.put("getFileTree", new GetFileTreeMethod());
            Map<String,String> cmdbPathMapping = new HashMap();
            input.put("cmdbPathMapping", cmdbPathMapping);
            input.put("lookupDir", "/work/psma/cot");
            input.put("CMDBNames", Arrays.asList(new String[]{"cot_accounts1", "cot_almv2", "cot_api"}));
/*
            input.put("baseCMDB", "cot_accounts");
*/
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
