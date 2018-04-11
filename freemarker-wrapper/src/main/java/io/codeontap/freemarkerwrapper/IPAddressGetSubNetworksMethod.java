package io.codeontap.freemarkerwrapper;

import freemarker.template.*;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Kseniya on 10/04/2018.
 */
public class IPAddressGetSubNetworksMethod implements TemplateMethodModelEx {

    public TemplateModel exec(List args) throws TemplateModelException {
        if (args.size() != 2) {
            throw new TemplateModelException("Wrong arguments");
        }
        String ipAddress = args.get(0).toString();
        String prefixLength = args.get(1).toString();
        IPAddress subnet = new IPAddressString(ipAddress).getAddress();
        IPAddress newSubnets = subnet.setPrefixLength(Integer.valueOf(prefixLength), false);
        TreeSet<IPAddress> subnetSet = new TreeSet<IPAddress>();
        Iterator<? extends IPAddress> iterator = newSubnets.prefixBlockIterator();
        while (iterator.hasNext()) {
            subnetSet.add(iterator.next());
        }
        //return subnetSet;
        return new SimpleSequence(subnetSet, null);
    }
}