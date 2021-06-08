package io.hamlet.freemarkerwrapper;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.core.Environment;
import freemarker.core.StopException;
import freemarker.template.*;
import io.hamlet.freemarkerwrapper.files.adapters.JsonValueWrapper;
import io.hamlet.freemarkerwrapper.files.methods.cp.layer.cmdb.CpCMDBMethod;
import io.hamlet.freemarkerwrapper.files.methods.init.layer.cmdb.InitCMDBsMethod;
import io.hamlet.freemarkerwrapper.files.methods.init.layer.plugin.InitPluginsMethod;
import io.hamlet.freemarkerwrapper.files.methods.list.layer.cmdb.GetCMDBsMethod;
import io.hamlet.freemarkerwrapper.files.methods.list.layer.plugin.GetPluginLayersMethod;
import io.hamlet.freemarkerwrapper.files.methods.mkdir.layer.cmdb.MkdirCMDBMethod;
import io.hamlet.freemarkerwrapper.files.methods.rm.layer.cmdb.RmCMDBMethod;
import io.hamlet.freemarkerwrapper.files.methods.set.status.SetExitStatusMethod;
import io.hamlet.freemarkerwrapper.files.methods.to.console.ToConsoleMethod;
import io.hamlet.freemarkerwrapper.files.methods.to.layer.cmdb.ToCMDBMethod;
import io.hamlet.freemarkerwrapper.files.methods.tree.layer.cmdb.GetCMDBTreeMethod;
import io.hamlet.freemarkerwrapper.files.methods.tree.layer.plugin.GetPluginTreeMethod;
import io.hamlet.freemarkerwrapper.files.processors.status.StatusProcessor;
import io.hamlet.freemarkerwrapper.utils.IPAddressGetSubNetworksMethod;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.*;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Created by kshychko on 24.02.14.
 */
public class RunFreeMarker {

    final static Options options = new Options();
    private static final Logger log = LogManager.getLogger();
    private static String templateFileName = null;
    private static String outputFileName = null;
    private static Map<String, Object> input = null;
    private static Map<String, Object> rawInput = null;
    private static Configuration cfg;
    private static final Version freemarkerVersion = Configuration.VERSION_2_3_31;
    private static final String GENERATION_LOG_LEVEL_VAR_NAME = "GENERATION_LOG_LEVEL";

    public static void main(String[] args) {
        try {
            Attributes mainAttributes = readProperties();
            String version = mainAttributes.getValue("Implementation-Version");

            cfg = new Configuration(freemarkerVersion);
            cfg.setDefaultEncoding("UTF-8");
            cfg.setLocale(Locale.UK);
            cfg.setObjectWrapper(new JsonValueWrapper(cfg.getIncompatibleImprovements()));
            cfg.setTemplateExceptionHandler(new WrapperTemplateExceptionHandler());
            cfg.setLogTemplateExceptions(false);
            input = new HashMap<String, Object>();
            rawInput = new HashMap<String, Object>();

            Option directoryOption = new Option("d", true, "templates directories. Multiple options are allowed. Multiple values are allowed.");
            directoryOption.setArgs(Option.UNLIMITED_VALUES);
            directoryOption.setValueSeparator(';');

            Option versionOption = new Option("?", "version", false, "display this help.");
            Option debugOption = new Option(null, "debug", false, "set logging level to debug.");
            Option traceOption = new Option(null, "trace", false, "set logging level to trace.");
            Option infoOption = new Option(null, "info", false, "set logging level to info.");
            Option warnOption = new Option(null, "warn", false, "set logging level to warn.");
            Option errorOption = new Option(null, "error", false, "set logging level to error.");
            Option fatalOption = new Option(null, "fatal", false, "set logging level to fatal.");

            Option inputOption = new Option("i", true, "template file.");

            Option variablesOption = new Option("v", true, "variables for freemarker template.");
            variablesOption.setArgs(Option.UNLIMITED_VALUES);
            variablesOption.setValueSeparator('=');

            Option rawVariablesOption = new Option("r", true, "raw variables for freemarker template.");
            rawVariablesOption.setArgs(Option.UNLIMITED_VALUES);
            rawVariablesOption.setValueSeparator('=');

            Option outputOption = new Option("o", true, "output file.");

            Option cmdbPathMappingOption = new Option("g", true, "the mapping of CMDB names to physical paths.");
            cmdbPathMappingOption.setRequired(false);
            cmdbPathMappingOption.setArgs(Option.UNLIMITED_VALUES);

            Option cmdbNamesOption = new Option("c", true, "the CMDBs to be processed.");
            cmdbNamesOption.setArgs(Option.UNLIMITED_VALUES);

            Option cmdbBaseNameOption = new Option("b", true, "the base CMDB name.");

            options.addOption(directoryOption);
            options.addOption(versionOption);
            options.addOption(inputOption);
            options.addOption(variablesOption);
            options.addOption(rawVariablesOption);
            options.addOption(outputOption);
            options.addOption(cmdbPathMappingOption);
            options.addOption(cmdbNamesOption);
            options.addOption(cmdbBaseNameOption);
            options.addOption(debugOption);
            options.addOption(traceOption);
            options.addOption(infoOption);
            options.addOption(warnOption);
            options.addOption(errorOption);
            options.addOption(fatalOption);

            HelpFormatter formatter = new HelpFormatter();

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String generationLogLevel = System.getenv(GENERATION_LOG_LEVEL_VAR_NAME);

            if (cmd.hasOption(traceOption.getLongOpt())) {
                generationLogLevel = traceOption.getLongOpt();
            } else if (cmd.hasOption(debugOption.getLongOpt())) {
                generationLogLevel = debugOption.getLongOpt();
            } else if (cmd.hasOption(infoOption.getLongOpt())) {
                generationLogLevel = infoOption.getLongOpt();
            } else if (cmd.hasOption(warnOption.getLongOpt())) {
                generationLogLevel = warnOption.getLongOpt();
            } else if (cmd.hasOption(errorOption.getLongOpt())) {
                generationLogLevel = errorOption.getLongOpt();
            } else if (cmd.hasOption(fatalOption.getLongOpt())) {
                generationLogLevel = fatalOption.getLongOpt();
            }

            if (generationLogLevel != null) {
                Configurator.setLevel("io.hamlet.freemarkerwrapper.RunFreeMarker", Level.valueOf(generationLogLevel));
            } else {
                Configurator.setLevel("io.hamlet.freemarkerwrapper.RunFreeMarker", Level.INFO);
            }

            if (cmd.hasOption(versionOption.getOpt())) {
                log.info(String.format("GSGEN v.%s\n\nFreemarker version - %s \n", version, freemarkerVersion));
                formatter.printHelp(String.format("java -jar freemarker-wrapper-%s.jar", version), options);
                return;
            }

            input.put("baseCMDB", "");

            List<String> CMDBNames = new ArrayList<>();

            Iterator<Option> optionIterator = cmd.iterator();
            List<FileTemplateLoader> loaderList = new ArrayList<>();
            Map<String, String> cmdbPathMappings = new TreeMap<>();
            List<String> directories = new ArrayList<>();
            List<String> lookupDirs = new ArrayList<>();


            while (optionIterator.hasNext()) {
                Option option = optionIterator.next();
                String opt = StringUtils.defaultIfEmpty(option.getOpt(), "");
                String[] values = option.getValues();
                if (opt.equals(inputOption.getOpt())) {
                    templateFileName = option.getValue();
                } else if (opt.equals(variablesOption.getOpt())) {
                    for (int i = 0; i < values.length; i++) {
                        input.put(values[i], values[i + 1]);
                        i++;
                    }
                } else if (opt.equals(rawVariablesOption.getOpt())) {
                    for (int i = 0; i < values.length; i++) {
                        rawInput.put(values[i], values[i + 1]);
                        i++;
                    }
                } else if (opt.equals(outputOption.getOpt())) {
                    outputFileName = option.getValue();
                } else if (opt.equals(directoryOption.getOpt())) {
                    for (String directory : values) {
                        if (SystemUtils.IS_OS_WINDOWS && directory.startsWith("/") && directory.length() > 2) {
                            directory = directory.substring(1, 2).toUpperCase().concat(":").concat(directory.substring(2));
                        }
                        directories.add(directory);
                        log.debug("Directory to be added to the template loader: " + directory);
                        loaderList.add(new FileTemplateLoader(new File(directory)));
                    }
                } else if (opt.equals(cmdbPathMappingOption.getOpt())) {
                    for (int i = 0; i < values.length; i++) {
                        if (StringUtils.contains(values[i], "=")) {
                            String[] pair = values[i].split("=");
                            cmdbPathMappings.put(pair[0], pair[1]);
                        } else {
                            lookupDirs.add(values[i]);
                        }
                    }
                } else if (opt.equals(cmdbNamesOption.getOpt())) {
                    CMDBNames.addAll(Arrays.asList(values));
                } else if (opt.equals(cmdbBaseNameOption.getOpt())) {
                    input.put("baseCMDB", option.getValue());
                }

            }

            input.put("lookupDirs", lookupDirs);
            input.put("cmdbPathMappings", cmdbPathMappings);
            input.put("CMDBNames", CMDBNames);
            input.put("pluginLayers", directories);

            loaderList.add(new FileTemplateLoader(new File("/")));

            log.debug("Templates directories in the order as they will be searched:");
            for (FileTemplateLoader fileTemplateLoader : loaderList) {
                log.debug(fileTemplateLoader.getBaseDirectory().getAbsolutePath());
            }
            cfg.setTemplateLoader(new MultiTemplateLoader(loaderList.toArray(new FileTemplateLoader[]{})));

            if (!StringUtils.isBlank(templateFileName)) {
                log.debug("Template file name - " + templateFileName);
            } else {
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
                for (int i = 0; i < lines.length; i++) {
                    log.debug(lines[i]);
                    printWriter.println(lines[i]);
                }

                fileWriter.close();
            }

            if (!StringUtils.isBlank(outputFileName)) {
                log.debug("Output file name - " + outputFileName);
            }
            for (String key : input.keySet()) {
                try {
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(new FileInputStream(input.get(key).toString()), writer, "UTF-8");
                    log.debug("JSON - " + key + ", value - " + input.get(key));
                    input.put(key, writer.toString());
                } catch (FileNotFoundException e) {
                    log.debug("Variable - " + key + ", value - " + input.get(key));
                }
            }

            for (String key : rawInput.keySet()) {
                log.debug("Raw Variable - " + key + ", value - " + rawInput.get(key));
            }
            input.putAll(rawInput);

            input.put("random", new Random());
            input.put(IPAddressGetSubNetworksMethod.METHOD_NAME, new IPAddressGetSubNetworksMethod());
            input.put(GetCMDBTreeMethod.METHOD_NAME, new GetCMDBTreeMethod());
            input.put(GetCMDBsMethod.METHOD_NAME, new GetCMDBsMethod());
            input.put(MkdirCMDBMethod.METHOD_NAME, new MkdirCMDBMethod());
            input.put(CpCMDBMethod.METHOD_NAME, new CpCMDBMethod());
            input.put(ToCMDBMethod.METHOD_NAME, new ToCMDBMethod());
            input.put(ToConsoleMethod.METHOD_NAME, new ToConsoleMethod());
            input.put(RmCMDBMethod.METHOD_NAME, new RmCMDBMethod());
            input.put(SetExitStatusMethod.METHOD_NAME, new SetExitStatusMethod());
            input.put(GetPluginLayersMethod.METHOD_NAME, new GetPluginLayersMethod());
            input.put(GetPluginTreeMethod.METHOD_NAME, new GetPluginTreeMethod());
            input.put(InitCMDBsMethod.METHOD_NAME, new InitCMDBsMethod());
            input.put(InitPluginsMethod.METHOD_NAME, new InitPluginsMethod());


            Template freeMarkerTemplate = cfg.getTemplate(templateFileName);
            if (outputFileName != null) {
                freeMarkerTemplate.process(input, new FileWriter(outputFileName));
            } else {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
                freeMarkerTemplate.process(input, consoleWriter);
                System.out.println("--------------------------- OUTPUT ---------------------------");
                System.out.write(byteArrayOutputStream.toByteArray());
            }
        } catch (StopException e){
            String msg = e.getMessage();
            System.err.print("Encountered stop instruction");
            if (msg != null && !msg.equals("")) {
                System.err.println("\nCause given: " + msg);
            } else System.err.println();
            System.exit(WrapperTemplateExceptionHandler.STOP_EXCEPTION_EXIT_CODE);
        } catch (ParseException e) {
            e.printStackTrace(System.err);
            System.exit(WrapperTemplateExceptionHandler.CLI_PARSE_EXCEPTION_EXIT_CODE);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(WrapperTemplateExceptionHandler.IO_EXCEPTION_EXIT_CODE);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(WrapperTemplateExceptionHandler.EXCEPTION_EXIT_CODE);
        }
        if(System.getProperty(StatusProcessor.existStatusVariableName)!=null){
            System.exit(Integer.parseInt(System.getProperty(StatusProcessor.existStatusVariableName)));
        }
    }

    public static Attributes readProperties() throws IOException {
        final InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("META-INF/MANIFEST.MF");
        final Manifest manifest = new Manifest(resourceAsStream);
        final Attributes mainAttributes = manifest.getMainAttributes();
        return mainAttributes;
    }

}

