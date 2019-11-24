package io.codeontap.freemarkerwrapper;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.template.*;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Created by kshychko on 24.02.14.
 */
public class RunFreeMarker {

    private static String templateFileName = null;
    private static String outputFileName = null;
    private static Map<String, Object> input = null;
    private static Map<String, Object> rawInput = null;
    private static Configuration cfg;
    private static String separator = " ";

    final static Options options = new Options();

    private static Version freemarkerVersion = Configuration.VERSION_2_3_28;

    public static void main (String args[]) throws RunFreeMarkerException, IOException, TemplateException, ParseException {

        Attributes mainAttribs = null;
        String version = "";
        try {
            mainAttribs = readProperties();
            version = mainAttribs.getValue("Implementation-Version");
        } catch (Exception e) {
            System.out.println("Unable to parse manifest file.");
            e.printStackTrace();
        }

        cfg = new Configuration(freemarkerVersion);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.UK);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        input = new HashMap<String, Object>();
        rawInput = new HashMap<String, Object>();

        Option directoryOption = new Option("d", true, "templates directories. Multiple options are allowed. Multiple values are allowed.");
        directoryOption.setArgs(Option.UNLIMITED_VALUES);
        directoryOption.setValueSeparator(';');
        Option versionOption = new Option("?", "version",false, "display this help.");
        Option inputOption = new Option("i", true, "template file.");
        Option variablesOption = new Option("v", true, "variables for freemarker template.");
        variablesOption.setArgs(Option.UNLIMITED_VALUES);
        variablesOption.setValueSeparator('=');
        Option rawVariablesOption = new Option("r", true, "raw variables for freemarker template.");
        rawVariablesOption.setArgs(Option.UNLIMITED_VALUES);
        rawVariablesOption.setValueSeparator('=');
        Option outputOption = new Option("o", true, "output file.");

        options.addOption(directoryOption);
        options.addOption(versionOption);
        options.addOption(inputOption);
        options.addOption(variablesOption);
        options.addOption(rawVariablesOption);
        options.addOption(outputOption);

        HelpFormatter formatter = new HelpFormatter();

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);
        FileTemplateLoader[] templateLoaders = null;

        if(cmd.hasOption(versionOption.getOpt())){
            System.out.println(String.format("GSGEN v.%s\n\nFreemarker version - %s \n", version, freemarkerVersion));
            formatter.printHelp(String.format("java -jar freemarker-wrapper-%s.jar", version), options);
            return;
        }

        if(cmd.hasOption(inputOption.getOpt())){
            templateFileName = cmd.getOptionValue(inputOption.getOpt());
        }

        if (cmd.hasOption(variablesOption.getOpt())){
            String[] values = cmd.getOptionValues(variablesOption.getOpt());
            for (int i=0; i<values.length; i++){
                input.put(values[i], values[i+1]);
                i++;
            }
        }

        if (cmd.hasOption(rawVariablesOption.getOpt())){
            String[] values = cmd.getOptionValues(rawVariablesOption.getOpt());
            for (int i=0; i<values.length; i++){
                rawInput.put(values[i], values[i+1]);
                i++;
            }
        }

        if (cmd.hasOption(outputOption.getOpt())){
            outputFileName = cmd.getOptionValue(outputOption.getOpt());
        }

        if (cmd.hasOption(directoryOption.getOpt())) {
            String[] optionValues = cmd.getOptionValues(directoryOption.getOpt());
            templateLoaders = new FileTemplateLoader[optionValues.length+1];
            int i=0;
            for (String directory:optionValues){
                templateLoaders[i] = new FileTemplateLoader(new File(directory));
                i++;
            }
            templateLoaders[i] = new FileTemplateLoader(new File("/"));
            System.out.println("Templates directories in the order as they will be searched:");
            for(FileTemplateLoader fileTemplateLoader : templateLoaders){
                System.out.println(fileTemplateLoader.getBaseDirectory().getAbsolutePath());
            }
            cfg.setTemplateLoader(new MultiTemplateLoader(templateLoaders));
        }

        if(!StringUtils.isBlank(templateFileName))
        {
            System.out.println("Template file name - " + templateFileName);
        }
        else
        {
            Console c = System.console();
            if (c == null) {
                System.err.println("No console.");
                System.exit(1);
            }
            templateFileName = "template_from_stdin.tfl";
            String template = c.readLine("Enter your template: ");
            FileWriter fileWriter = new FileWriter(templateFileName);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            String[] lines = StringUtils.split(template, "\\n");
            for(int i = 0; i<lines.length; i++)
            {
                System.out.println(lines[i]);
                printWriter.println(lines[i]);
            }

            fileWriter.close();
        }

        if(!StringUtils.isBlank(outputFileName))
        {
            System.out.println("Output file name - " + outputFileName);
        }
        for (String key:input.keySet())
        {
            try {
                StringWriter writer = new StringWriter();
                IOUtils.copy(new FileInputStream(new File(input.get(key).toString())), writer, "UTF-8");
                System.out.println("JSON - " + key + ", value - " + input.get(key));
                input.put(key, writer.toString());
            }
            catch (FileNotFoundException e)
            {
                System.out.println("Variable - " + key + ", value - " + input.get(key));
            }
        }

        for (String key:rawInput.keySet())
        {
            System.out.println("Raw Variable - " + key + ", value - " + rawInput.get(key));
        }
        input.putAll(rawInput);

        input.put("random", new Random());
        input.put("IPAddress__getSubNetworks", new IPAddressGetSubNetworksMethod());

        Template freeMarkerTemplate = cfg.getTemplate(templateFileName);
        if(outputFileName!=null)
        {
            freeMarkerTemplate.process(input, new FileWriter(outputFileName));
        }
        else
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
            freeMarkerTemplate.process(input, consoleWriter);
            System.out.println("--------------------------- OUTPUT ---------------------------");
            System.out.write(byteArrayOutputStream.toByteArray());
        }
    }

    public static Attributes readProperties() throws IOException{
        final InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("META-INF/MANIFEST.MF" );
        final Manifest manifest = new Manifest(resourceAsStream);
        final Attributes mainAttribs = manifest.getMainAttributes();
        return mainAttribs;
    }

}
class RunFreeMarkerException extends Exception
{
    RunFreeMarkerException(String message) {
        super(message);
    }
}
