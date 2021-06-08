package io.hamlet.freemarkerwrapper;

import freemarker.cache.FileTemplateLoader;
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
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    private static final Version freemarkerVersion = Configuration.VERSION_2_3_31;
    private static Configuration cfg;
    private static Map<String, Object> input = null;

    private final String templatesPath = "/tmp/gen3/templates";
    private final String cmdbsPath = "/tmp/gen3/cmdbs";
    private final String pluginsPath = "/tmp/gen3/plugins";
    private final String getPluginLayersTemplate = "[#ftl]\n" +
            "\n" +
            "\n" +
            "[#assign plugins = getPluginLayers(\n" +
            "        {}\n" +
            "    ) ]\n" +
            "\n" +
            "[#list plugins as plugin ]\n" +
            "[#list plugin as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#else]${value}[/#if]\n" +
            "[/#list]\n" +
            "[/#list]";
    private final String getEngineTemplate = "[#ftl]\n" +
            "[#assign regex=\"%s\"]\n" +
            "[#assign candidates =\n" +
            "  getPluginTree(\n" +
            "    \"/\",\n" +
            "    {\n" +
            "        \"Regex\" : regex,\n" +
            "        \"IgnoreDotDirectories\" : false,\n" +
            "        \"IgnoreDotFiles\" : false,\n" +
            "        \"IncludePluginInformation\" : true\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#list candidates as candidate ]\n" +
            "[#list candidate as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#elseif value?is_sequence]#is_sequence#[#else]${value}[/#if]\n" +
            "[#if property==\"Plugin\"]\n" +
            "Name : ${value.Name}\n" +
            "File : ${value.File}\n" +
            "[/#if]\n" +
            "[/#list]\n" +
            "[/#list]\n";
    private final String getEngineTemplate2 = "[#ftl]\n" +
            "[#assign regex=[\"test-aws.json\", \"test-azure.json\", \"test.json\"]]\n" +
            "[#assign candidates =\n" +
            "  getPluginTree(\n" +
            "    \"/\",\n" +
            "    {\n" +
            "        \"Regex\" : regex,\n" +
            "        \"IgnoreDotDirectories\" : false,\n" +
            "        \"IgnoreDotFiles\" : false,\n" +
            "        \"AddStartingWildcard\" : true,\n" +
            "        \"AddEndingWildcard\" : true,\n" +
            "        \"IncludePluginInformation\" : true\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#list candidates as candidate ]\n" +
            "[#list candidate as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#elseif value?is_sequence]#is_sequence#[#else]${value}[/#if]\n" +
            "[#if property==\"Plugin\"]\n" +
            "Name : ${value.Name}\n" +
            "File : ${value.File}\n" +
            "[/#if]\n" +
            "[/#list]\n" +
            "[/#list]\n";
    private final String getEngineTemplate3 = "[#ftl]\n" +
            "[#assign regex=[\"test.json\"]]\n" +
            "[#assign candidates =\n" +
            "  getPluginTree(\n" +
            "    \"/aws/test\",\n" +
            "    {\n" +
            "        \"Regex\" : regex,\n" +
            "        \"IgnoreDotDirectories\" : false,\n" +
            "        \"IgnoreDotFiles\" : false,\n" +
            "        \"IncludePluginInformation\" : true\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#list candidates as candidate ]\n" +
            "[#list candidate as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#elseif value?is_sequence]#is_sequence#[#else]${value}[/#if]\n" +
            "[#if property==\"Plugin\"]\n" +
            "Name : ${value.Name}\n" +
            "File : ${value.File}\n" +
            "[/#if]\n" +
            "[/#list]\n" +
            "[/#list]\n";
    private final String getEngineTemplateCaseSensitive = "[#ftl]\n" +
            "[#assign regex=[\"test.json\"]]\n" +
            "[#assign candidates =\n" +
            "  getPluginTree(\n" +
            "    \"/\",\n" +
            "    {\n" +
            "        \"Regex\" : regex,\n" +
            "        \"IgnoreDotDirectories\" : false,\n" +
            "        \"IgnoreDotFiles\" : false,\n" +
            "        \"IncludePluginInformation\" : true,\n" +
            "        \"CaseSensitive\" : true\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#list candidates as candidate ]\n" +
            "[#list candidate as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#elseif value?is_sequence]#is_sequence#[#else]${value}[/#if]\n" +
            "[#if property==\"Plugin\"]\n" +
            "Name : ${value.Name}\n" +
            "File : ${value.File}\n" +
            "[/#if]\n" +
            "[/#list]\n" +
            "[/#list]\n";
    private final String getEngineTemplate4 = "[#ftl]\n" +
            "[#assign regex=[\"test/test.json\"]]\n" +
            "[#assign candidates =\n" +
            "  getPluginTree(\n" +
            "    \"/path\",\n" +
            "    {\n" +
            "        \"Regex\" : regex,\n" +
            "        \"IgnoreDotDirectories\" : false,\n" +
            "        \"IgnoreDotFiles\" : false,\n" +
            "        \"IncludePluginInformation\" : true\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#list candidates as candidate ]\n" +
            "[#list candidate as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#elseif value?is_sequence]#is_sequence#[#else]${value}[/#if]\n" +
            "[#if property==\"Plugin\"]\n" +
            "Name : ${value.Name}\n" +
            "File : ${value.File}\n" +
            "[/#if]\n" +
            "[/#list]\n" +
            "[/#list]\n";
    private final String getEngineTemplate5 = "[#ftl]\n" +
            "[#assign regex=[\"provider.json\"]]\n" +
            "[#assign candidates =\n" +
            "  getPluginTree(\n" +
            "    \"/\",\n" +
            "    {\n" +
            "        \"Regex\" : regex,\n" +
            "        \"AddStartingWildcard\" : true,\n" +
            "        \"AddEndingWildcard\" : false,\n" +
            "        \"IncludePluginInformation\" : true\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#list candidates as candidate ]\n" +
            "[#list candidate as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#elseif value?is_sequence]#is_sequence#[#else]${value}[/#if]\n" +
            "[#if property==\"Plugin\"]\n" +
            "Name : ${value.Name}\n" +
            "File : ${value.File}\n" +
            "[/#if]\n" +
            "[/#list]\n" +
            "[/#list]\n";
    private final String getEngineTemplate6 = "[#ftl]\n" +
            "[#assign regex=[\"provider.json\"]]\n" +
            "[#assign candidates =\n" +
            "  getPluginTree(\n" +
            "    \"base\",\n" +
            "    {\n" +
            "        \"Regex\" : regex,\n" +
            "        \"AddStartingWildcard\" : true,\n" +
            "        \"AddEndingWildcard\" : false,\n" +
            "        \"MaxDepth\" : 3,\n" +
            "        \"MinDepth\" : 2,\n" +
            "        \"IncludePluginInformation\" : true\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#list candidates as candidate ]\n" +
            "[#list candidate as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#elseif value?is_sequence]#is_sequence#[#else]${value}[/#if]\n" +
            "[#if property==\"Plugin\"]\n" +
            "Name : ${value.Name}\n" +
            "File : ${value.File}\n" +
            "[/#if]\n" +
            "[/#list]\n" +
            "[/#list]\n";
    private final String getEngineTemplate7 = "[#ftl]\n" +
            "[#assign regex=[\"%s\"]]\n" +
            "[#assign candidates =\n" +
            "  getPluginTree(\n" +
            "    \"/\",\n" +
            "    {\n" +
            "        \"Regex\" : regex,\n" +
            "        \"AddStartingWildcard\" : true,\n" +
            "        \"AddEndingWildcard\" : false,\n" +
            "        \"MaxDepth\" : 3,\n" +
            "        \"MinDepth\" : 2,\n" +
            "        \"IgnoreDirectories\" : %s,\n" +
            "        \"IgnoreFiles\" : %s,\n" +
            "        \"IncludePluginInformation\" : true\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#list candidates as candidate ]\n" +
            "[#list candidate as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#elseif value?is_sequence]#is_sequence#[#else]${value}[/#if]\n" +
            "[#if property==\"Plugin\"]\n" +
            "Name : ${value.Name}\n" +
            "File : ${value.File}\n" +
            "[/#if]\n" +
            "[/#list]\n" +
            "[/#list]\n";
    private final String getEngineTemplate8 = "[#ftl]\n" +
            "[#assign candidates =\n" +
            "  getPluginTree(\n" +
            "    \"/\",\n" +
            "    {\n" +
            "        \"MaxDepth\" : 3,\n" +
            "        \"MinDepth\" : 2,\n" +
            "        \"IgnoreDirectories\" : %s,\n" +
            "        \"IgnoreFiles\" : %s,\n" +
            "        \"IncludePluginInformation\" : true\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#list candidates as candidate ]\n" +
            "[#list candidate as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#elseif value?is_sequence]#is_sequence#[#else]${value}[/#if]\n" +
            "[#if property==\"Plugin\"]\n" +
            "Name : ${value.Name}\n" +
            "File : ${value.File}\n" +
            "[/#if]\n" +
            "[/#list]\n" +
            "[/#list]\n";
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
    private final String getCMDBsAccountsTemplateMkdir = "{\n" +
            "  \"Version\": {\n" +
            "    \"Upgrade\": \"v1.3.2\",\n" +
            "    \"Cleanup\": \"v1.1.0\"\n" +
            "  },\n" +
            "  \"Layers\" : [\n" +
            "    {\n" +
            "      \"Name\" : \"api\",\n" +
            "      \"BasePath\" : \"products/api/config\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"Name\" : \"almv2\",\n" +
            "      \"BasePath\" : \"/products/almv2\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    private final String mkDirTemplate = "[#ftl]\n" +
            "\n" +
            "[#assign init =\n" +
            "  initialiseCMDBFileSystem({}) ]\n" +
            "[#assign candidates =\n" +
            "  mkdirCMDB(\n" +
            "    \"%s\",\n" +
            "    {\n" +
            "        \"Parents\" : %s,\n" +
            "        \"Sync\" : %s\n" +
            "    }\n" +
            "  ) ]\n" +
            "\n";
    private final String cpTemplate = "[#ftl]\n" +
            "\n" +
            "[#assign candidates =\n" +
            "  cpCMDB(\n" +
            "    \"%s\",\n" +
            "    \"%s\",\n" +
            "    {\n" +
            "        \"Recurse\" : %s,\n" +
            "        \"Preserve\" : %s,\n" +
            "        \"Sync\" : %s\n" +
            "    }\n" +
            "  ) ]\n" +
            "\n";
    private final String rmTemplate = "[#ftl]\n" +
            "\n" +
            "[#assign candidates =\n" +
            "  rmCMDB(\n" +
            "    \"%s\",\n" +
            "    {\n" +
            "        \"Recurse\" : %s,\n" +
            "        \"Force\" : %s,\n" +
            "        \"Sync\" : %s\n" +
            "    }\n" +
            "  ) ]\n" +
            "\n";
    private final String toTemplate = "[#ftl]\n" +
            "\n" +
            "[#assign content = { \"var\": \"value\"} ]\n" +
            "[#assign line = \"line2\n\" ]\n" +
            "[#assign list = [ \"var\", \"value\"] ]\n" +
            "[#assign candidates =\n" +
            "  toCMDB(\n" +
            "    \"%s\",\n" +
            "    %s,\n" +
            "    {\n" +
            "        \"Append\" : %s,\n" +
            "        \"Format\" : \"%s\",\n" +
            "        \"Sync\" : %s\n" +
            "    }\n" +
            "  ) ]\n" +
            "\n";
    private final String setExitStatus = "[#ftl]\n" +
            "\n" +
            "[#assign exit = setExitStatus(%s)]\n" +
            "\n";

    private final String stopException = "[#ftl]\n" +
            "\n" +
            "[#stop \"stop exception\"/]\n" +
            "\n";

    private final String toConsole = "[#ftl]\n" +
            "\n" +
            "[#assign content = { \"var\": \"value\"} ]\n" +
            "[#assign line = \"line2\n\" ]\n" +
            "[#assign list = [ \"var\", \"value\"] ]\n" +
            "[#assign candidates =\n" +
            "  toConsole(\n" +
            "    %s,\n" +
            "    {\n" +
            "        \"SendTo\" : \"%s\"\n" +
            "    }\n" +
            "  ) ]\n" +
            "\n";
    private final String toConsoleDefaultSendTo = "[#ftl]\n" +
            "\n" +
            "[#assign content = { \"var\": \"value\"} ]\n" +
            "[#assign line = \"line2\n\" ]\n" +
            "[#assign list = [ \"var\", \"value\"] ]\n" +
            "[#assign candidates =\n" +
            "  toConsole(\n" +
            "    %s\n" +
            "  ) ]\n" +
            "\n";
    private final String toTemplatePrettyPrint = "[#ftl]\n" +
            "\n" +
            "[#assign content = { \"var\": \"value\"} ]\n" +
            "[#assign line = \"line2\n\" ]\n" +
            "[#assign list = [ \"var\", \"value\"] ]\n" +
            "[#assign candidates =\n" +
            "  toCMDB(\n" +
            "    \"%s\",\n" +
            "    %s,\n" +
            "    {\n" +
            "        \"Append\" : %s,\n" +
            "        \"Format\" : \"%s\",\n" +
            "        \"Formatting\" : \"%s\",\n" +
            "        \"Indent\" : %s,\n" +
            "        \"Sync\" : %s\n" +
            "    }\n" +
            "  ) ]\n" +
            "\n";
    private final String toTemplatePrettyPrint2 = "[#ftl]\n" +
            "\n" +
            "[#assign content = { \"var\": \"value\"} ]\n" +
            "[#assign line = \"line2\n\" ]\n" +
            "[#assign list = [ \"var\", \"value\"] ]\n" +
            "[#assign candidates =\n" +
            "  toCMDB(\n" +
            "    \"%s\",\n" +
            "    %s,\n" +
            "    {\n" +
            "        \"Append\" : %s,\n" +
            "        \"Format\" : \"%s\",\n" +
            "        \"Formatting\" : \"%s\",\n" +
            "        \"Sync\" : %s\n" +
            "    }\n" +
            "  ) ]\n" +
            "\n";
    private final String getFileTreeAccountsTemplate = "[#ftl]\n" +
            "\n" +
            "[#assign regex=\".json\"]\n" +
            "[#assign candidates =\n" +
            "  getFileTree(\n" +
            "    \"products\",\n" +
            "    {\n" +
            "        \"Regex\" : regex,\n" +
            "        \"IgnoreDotDirectories\" : false,\n" +
            "        \"IgnoreDotFiles\" : false,\n" +
            "        \"MinDepth\" : 2,\n" +
            "        \"MaxDepth\" : 3,\n" +
            "\t\"IncludeCMDBInformation\" : true\t,\n" +
            "\t\"FilenameGlob\" : \"test.*\"\t,\n" +
            "\t\"UseCMDBPrefix\" : false\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#list candidates as candidate ]\n" +
            "[#list candidate as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#elseif value?is_sequence]#is_sequence#[#else]${value}[/#if]\n" +
            "[/#list]\n" +
            "[/#list]\n" +
            "\n";
    private final String getFileTreeAccountsTemplate2 = "[#ftl]\n" +
            "\n" +
            "[#assign regex=\".cmdb\"]\n" +
            "[#assign candidates =\n" +
            "  getFileTree(\n" +
            "    \"products/api\",\n" +
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
    private final String getFileTreeAccountsTemplate3 = "[#ftl]\n" +
            "\n" +
            "[#assign regex=\"match$\"]\n" +
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
    private final String getFileTreeAccountsTemplateMatchOptions = "[#ftl]\n" +
            "\n" +
            "[#assign regex=\"match$\"]\n" +
            "[#assign init =\n" +
            "  initialiseCMDBFileSystem(\n" +
            "    {\n" +
            "        \"IgnoreDotDirectories\" : false,\n" +
            "        \"IgnoreDotFiles\" : false,\n" +
            "        \"StopAfterFirstMatch\" : %s,\n" +
            "        \"IgnoreSubtreeAfterMatch\" : %s,\n" +
            "\t\"IncludeCMDBInformation\" : true\t,\n" +
            "\t\"UseCMDBPrefix\" : false\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#assign candidates =\n" +
            "  getFileTree(\n" +
            "    \"/\",\n" +
            "    {\n" +
            "        \"Regex\" : regex,\n" +
            "        \"IgnoreDotDirectories\" : false,\n" +
            "        \"IgnoreDotFiles\" : false,\n" +
            "        \"StopAfterFirstMatch\" : %s,\n" +
            "        \"IgnoreSubtreeAfterMatch\" : %s,\n" +
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
    private final String getFileTreeEffectiveRegex = "[#ftl]\n" +
            "\n" +
            "[#assign regex=\"almv2/path/.*/test/test.json\"]\n" +
            "[#assign candidates =\n" +
            "  getFileTree(\n" +
            "    \"products\",\n" +
            "    {\n" +
            "        \"Regex\" : regex,\n" +
            "        \"IgnoreDotDirectories\" : false,\n" +
            "        \"IgnoreDotFiles\" : false,\n" +
            "\t\"IncludeCMDBInformation\" : true\t,\n" +
            "\t\"UseCMDBPrefix\" : false,\n" +
            "\t\"AddStartingWildcard\" : false\n" +
            "    }\n" +
            "  ) ]\n" +
            "[#list candidates as candidate ]\n" +
            "[#list candidate as property,value ]\n" +
            "${property} : [#if (value?is_boolean || value?is_number)]${value?c}[#elseif value?is_hash]#hash#[#elseif value?is_sequence]#is_sequence#[#else]${value}[/#if]\n" +
            "[/#list]\n" +
            "[/#list]\n" +
            "\n";

    {
        cfg = new Configuration(freemarkerVersion);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.UK);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setObjectWrapper(new JsonValueWrapper(cfg.getIncompatibleImprovements()));
        cfg.setTemplateExceptionHandler(new WrapperTemplateExceptionHandler());
    }

    @Before
    public void before() {
        File templateDir = new File(templatesPath);
        if (!templateDir.exists()) templateDir.mkdirs();
        File cmdbsDir = new File(cmdbsPath);
        if (!cmdbsDir.exists()) cmdbsDir.mkdirs();

    }

    @After
    public void after() {
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
        File pluginsDir = new File(pluginsPath);
        if (pluginsDir.exists()) {
            try {
                FileUtils.deleteDirectory(pluginsDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void getPluginLayers() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getPluginLayers", new GetPluginLayersMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getPluginLayersTemplate).getBytes());
        createPlugin("aws");
        createPlugin("azure");

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/azure"), pluginsPath.concat("/aws")));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("Name : ");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(2, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
    }

    @Test
    public void getPluginTree() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (String.format(getEngineTemplate, "test.json")).getBytes());

        createFile(pluginsPath, "test/aws", "test.JSON", "{}");
        createFile(pluginsPath, "test/azure", "test.json", "{}");
        createFile(pluginsPath, "test/test", "test-1.json", "{}");

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test/aws"),
                pluginsPath.concat("/test/azure"),
                pluginsPath.concat("/test/test")));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("Name : aws");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("ContentsAsJSON : #hash#");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        cfg.clearSharedVariables();
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test/azure"),
                pluginsPath.concat("/test/aws"),
                pluginsPath.concat("/test/test")));

        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
        p = Pattern.compile("Name : aws");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test/test"),
                pluginsPath.concat("/test/azure"),
                pluginsPath.concat("/test/aws")));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : test");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
    }

    @Test
    public void getPluginTreeAfterCMDBInit() throws IOException, TemplateException {

        input = new HashMap<String, Object>();
        input.put("getFileTree", new GetCMDBTreeMethod());
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put("initialisePluginFileSystem", new InitPluginsMethod());
        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (String.format(getFileTreeAccountsTemplateMatchOptions, false, false, false, false)).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts/path/1/test", "test.json", content);
        createFile(cmdbsPath, "api/path/2/match", "test.json", "{}");
        createFile(cmdbsPath, "almv2/path/3/test", "match", "[#ftl]");
        createFile(cmdbsPath, "almv2/path/3/test/subdir", "match", "[#ftl]");
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "almv2", "api"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);


        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName2 = templatesPath.concat("/file2.ftl");
        Files.write(Paths.get(fileName2), (String.format(getEngineTemplate, "test.json")).getBytes());

        createFile(pluginsPath, "test/aws", "test.JSON", "{}");
        createFile(pluginsPath, "test/azure", "test.json", "{}");
        createFile(pluginsPath, "test/test", "test-1.json", "{}");

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test/aws"),
                pluginsPath.concat("/test/azure"),
                pluginsPath.concat("/test/test")));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate2 = cfg.getTemplate(fileName2);
        ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
        Writer consoleWriter2 = new OutputStreamWriter(byteArrayOutputStream2);
        freeMarkerTemplate2.process(input, consoleWriter2);
        String output = byteArrayOutputStream2.toString();
        Pattern p = Pattern.compile("Name : aws");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("ContentsAsJSON : #hash#");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

    }

    @Test
    public void getPluginTreeYaml() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (String.format(getEngineTemplate, "test.yaml")).getBytes());

        createFile(pluginsPath, "test/aws", "test.yaml", "firstName: Billy\n");
        createFile(pluginsPath, "test/azure", "test.yaml", "firstName Billy\n");
        createFile(pluginsPath, "test/test", "test-1.yaml", "{}");

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test/aws"),
                pluginsPath.concat("/test/azure"),
                pluginsPath.concat("/test/test")));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("Name : aws");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("ContentsAsJSON : #hash#");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        cfg.clearSharedVariables();
        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test/azure"),
                pluginsPath.concat("/test/aws"),
                pluginsPath.concat("/test/test")));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();
        p = Pattern.compile("Name : aws");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("ContentsAsJSON : #hash#");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test/test"),
                pluginsPath.concat("/test/azure"),
                pluginsPath.concat("/test/aws")));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : test");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
    }

    @Test
    public void getPluginTree2() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getEngineTemplate2).getBytes());

        createFile(pluginsPath, "test/aws", "test-aws.json", "{}");
        createFile(pluginsPath, "test/azure", "test-azure.json", "{}");
        createFile(pluginsPath, "test/test", "test.json", "{}");

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test/aws"),
                pluginsPath.concat("/test/azure"),
                pluginsPath.concat("/test/test")));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("Name : aws");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : test");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        cfg.clearSharedVariables();
        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test/aws"),
                pluginsPath.concat("/test/test")));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();

        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("Name : aws");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        cfg.clearSharedVariables();
        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test/test"),
                pluginsPath.concat("/test/azure")));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : aws");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

    }

    @Test
    public void getPluginTree3() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getEngineTemplate3).getBytes());

        createFile(pluginsPath, "aws/aws/test", "test.json", "{}");
        createFile(pluginsPath, "azure/azure/test", "test.json", "{}");
        createFile(pluginsPath, "test/test/test", "test.json", "{}");

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test"),
                pluginsPath.concat("/azure"),
                pluginsPath.concat("/aws/")));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("Name : aws");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("Name : test");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        /*input.put("pluginLayers", Arrays.asList(new String[]{
                pluginsPath.concat("/test/aws"),
                pluginsPath.concat("/test/test")
        }));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());

        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("Name : aws");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
        input.put("pluginLayers", Arrays.asList(new String[]{
                pluginsPath.concat("/test/test"),
                pluginsPath.concat("/test/azure"),
        }));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : aws");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());*/

    }

    @Test
    public void getPluginTreeCaseSensitive() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getEngineTemplateCaseSensitive).getBytes());

        createFile(pluginsPath, "aws/aws/test", "test.JSON", "{}");
        createFile(pluginsPath, "azure/azure/test", "test.json", "{}");
        createFile(pluginsPath, "test/test/test", "TEST.json", "{}");

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test"),
                pluginsPath.concat("/azure"),
                pluginsPath.concat("/aws/")));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("Name : aws");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : test");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        /*input.put("pluginLayers", Arrays.asList(new String[]{
                pluginsPath.concat("/test/aws"),
                pluginsPath.concat("/test/test")
        }));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());

        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("Name : aws");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
        input.put("pluginLayers", Arrays.asList(new String[]{
                pluginsPath.concat("/test/test"),
                pluginsPath.concat("/test/azure"),
        }));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : aws");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());*/

    }

    @Test
    public void getPluginTree4() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getEngineTemplate4).getBytes());

        createFile(pluginsPath, "aws/path/aws/test", "test.json", "{}");
        createFile(pluginsPath, "azure/path/azure/test", "test.json", "{}");
        createFile(pluginsPath, "test/path/test/test", "test.json", "{}");

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test"),
                pluginsPath.concat("/azure"),
                pluginsPath.concat("/aws/")));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("Name : aws");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : test");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        /*input.put("pluginLayers", Arrays.asList(new String[]{
                pluginsPath.concat("/test/aws"),
                pluginsPath.concat("/test/test")
        }));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());

        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("Name : aws");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
        input.put("pluginLayers", Arrays.asList(new String[]{
                pluginsPath.concat("/test/test"),
                pluginsPath.concat("/test/azure"),
        }));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : aws");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());*/

    }

    @Test
    public void getPluginTree5() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getEngineTemplate5).getBytes());

        createFile(pluginsPath, "aws/path/test", "provider.json", "{}");
        createFile(pluginsPath, "azure/path/test", "provider.json", "{}");
        createFile(pluginsPath, "test/test/test", "provider.json", "{}");

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test"),
                pluginsPath.concat("/azure"),
                pluginsPath.concat("/aws/")));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("Name : aws");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : test");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        cfg.clearSharedVariables();
        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test"),
                pluginsPath.concat("/aws/"),
                pluginsPath.concat("/azure")));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();

        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("Name : aws");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : test");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
        /*input.put("pluginLayers", Arrays.asList(new String[]{
                pluginsPath.concat("/test/test"),
                pluginsPath.concat("/test/azure"),
        }));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : aws");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());*/

    }

    @Test
    public void getPluginTree6() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getEngineTemplate6).getBytes());

        createFile(pluginsPath, "aws/base/", "provider.json", "{}");
        createFile(pluginsPath, "azure/base/path", "provider.json", "{}");
        createFile(pluginsPath, "test/base/test/test", "provider.json", "{}");

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test"),
                pluginsPath.concat("/azure"),
                pluginsPath.concat("/aws/")));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("Name : aws");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : test");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        /*input.put("pluginLayers", Arrays.asList(new String[]{
                pluginsPath.concat("/test/test"),
                pluginsPath.concat("/test/azure"),
        }));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = new String(byteArrayOutputStream.toByteArray());
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : aws");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());*/

    }

    @Test
    public void getPluginTree7() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (String.format(getEngineTemplate7, "provider.json", false, false)).getBytes());

        createFile(pluginsPath, "aws/base/", "provider.json", "{}");
        createFile(pluginsPath, "azure/base/path", "provider.json", "{}");
        createFile(pluginsPath, "test/base/test/test", "provider.json", "{}");

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test"),
                pluginsPath.concat("/azure"),
                pluginsPath.concat("/aws/")));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("Name : aws");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : azure");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("Name : test");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
    }

    @Test
    public void getPluginTree81() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (String.format(getEngineTemplate8, false, false)).getBytes());

        createFile(pluginsPath, "aws/base/", "provider.json", "{}");
        createFile(pluginsPath, "azure/base/path", "provider.json", "{}");
        createFile(pluginsPath, "test/base/test/test", "provider.json", "{}");

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test"),
                pluginsPath.concat("/azure"),
                pluginsPath.concat("/aws/")));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
        Pattern p = Pattern.compile("File : /tmp/gen3/plugins/test/base/test");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(2, count);
        p = Pattern.compile("File : /tmp/gen3/plugins/test/base/test/test");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("File : /tmp/gen3/plugins/azure/base/path");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(2, count);
        p = Pattern.compile("File : /tmp/gen3/plugins/azure/base/path/provider.json");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("File : /tmp/gen3/plugins/aws/base/provider.json\n");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);

    }

    @Test
    public void getPluginTree82() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (String.format(getEngineTemplate8, true, false)).getBytes());

        createFile(pluginsPath, "aws/base/", "provider.json", "{}");
        createFile(pluginsPath, "azure/base/path", "provider.json", "{}");
        createFile(pluginsPath, "test/base/test/test", "provider.json", "{}");

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test"),
                pluginsPath.concat("/azure"),
                pluginsPath.concat("/aws/")));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
        Pattern p = Pattern.compile("File : /tmp/gen3/plugins/test/base/test\n");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("File : /tmp/gen3/plugins/test/base/test/test\n");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("File : /tmp/gen3/plugins/azure/base/path\n");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("File : /tmp/gen3/plugins/azure/base/path/provider.json");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("File : /tmp/gen3/plugins/aws/base/provider.json\n");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);

    }

    @Test
    public void getPluginTree83() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (String.format(getEngineTemplate8, false, true)).getBytes());

        createFile(pluginsPath, "aws/base/", "provider.json", "{}");
        createFile(pluginsPath, "azure/base/path", "provider.json", "{}");
        createFile(pluginsPath, "test/base/test/test", "provider.json", "{}");

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test"),
                pluginsPath.concat("/azure"),
                pluginsPath.concat("/aws/")));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
        Pattern p = Pattern.compile("File : /tmp/gen3/plugins/test/base/test\n");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("File : /tmp/gen3/plugins/test/base/test/test\n");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("File : /tmp/gen3/plugins/azure/base/path\n");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("File : /tmp/gen3/plugins/azure/base/path/provider.json");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("File : /tmp/gen3/plugins/aws/base/provider.json\n");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);

    }

    @Test
    public void getPluginTree84() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getPluginTree", new GetPluginTreeMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (String.format(getEngineTemplate8, true, true)).getBytes());

        createFile(pluginsPath, "aws/base/", "provider.json", "{}");
        createFile(pluginsPath, "azure/base/path", "provider.json", "{}");
        createFile(pluginsPath, "test/base/test/test", "provider.json", "{}");

        input.put("pluginLayers", Arrays.asList(pluginsPath.concat("/test"),
                pluginsPath.concat("/azure"),
                pluginsPath.concat("/aws/")));

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
        Assert.assertEquals(0, output.length());

    }

    @Test
    public void getCMDBs() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getFileTree", new GetCMDBTreeMethod());
        input.put("getCMDBs", new GetCMDBsMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getCMDBsTemplateFileActiveOnly).getBytes());
        String content = getCMDBsAccountsTemplate;

        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");

        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);

        input.put("lookupDirs", Arrays.asList());
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("Name : ");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(3, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("lookupDirs", Arrays.asList());
        cmdbPathMapping.put("accounts", cmdbsPath.concat("/accounts"));
        cmdbPathMapping.put("almv2", cmdbsPath.concat("/almv2"));
        cmdbPathMapping.put("api", cmdbsPath.concat("/api"));
        input.put("cmdbPathMappings", cmdbPathMapping);
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(3, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

    }

    @Test
    public void getCMDBs2() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getFileTree", new GetCMDBTreeMethod());
        input.put("getCMDBs", new GetCMDBsMethod());

        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getCMDBsTemplateFileActiveOnly).getBytes());
        String content = getCMDBsAccountsTemplate;

        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");

        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);

        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api"));
        input.put("baseCMDB", "accounts");

        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("Name : ");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(2, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
    }

    @Test
    public void testMkDir() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put("mkdirCMDB", new MkdirCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileName), (String.format(mkDirTemplate, "state/cf/e5/default/baseline/default", "false", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "accoun", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/non-exist/products/new-product/another-dir"));
        Assert.assertFalse(fileInAccountsCMDB.exists());

        Files.write(Paths.get(fileName2), (String.format(mkDirTemplate, "/non-exist/products/new-product/another-dir", "true", "false")).getBytes());
        freeMarkerTemplate = cfg.getTemplate(fileName2);
        freeMarkerTemplate.process(input, consoleWriter);
        Assert.assertTrue(fileInAccountsCMDB.exists());

        input.put("CMDBNames", Arrays.asList("api", "almv2", "accounts"));
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInApiCMDB = new File(cmdbsPath.concat("/api").concat("/non-exist/products/new-product/another-dir"));
        Assert.assertFalse(fileInApiCMDB.exists());
        FileUtils.deleteDirectory(new File(cmdbsPath.concat("/accounts").concat("/non-exist")));
        freeMarkerTemplate.process(input, consoleWriter);
        Assert.assertTrue(fileInApiCMDB.exists());

        Files.write(Paths.get(fileName3), (String.format(mkDirTemplate, "/products/almv2/new-product/another-dir", "true", "false")).getBytes());
        File dirInAlmv2CMDB = new File(cmdbsPath.concat("/almv2").concat("/new-product/another-dir"));
        freeMarkerTemplate = cfg.getTemplate(fileName3);
        freeMarkerTemplate.process(input, consoleWriter);
        Assert.assertTrue(dirInAlmv2CMDB.exists());
    }

    @Test
    public void testCopy() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put(CpCMDBMethod.METHOD_NAME, new CpCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileName), (String.format(cpTemplate, "/products/almv2/dir/test.json", "/products/test.json", "false", "false", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "temp.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("api", "accounts", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/api").concat("/test.json"));
        Assert.assertTrue(fileInAccountsCMDB.exists());
    }

    @Test
    public void testCopy1() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put(CpCMDBMethod.METHOD_NAME, new CpCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileName), (String.format(cpTemplate, "/products/almv2/dir/test-*.json", "/products/almv2-copy/test.json", "false", "false", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/test.json"));
        Assert.assertFalse(fileInAccountsCMDB.exists());
    }

    @Test
    public void testCopy2() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put(CpCMDBMethod.METHOD_NAME, new CpCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileName), (String.format(cpTemplate, "/products/almv2/dir/test.json", "/products/almv2-copy/test.json", "false", "false", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "accounts/products/almv2-copy", "temp.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/test.json"));
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Assert.assertTrue(fileInAccountsCMDB.isFile());
    }

    @Test
    public void testRm1() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put("rmCMDB", new RmCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileName), (String.format(rmTemplate, "/products/almv2/dir", "true", "false", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "accounts/products/almv2-copy", "temp.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/almv2/dir"));
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        Assert.assertFalse(fileInAccountsCMDB.exists());
    }

    @Test
    public void testRm2() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put("rmCMDB", new RmCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileName), (String.format(rmTemplate, "/products/almv2/dir/test.json", "false", "false", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "accounts/products/almv2-copy", "temp.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        File fileInAccountsCMDB = new File(cmdbsPath.concat("/almv2/dir/test.json"));
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        Assert.assertFalse(fileInAccountsCMDB.exists());
        fileInAccountsCMDB = new File(cmdbsPath.concat("/almv2/dir/test-1.json"));
        Assert.assertTrue(fileInAccountsCMDB.exists());
    }

    @Test
    public void testRm3() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put("rmCMDB", new RmCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileName), (String.format(rmTemplate, "/products/almv2/dir", "false", "true", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "accounts/products/almv2-copy", "temp.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/almv2/dir/test.json"));
        Assert.assertFalse(fileInAccountsCMDB.exists());
        fileInAccountsCMDB = new File(cmdbsPath.concat("/almv2/dir/test-1.json"));
        Assert.assertFalse(fileInAccountsCMDB.exists());
    }

    @Test
    public void testCopy3() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put(CpCMDBMethod.METHOD_NAME, new CpCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileName), (String.format(cpTemplate, "/products/almv2/dir/test-*.json", "/products/almv2-copy/", "false", "false", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "accounts/products/almv2-copy", "temp.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/test-1.json"));
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Assert.assertTrue(fileInAccountsCMDB.isFile());
        fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/test-2.json"));
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Assert.assertTrue(fileInAccountsCMDB.isFile());
    }

    @Test
    public void testCopy4() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put(CpCMDBMethod.METHOD_NAME, new CpCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileName), (String.format(cpTemplate, "/products/almv2/dir/test-*.json", "/products/almv2-copy/temp.json", "false", "false", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "accounts/products/almv2-copy", "temp.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/test-1.json"));
        Assert.assertFalse(fileInAccountsCMDB.exists());
        Assert.assertFalse(fileInAccountsCMDB.isFile());
        fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/test-2.json"));
        Assert.assertFalse(fileInAccountsCMDB.exists());
        Assert.assertFalse(fileInAccountsCMDB.isFile());
    }

    @Test
    public void testCopy5() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put(CpCMDBMethod.METHOD_NAME, new CpCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileName), (String.format(cpTemplate, "/products/almv2/", "/products/almv2-copy", "false", "false", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "accounts/products/almv2-copy", "temp.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/dir/test-1.json"));
        Assert.assertFalse(fileInAccountsCMDB.exists());
        Assert.assertFalse(fileInAccountsCMDB.isFile());
        fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/dir/test-2.json"));
        Assert.assertFalse(fileInAccountsCMDB.exists());
        Assert.assertFalse(fileInAccountsCMDB.isFile());
        fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/.cmdb"));
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Assert.assertTrue(fileInAccountsCMDB.isFile());
    }

    @Test
    public void testCopy6() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put(CpCMDBMethod.METHOD_NAME, new CpCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileName), (String.format(cpTemplate, "/products/almv2/", "/products/almv2-copy/temp2.json", "true", "false", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "accounts/products/almv2-copy", "temp.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        cfg.setTemplateExceptionHandler(new WrapperTemplateExceptionHandler());
        cfg.setLogTemplateExceptions(false);
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/temp2.json/dir/test-1.json"));
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Assert.assertTrue(fileInAccountsCMDB.isFile());
        fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/temp2.json/dir/test-2.json"));
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Assert.assertTrue(fileInAccountsCMDB.isFile());
        fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/temp2.json/.cmdb"));
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Assert.assertTrue(fileInAccountsCMDB.isFile());
    }

    @Test
    public void testToCMDB() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put("toCMDB", new ToCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileName), (String.format(toTemplate, "/products/almv2-copy/temp.yml", "content", "false", "yml", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "accounts/products/almv2-copy", "temp.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/temp.yml"));
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Assert.assertTrue(fileInAccountsCMDB.isFile());
    }

    @Test
    public void testSetExitStatus() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put(SetExitStatusMethod.METHOD_NAME, new SetExitStatusMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String statusCode = "120";
        Files.write(Paths.get(fileName), (String.format(setExitStatus, statusCode)).getBytes());
        String fileNameExpected = templatesPath.concat("/file.txt");
        Files.write(Paths.get(fileNameExpected), "{var=value}".getBytes());
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        cfg.setTemplateExceptionHandler(new WrapperTemplateExceptionHandler());
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        Assert.assertTrue(statusCode.contentEquals(System.getProperty(StatusProcessor.existStatusVariableName)));
        System.clearProperty(StatusProcessor.existStatusVariableName);
    }

    @Test
    public void testStopException() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put(SetExitStatusMethod.METHOD_NAME, new SetExitStatusMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String statusCode = "100";
        Files.write(Paths.get(fileName), (String.format(stopException, statusCode)).getBytes());
        String fileNameExpected = templatesPath.concat("/file.txt");
        Files.write(Paths.get(fileNameExpected), "{var=value}".getBytes());
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        cfg.setTemplateExceptionHandler(new WrapperTemplateExceptionHandler());
        cfg.setLogTemplateExceptions(false);

        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        try {
            freeMarkerTemplate.process(input, consoleWriter);
        } catch (TemplateException ex) {
            if (ex instanceof StopException){
                String msg = ex.getMessage();
                System.err.print("Encountered stop instruction");
                if (msg != null && !msg.equals("")) {
                    System.err.println("\nCause given: " + msg);
                } else System.err.println();
            }
        }
    }

    @Test
    public void testToConsole() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("toConsole", new ToConsoleMethod());
        input.put(SetExitStatusMethod.METHOD_NAME, new SetExitStatusMethod());
        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (String.format(toConsole, "content", "stdout")).getBytes());
        String fileNameExpected = templatesPath.concat("/file.txt");
        Files.write(Paths.get(fileNameExpected), "{var=value}".getBytes());
        File fileExpected = new File(fileNameExpected);
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        cfg.setTemplateExceptionHandler(new WrapperTemplateExceptionHandler());
        cfg.setLogTemplateExceptions(false);
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        PrintStream ps_console = System.out;
        createFile(cmdbsPath, "out", "file.txt", "");
        File file = new File(cmdbsPath.concat("out").concat("file.txt"));
        FileOutputStream fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        System.setOut(ps);
            freeMarkerTemplate.process(input, consoleWriter);
        Assert.assertTrue(FileUtils.contentEquals(file, fileExpected));
        System.setOut(ps_console);

        if(System.getProperty(StatusProcessor.existStatusVariableName)!=null){
            System.exit(Integer.parseInt(System.getProperty(StatusProcessor.existStatusVariableName)));
        }
    }

    @Test
    public void testToConsoleStdErr() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("toConsole", new ToConsoleMethod());
        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (String.format(toConsole, "content", "stderr")).getBytes());
        String fileNameExpected = templatesPath.concat("/file.txt");
        Files.write(Paths.get(fileNameExpected), "{var=value}".getBytes());
        File fileExpected = new File(fileNameExpected);
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        PrintStream ps_console = System.err;
        createFile(cmdbsPath, "err", "file.txt", "");
        File file = new File(cmdbsPath.concat("err").concat("file.txt"));
        FileOutputStream fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        System.setErr(ps);

        freeMarkerTemplate.process(input, consoleWriter);
        Assert.assertTrue(FileUtils.contentEquals(file, fileExpected));
        System.setErr(ps_console);
    }

    @Test
    public void testToConsoleDefaultSendTo() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("toConsole", new ToConsoleMethod());
        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (String.format(toConsoleDefaultSendTo, "content")).getBytes());
        String fileNameExpected = templatesPath.concat("/file.txt");
        Files.write(Paths.get(fileNameExpected), "{var=value}".getBytes());
        File fileExpected = new File(fileNameExpected);
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        PrintStream ps_console = System.out;
        createFile(cmdbsPath, "out", "file.txt", "");
        File file = new File(cmdbsPath.concat("out").concat("file.txt"));
        FileOutputStream fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        System.setOut(ps);

        freeMarkerTemplate.process(input, consoleWriter);
        Assert.assertTrue(FileUtils.contentEquals(file, fileExpected));
        System.setOut(ps_console);
    }

    @Test
    public void testToCMDBPrettyPrint1() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put("toCMDB", new ToCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileNameExpected = templatesPath.concat("/temp.json");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileNameExpected), ("{\n" +
                "    \"a1\" : [\n" +
                "        \"v2\",\n" +
                "        \"v2\",\n" +
                "        \"v2\"\n" +
                "    ],\n" +
                "    \"p1\" : \"v1\"\n" +
                "}").getBytes());
        Files.write(Paths.get(fileName), (String.format(toTemplatePrettyPrint, "/products/almv2-copy/temp.json", "{\"p1\":\"v1\", \"a1\":[\"v2\",\"v2\",\"v2\"]}", "false", "json", "pretty", 4, "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "accounts/products/almv2-copy", "temp.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/temp.json"));
        File fileExpected = new File(fileNameExpected);
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Assert.assertTrue(fileInAccountsCMDB.isFile());
        Assert.assertTrue(FileUtils.contentEquals(fileInAccountsCMDB, fileExpected));
    }

    @Test
    public void testToCMDBPrettyPrint2() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put("toCMDB", new ToCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileNameExpected = templatesPath.concat("/temp.json");
        Files.write(Paths.get(fileNameExpected), ("{\n" +
                "  \"a1\" : [\n" +
                "    \"v2\",\n" +
                "    \"v2\",\n" +
                "    \"v2\"\n" +
                "  ],\n" +
                "  \"p1\" : \"v1\"\n" +
                "}").getBytes());
        Files.write(Paths.get(fileName), (String.format(toTemplatePrettyPrint2, "/products/almv2-copy/temp.json", "{\"p1\":\"v1\", \"a1\":[\"v2\",\"v2\",\"v2\"]}", "false", "json", "pretty", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "accounts/products/almv2-copy", "temp.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/temp.json"));
        File fileExpected = new File(fileNameExpected);
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Assert.assertTrue(fileInAccountsCMDB.isFile());
        Assert.assertTrue(FileUtils.contentEquals(fileInAccountsCMDB, fileExpected));
    }

    @Test
    public void testToCMDBPrettyPrint3() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put("toCMDB", new ToCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileNameExpected = templatesPath.concat("/temp.json");
        Files.write(Paths.get(fileNameExpected), ("{\"a1\":[\"v2\",\"v2\",\"v2\"],\"p1\":\"v1\"}").getBytes());
        Files.write(Paths.get(fileName), (String.format(toTemplate, "/products/almv2-copy/temp.json", "{\"p1\":\"v1\", \"a1\":[\"v2\",\"v2\",\"v2\"]}", "false", "json", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "accounts/products/almv2-copy", "temp.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/temp.json"));
        File fileExpected = new File(fileNameExpected);
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Assert.assertTrue(fileInAccountsCMDB.isFile());
        Assert.assertTrue(FileUtils.contentEquals(fileInAccountsCMDB, fileExpected));
    }

    @Test
    public void testToCMDB2() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put("toCMDB", new ToCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileName), (String.format(toTemplate, "/products/almv2-copy/temp.json", "content", "true", "json", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "accounts/products/almv2-copy", "temp.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/temp.json"));
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Assert.assertTrue(fileInAccountsCMDB.isFile());
    }

    @Test
    public void testToCMDB3() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put("toCMDB", new ToCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileName), (String.format(toTemplate, "/products/almv2-copy/temp.json", "list", "true", "json", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "accounts/products/almv2-copy", "temp.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/temp.json"));
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Assert.assertTrue(fileInAccountsCMDB.isFile());
    }

    @Test
    public void testToCMDB4() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put("toCMDB", new ToCMDBMethod());
        String fileName = templatesPath.concat("/file.ftl");
        String fileName2 = templatesPath.concat("/file2.ftl");
        String fileName3 = templatesPath.concat("/file3.ftl");
        Files.write(Paths.get(fileName), (String.format(toTemplate, "/products/almv2-copy/temp.txt", "line", "true", "text", "false")).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "accounts/products/almv2-copy", "temp.txt", "line1\n");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-1.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test-2.json", "{}");
        createFile(cmdbsPath, "api/another-dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        File fileInAccountsCMDB = new File(cmdbsPath.concat("/accounts").concat("/products/almv2-copy/temp.txt"));
        Assert.assertTrue(fileInAccountsCMDB.exists());
        Assert.assertTrue(fileInAccountsCMDB.isFile());
    }

    @Test
    public void testLookupDir() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getFileTree", new GetCMDBTreeMethod());
        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getFileTreeAccountsTemplate).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        createFile(cmdbsPath, "accounts/products", "test.json", "{}");
        createFile(cmdbsPath, "api", "test.json", "{}");
        createFile(cmdbsPath, "almv2/dir", "test.json", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("File : \\/products\\/test.json");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("CMDBNames", Arrays.asList("api", "almv2", "accounts"));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();
        p = Pattern.compile("File : \\/products\\/api\\/test.json");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("CMDBNames", Arrays.asList("almv2", "accounts", "api"));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();
        p = Pattern.compile("File : \\/products\\/almv2\\/dir\\/test.json");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("CMDBNames", Arrays.asList("almv2", "api"));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();
        p = Pattern.compile("File : \\/products\\/test.json");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
    }

    @Test
    public void testLookupDir2() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getFileTree", new GetCMDBTreeMethod());
        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getFileTreeAccountsTemplate2).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("File : \\/.cmdb");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("CMDBNames", Arrays.asList("api", "almv2", "accounts"));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();
        p = Pattern.compile("File : \\/products\\/api\\/.cmdb");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("CMDBNames", Arrays.asList("almv2", "accounts", "api"));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();
        p = Pattern.compile("File : \\/products\\/almv2\\/.cmdb");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("CMDBNames", Arrays.asList("almv2", "api"));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();
        p = Pattern.compile("File : \\/.cmdb");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
    }

    @Test
    public void testLookupDir3() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getFileTree", new GetCMDBTreeMethod());
        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getFileTreeEffectiveRegex).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts/path/1/test", "test.json", content);
        createFile(cmdbsPath, "api/path/2/test", "test.json", "{}");
        createFile(cmdbsPath, "almv2/path/3/test", "test.JSON", "{not a valid json}");
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("File : \\/.cmdb");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("ContentsAsJSON : #hash#");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("CMDBNames", Arrays.asList("api", "almv2", "accounts"));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();
        p = Pattern.compile("File : \\/products\\/api\\/path\\/2/\\/test\\/test.json");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
    }

    @Test
    public void testLookupDirFtl() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getFileTree", new GetCMDBTreeMethod());
        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (getFileTreeAccountsTemplate3).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts/path/1/test", "test.json", content);
        createFile(cmdbsPath, "api/path/2/match", "test.json", "{}");
        createFile(cmdbsPath, "almv2/path/3/test", "match", "\n   \n   \n\t  [#ftl] \n\n");
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("IsDirectory : true");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());

        input.put("CMDBNames", Arrays.asList("almv2", "accounts"));
        byteArrayOutputStream.reset();
        consoleWriter.flush();
        freeMarkerTemplate.process(input, consoleWriter);
        output = byteArrayOutputStream.toString();
        p = Pattern.compile("IsTemplate : true");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
    }

    @Test
    public void testLookupDirOptions1() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getFileTree", new GetCMDBTreeMethod());
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        input.put("initialisePluginFileSystem", new InitPluginsMethod());
        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (String.format(getFileTreeAccountsTemplateMatchOptions, false, false, false, false)).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts/path/1/test", "test.json", content);
        createFile(cmdbsPath, "api/path/2/match", "test.json", "{}");
        createFile(cmdbsPath, "almv2/path/3/test", "match", "[#ftl]");
        createFile(cmdbsPath, "almv2/path/3/test/subdir", "match", "[#ftl]");
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "almv2", "api"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("File : /products/almv2/path/3/test/match");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("File : /products/almv2/path/3/test/subdir/match");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("File : /products/api/path/2/match");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
        freeMarkerTemplate.process(input, consoleWriter);
    }

    @Test
    public void testLookupDirOptions2() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getFileTree", new GetCMDBTreeMethod());
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (String.format(getFileTreeAccountsTemplateMatchOptions, false, true, false, true)).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts/path/1/test", "test.json", content);
        createFile(cmdbsPath, "api/path/2/match", "test.json", "{}");
        createFile(cmdbsPath, "almv2/path/3/test", "match", "[#ftl]");
        createFile(cmdbsPath, "almv2/path/3/test/subdir", "match", "[#ftl]");
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "almv2", "api"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("File : /products/almv2/path/3/test/match");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        p = Pattern.compile("File : /products/almv2/path/3/test/subdir/match");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("File : /products/api/path/2/match");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
    }

    @Test
    public void testLookupDirOptions3() throws IOException, TemplateException {
        input = new HashMap<String, Object>();
        input.put("getFileTree", new GetCMDBTreeMethod());
        input.put("initialiseCMDBFileSystem", new InitCMDBsMethod());
        String fileName = templatesPath.concat("/file.ftl");
        Files.write(Paths.get(fileName), (String.format(getFileTreeAccountsTemplateMatchOptions, true, false, true, false)).getBytes());
        String content = getCMDBsAccountsTemplate;
        createFile(cmdbsPath, "accounts/path/1/test", "test.json", content);
        createFile(cmdbsPath, "api/path/2/match", "test.json", "{}");
        createFile(cmdbsPath, "almv2/path/3/test", "match", "[#ftl]");
        createFile(cmdbsPath, "almv2/path/3/test/subdir", "match", "[#ftl]");
        createFile(cmdbsPath, "accounts", ".cmdb", content);
        createFile(cmdbsPath, "api", ".cmdb", "{}");
        createFile(cmdbsPath, "almv2", ".cmdb", "{}");
        Map<String, String> cmdbPathMapping = new HashMap();
        input.put("cmdbPathMappings", cmdbPathMapping);
        input.put("lookupDirs", Arrays.asList(cmdbsPath));
        input.put("CMDBNames", Arrays.asList("accounts", "api", "almv2"));
        input.put("baseCMDB", "accounts");
        cfg.setTemplateLoader(new FileTemplateLoader(new File("/")));
        Template freeMarkerTemplate = cfg.getTemplate(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Writer consoleWriter = new OutputStreamWriter(byteArrayOutputStream);

        Environment env = freeMarkerTemplate.createProcessingEnvironment(input, consoleWriter);
        freeMarkerTemplate.process(input, consoleWriter);
        String output = byteArrayOutputStream.toString();
        Pattern p = Pattern.compile("File : /products/almv2/path/3/test/match");
        Matcher m = p.matcher(output);
        int count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("File : /products/almv2/path/3/test/subdir/match");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(0, count);
        p = Pattern.compile("File : /products/api/path/2/match");
        m = p.matcher(output);
        count = 0;
        while (m.find())
            count++;
        Assert.assertEquals(1, count);
        System.out.println("--------------------------- OUTPUT ---------------------------");
        System.out.write(output.getBytes());
    }

    private void createPlugin(String cmdbName) {
        File accountsDir = new File(pluginsPath.concat("/").concat(cmdbName));
        if (!accountsDir.exists()) accountsDir.mkdirs();
    }

    private void createFile(String path, String dirName, String fileName, String content) {
        File accountsDir = new File(path.concat("/").concat(dirName));
        if (!accountsDir.exists()) accountsDir.mkdirs();
        if (content != null) {
            String cmdbFile = path.concat("/").concat(dirName).concat("/".concat(fileName));
            try {
                Files.write(Paths.get(cmdbFile), content.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
