package io.codeontap.freemarkerwrapper;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.*;
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

    private static Version freemarkerVersion = Configuration.VERSION_2_3_27;

    public static void main (String args[]) throws RunFreeMarkerException, IOException, TemplateException
    {
        cfg = new Configuration(freemarkerVersion);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.UK);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        input = new HashMap<String, Object>();
        rawInput = new HashMap<String, Object>();

        FileTemplateLoader ftl1 = null;
        FileTemplateLoader ftl2 = new FileTemplateLoader(new File("/"));
        for(int i = 0; i < args.length; i++)
        {
            if (args[i].startsWith("-"))
            {
                if("--version".equalsIgnoreCase(args[i]) || "-?".equalsIgnoreCase(args[i])){
                    echoHelp();
                    return;
                }

                if("i".equalsIgnoreCase(StringUtils.substringAfter(args[i], "-")))
                {
                    try{
                        if (StringUtils.startsWith(args[i+1],"-"))
                        {
                            throw new ArrayIndexOutOfBoundsException("");
                        }
                        else
                        {
                            templateFileName = args[i+1];
                            i++;
                        }
                    }
                    catch (ArrayIndexOutOfBoundsException e)
                    {
                        throw new RunFreeMarkerException("No value for option -i found");
                    }
                }
                else if("v".equalsIgnoreCase(StringUtils.substringAfter(args[i], "-")))
                {
                    try {
                        for (int j = i+1; j < args.length; j++)
                        {
                            if (args[j].startsWith("-"))
                            {
                                i = j -1;
                                break;
                            }
                            else
                            {
                                String[] vars = StringUtils.split(args[j],"=");
                                input.put(vars[0],vars.length>1?vars[1]:"");
                                i = j;
                            }
                        }

                    }
                    catch (ArrayIndexOutOfBoundsException e)
                    {
                        throw new RunFreeMarkerException("No value for option -v found");
                    }

                }
                else if("r".equalsIgnoreCase(StringUtils.substringAfter(args[i], "-")))
                {
                    try {
                        for (int j = i+1; j < args.length; j++)
                        {
                            if (args[j].startsWith("-"))
                            {
                                i = j -1;
                                break;
                            }
                            else
                            {
                                String[] vars = StringUtils.split(args[j],"=");
                                rawInput.put(vars[0],vars.length>1?vars[1]:"");
                                i = j;
                            }
                        }

                    }
                    catch (ArrayIndexOutOfBoundsException e)
                    {
                        throw new RunFreeMarkerException("No value for option -r found");
                    }

                }
                else if("o".equalsIgnoreCase(StringUtils.substringAfter(args[i], "-")))
                {
                    try
                    {
                        if (StringUtils.startsWith(args[i+1],"-"))
                        {
                            throw new ArrayIndexOutOfBoundsException("");
                        }
                        else
                            outputFileName = args[i+1];
                        i++;
                    }
                    catch (ArrayIndexOutOfBoundsException e)
                    {
                        throw new RunFreeMarkerException("No value for option -o found");
                    }

                }
            else if("d".equalsIgnoreCase(StringUtils.substringAfter(args[i], "-")))
            {
                try
                {
                    if (StringUtils.startsWith(args[i+1],"-"))
                    {
                        throw new ArrayIndexOutOfBoundsException("");
                    }
                    else
                        ftl1 = new FileTemplateLoader(new File(args[i+1]));
                    i++;
                }
                catch (ArrayIndexOutOfBoundsException e)
                {
                    throw new RunFreeMarkerException("No value for option -d found");
                }

            }
                else
                {
                    throw new RunFreeMarkerException("unknown option - " + args[i]);
                }
            }
            else
            {
                throw new RunFreeMarkerException("unknown option - " + args[i] + ". Allowed options - -o, -v, -i");
            }
        }

        cfg.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{ftl1, ftl2}));

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
/*
            printWriter.print(template);
*/
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
            /*InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            System.out.print(inputStream.read());*/
        }
    }

    /**
     * Commandline help output
     */
    public static void echoHelp() {
        Attributes mainAttribs = null;
        String version = "";
        try {
            mainAttribs = readProperties();
            version = mainAttribs.getValue("Implementation-Version");
        } catch (Exception e) {
            System.out.println("Unable to parse manifest file.");
            e.printStackTrace();
        }

        System.out.println("\n" +
                "GSGEN v." + version+ "\n\n"+
                "Freemarker version - " +  freemarkerVersion + " \n" +
                "Params:");
        System.out.println("-i  : template file.");
        System.out.println("-v  : variables for freemarker template.");
        System.out.println("-r  : raw variables for freemarker template.");
        System.out.println("-o  : output file.");
        System.out.println("-d  : templates directory.");
        System.out.println("-?  or --version            	 : Display this help.");
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
