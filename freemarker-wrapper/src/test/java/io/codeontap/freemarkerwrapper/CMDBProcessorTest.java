package io.codeontap.freemarkerwrapper;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class CMDBProcessorTest  {
    @Test
    public void testGetFileTree() throws IOException, RunFreeMarkerException {
        CMDBProcessor cmdbProcessor = new CMDBProcessor();
        cmdbProcessor.getFileTree(null, null, Arrays.asList(new String[]{"/"}),
                "", null, Arrays.asList( new String[]{"*.json", "*.ftl"}),true, false, false, false);
    }
}
