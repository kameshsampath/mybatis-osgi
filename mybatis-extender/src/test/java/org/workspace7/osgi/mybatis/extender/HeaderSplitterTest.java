/*
 *
 *    Copyright (c) 2016. Kamesh Sampath.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *    implied.
 *
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package org.workspace7.osgi.mybatis.extender;

import aQute.bnd.build.model.BndEditModel;
import org.junit.Test;
import org.workspace7.osgi.mybatis.extender.impl.ExtenderUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * @author kameshs
 */
public class HeaderSplitterTest {

    private ExtenderUtil extenderUtil = ExtenderUtil.getInstance();


    private String[] getSplitHeaders(String bndFile) throws IOException {
        BndEditModel bndEditModel = new BndEditModel();
        bndEditModel.loadFrom(getClass().getResourceAsStream("/"+bndFile));

        List<String> properties = bndEditModel.getAllPropertyNames();
        assertNotNull(properties);

        String mapperValue = (String) bndEditModel.genericGet("MyBatis-Mappers");
        return extenderUtil.split(mapperValue, ",");
    }

    @Test
    public void testSplitSingleDS() throws Exception {
        String[] dsMappers = getSplitHeaders("test.bnd");
        assertNotNull(dsMappers);
        assertEquals(1, dsMappers.length);
    }

    @Test
    public void testSplitMultiDS() throws Exception {
        String[] dsMappers = getSplitHeaders("test2.bnd");
        assertNotNull(dsMappers);
        assertEquals(2, dsMappers.length);
    }

    @Test
    public void testDSAndMapper() throws Exception {
        String[] headerValue = getSplitHeaders("test.bnd");
        assertNotNull(headerValue);
        Map<String, List<String>> dsMappedValues = extenderUtil.mappersByDataSource(headerValue);
        assertNotNull(dsMappedValues);
        assertFalse(dsMappedValues.isEmpty());
        assertNotNull(dsMappedValues.get("demodbDS"));
        assertEquals(1, dsMappedValues.get("demodbDS").size());
        assertEquals("org.workspace7.mybatis.itests.mappers", dsMappedValues.get("demodbDS").get(0));
    }

    @Test
    public void testDSAndMapperMulti() throws Exception {
        String[] headerValue = getSplitHeaders("test2.bnd");
        assertNotNull(headerValue);
        Map<String, List<String>> dsMappedValues = extenderUtil.mappersByDataSource(headerValue);
        assertNotNull(dsMappedValues);
        assertFalse(dsMappedValues.isEmpty());
        assertNotNull(dsMappedValues.get("demodbDS"));
        assertEquals(1, dsMappedValues.get("demodbDS").size());
        assertEquals("org.workspace7.mybatis.demo.mappers", dsMappedValues.get("demodbDS").get(0));
        assertEquals(1, dsMappedValues.get("persondbDS").size());
        assertEquals("org.workspace7.mybatis.person.mappers", dsMappedValues.get("persondbDS").get(0));
    }

    @Test
    public void testDSAndMapperMultiSameDS() throws Exception {
        String[] headerValue = getSplitHeaders("test4.bnd");
        assertNotNull(headerValue);
        Map<String, List<String>> dsMappedValues = extenderUtil.mappersByDataSource(headerValue);
        assertNotNull(dsMappedValues);
        assertFalse(dsMappedValues.isEmpty());
        assertEquals(2, dsMappedValues.size());
        assertNotNull(dsMappedValues.get("demodbDS"));
        assertEquals(2, dsMappedValues.get("demodbDS").size());
        assertEquals("org.workspace7.mybatis.demo.mappers", dsMappedValues.get("demodbDS").get(0));
        assertEquals("org.workspace7.mybatis.demo.mappers", dsMappedValues.get("demodbDS").get(1));
        assertNotNull(dsMappedValues.get("persondbDS"));
        assertEquals(2, dsMappedValues.get("persondbDS").size());
        assertEquals("org.workspace7.mybatis.person.mappers", dsMappedValues.get("persondbDS").get(0));
        assertEquals("org.workspace7.mybatis.person.mappers", dsMappedValues.get("persondbDS").get(1));
    }
}
