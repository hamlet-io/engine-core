package io.hamlet.freemarkerwrapper;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

public class IPAddressGetSubNetworksMethodTest {

    @Test
    void getSubNetworks() {
        String result = "[";
        String expectedResult = "[\"10.0.0.0/10\",\"10.64.0.0/10\",\"10.128.0.0/10\",\"10.192.0.0/10\"]";
        IPAddress subnet = new IPAddressString("10.0.0.0/8").getAddress();
        IPAddress newSubnets = subnet.setPrefixLength(Integer.valueOf("10"), false);
        Iterator<? extends IPAddress> iterator = newSubnets.prefixBlockIterator();
        while (iterator.hasNext()) {
            result = result.concat("\"".concat(iterator.next().toCanonicalString()).concat("\""));
            if(iterator.hasNext())
                result = result.concat(",");
            else
                result = result.concat("]");
        }
        Assertions.assertEquals(expectedResult, result);
    }
}
