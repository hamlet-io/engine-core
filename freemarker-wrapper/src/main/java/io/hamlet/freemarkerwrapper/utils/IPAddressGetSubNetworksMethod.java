package io.hamlet.freemarkerwrapper.utils;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Kseniya on 10/04/2018.
 */
public class IPAddressGetSubNetworksMethod implements TemplateMethodModelEx {

    public static String METHOD_NAME = "IPAddress__getSubNetworks";
    /**
     * IPAddress function
     *
     * @param args ipAddress and prefixLength
     * @return a json array string, ?eval needs to be called in a template
     * @throws TemplateModelException
     */
    public TemplateModel exec(List args) throws TemplateModelException {
        if (args.size() != 2) {
            throw new TemplateModelException("Wrong arguments");
        }
        String ipAddress = args.get(0).toString();
        String prefixLength = args.get(1).toString();
        IPAddress subnet = new IPAddressString(ipAddress).getAddress();
        IPAddress newSubnets = subnet.setPrefixLength(Integer.valueOf(prefixLength), false);
        String result = "[";
        Iterator<? extends IPAddress> iterator = newSubnets.prefixBlockIterator();
        while (iterator.hasNext()) {
            result = result.concat("\"".concat(iterator.next().toCanonicalString()).concat("\""));
            if (iterator.hasNext())
                result = result.concat(",");
            else
                result = result.concat("]");
        }
        return new SimpleScalar(result);
    }
}