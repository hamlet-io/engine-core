package io.codeontap.freemarkerwrapper;

import freemarker.cache.FileTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.*;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetCMDBTreeMethodTest {

    private static Version freemarkerVersion = Configuration.VERSION_2_3_28;
    private static Configuration cfg;
    private static Map<String, Object> input = null;

    private final String templatesPath = "/tmp/gen3/templates";
    private final String cmdbsPath = "/tmp/gen3/cmdbs";


    {
        cfg = new Configuration(freemarkerVersion);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.UK);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setObjectWrapper(new JsonValueWrapper(cfg.getIncompatibleImprovements()));
    }


    @Before
    public void before(){
        File templateDir = new File(templatesPath);
        if (!templateDir.exists()) templateDir.mkdirs();
        File cmdbsDir = new File(cmdbsPath);
        if (!cmdbsDir.exists()) cmdbsDir.mkdirs();

    }

    @After
    public void after(){
        File templateDir = new File(templatesPath);
        if (templateDir.exists()) {
            try {
                FileUtils.deleteDirectory(templateDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File cmdbsDir = new File(cmdbsPath);
        if (cmdbsDir.exists()) {
            try {
                FileUtils.deleteDirectory(cmdbsDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void getCMDBs(){
        input = new HashMap<String, Object>();
        input.put("getFileTree", new GetCMDBTreeMethod());
        input.put("getCMDBs", new GetCMDBsMethod());

        String fileName = templatesPath.concat("/file.ftl");
        try {
            Files.write(Paths.get(fileName), (getCMDBsTemplateFileActiveOnly).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String content = getCMDBsAccountsTemplate;

        createCMDB("accounts", content);
        createCMDB("api", "{}");
        createCMDB("almv2", "{}");

        Map<String,String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);

        input.put("lookupDirs", Arrays.asList(new String[]{}));
        input.put("CMDBNames", Arrays.asList(new String[]{  "accounts","api", "almv2",}));
        input.put("baseCMDB","accounts");

        try {
            cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
            Template freeMarkerTemplate = cfg.getTemplate(fileName);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
            Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
            freeMarkerTemplate.process(input, consoleWriter);
            String output = new String(byteArrayOutputStream.toByteArray());
            Pattern p = Pattern.compile("Name : ");
            Matcher m = p.matcher(output);
            int count = 0;
            while (m.find())
                count++;
            Assert.assertEquals(0, count);
            System.out.println("--------------------------- OUTPUT ---------------------------");
            System.out.write(output.getBytes());

            input.put("lookupDirs", Arrays.asList(new String[]{cmdbsPath}));
            byteArrayOutputStream.reset();
            consoleWriter.flush();
            freeMarkerTemplate.process(input, consoleWriter);
            output = new String(byteArrayOutputStream.toByteArray());
            m = p.matcher(output);
            count = 0;
            while (m.find())
                count++;
            Assert.assertEquals(3, count);
            System.out.println("--------------------------- OUTPUT ---------------------------");
            System.out.write(output.getBytes());

            input.put("lookupDirs", Arrays.asList(new String[]{}));
            cmdbPathMapping.put("accounts", cmdbsPath.concat("/accounts"));
            cmdbPathMapping.put("almv2", cmdbsPath.concat("/almv2"));
            cmdbPathMapping.put("api", cmdbsPath.concat("/api"));
            input.put("cmdbPathMappings", cmdbPathMapping);
            byteArrayOutputStream.reset();
            consoleWriter.flush();
            freeMarkerTemplate.process(input, consoleWriter);
            output = new String(byteArrayOutputStream.toByteArray());
            m = p.matcher(output);
            count = 0;
            while (m.find())
                count++;
            Assert.assertEquals(3, count);
            System.out.println("--------------------------- OUTPUT ---------------------------");
            System.out.write(output.getBytes());

            input.put("CMDBNames", Arrays.asList(new String[]{"accounts", "api"}));
            byteArrayOutputStream.reset();
            consoleWriter.flush();
            freeMarkerTemplate.process(input, consoleWriter);
            output = new String(byteArrayOutputStream.toByteArray());
            m = p.matcher(output);
            count = 0;
            while (m.find())
                count++;
            Assert.assertEquals(2, count);
            System.out.println("--------------------------- OUTPUT ---------------------------");
            System.out.write(output.getBytes());

        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLookupDir(){
        try {
            input = new HashMap<String, Object>();
            input.put("getFileTree", new GetCMDBTreeMethod());
            String fileName = templatesPath.concat("/file.ftl");
            try {
                Files.write(Paths.get(fileName), (getFileTreeAccountsTemplate).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String content = getCMDBsAccountsTemplate;
            createCMDB("accounts", content);
            createCMDB("api", "{}");
            createCMDB("almv2", "{}");
            Map<String,String> cmdbPathMapping = new HashMap();
            input.put("cmdbPathMappings", cmdbPathMapping);
            input.put("lookupDirs", Arrays.asList(new String[]{cmdbsPath}));
            input.put("CMDBNames", Arrays.asList(new String[]{"accounts", "almv2", "api"}));
            input.put("baseCMDB", "accounts");
            cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
            Template freeMarkerTemplate = cfg.getTemplate(fileName);
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

    private void createCMDB(String cmdbName, String content){
        File accountsDir = new File(cmdbsPath.concat("/").concat(cmdbName));
        if (!accountsDir.exists()) accountsDir.mkdirs();
        String cmdbFile = cmdbsPath.concat("/").concat(cmdbName).concat("/.cmdb");
        try {
            Files.write(Paths.get(cmdbFile), content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final String getCMDBsTemplateFileActiveOnly = "[#ftl]\n" +
            "\n" +
            "\n" +
            "[#assign cmdbs = getCMDBs(\n" +
            "        {\"ActiveOnly\":true}\n" +
            "    ) ]\n" +
            "\n" +
            "[#list cmdbs as cmdb ]\n" +
            "[#list cmdb as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#else]${value}[/#if]\n" +
            "[/#list]\n" +
            "[/#list]";

    private final String getCMDBsAccountsTemplate = "{\n" +
            "  \"Version\": {\n" +
            "    \"Upgrade\": \"v1.3.2\",\n" +
            "    \"Cleanup\": \"v1.1.0\"\n" +
            "  },\n" +
            "  \"Layers\" : [\n" +
            "    {\n" +
            "      \"Name\" : \"api\",\n" +
            "      \"BasePath\" : \"products/api\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"Name\" : \"almv2\",\n" +
            "      \"BasePath\" : \"/products/almv2\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private final String getFileTreeAccountsTemplate = "[#ftl]\n" +
            "\n" +
            "[#assign regex=\".cmdb\"]\n" +
            "[#assign candidates =\n" +
            "  getFileTree(\n" +
            "    \"/\",\n" +
            "    {\n" +
            "        \"Regex\" : regex,\n" +
            "        \"IgnoreDotDirectories\" : false,\n" +
            "        \"IgnoreDotFiles\" : false,\n" +
            "\t\"IncludeCMDBInformation\" : true\t,\n" +
            "\t\"UseCMDBPrefix\" : false\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#list candidates as candidate ]\n" +
            "[#list candidate as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#elseif value?is_sequence]#is_sequence#[#else]${value}[/#if]\n" +
            "[/#list]\n" +
            "[/#list]\n" +
            "\n";
}
