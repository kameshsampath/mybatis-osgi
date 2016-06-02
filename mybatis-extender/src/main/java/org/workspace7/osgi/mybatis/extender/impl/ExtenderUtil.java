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

package org.workspace7.osgi.mybatis.extender.impl;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

/**
 * @author kameshs
 */
public class ExtenderUtil {

    static final String MYBATIS_MAPPER_HEADER = "MyBatis-Mappers";
    static final ExtenderUtil INSTANCE = new ExtenderUtil();
    private static final Logger logger = LoggerFactory.getLogger(ExtenderUtil.class);

    private ExtenderUtil() {
    }

    public static ExtenderUtil getInstance() {
        return INSTANCE;
    }

    static Map<String, List<String>> getMapperClauses(Bundle bundle) {

        Map<String, List<String>> mapperClauses = new HashMap<>();

        String mapperHeader = bundle.getHeaders().get(MYBATIS_MAPPER_HEADER);

        if (mapperHeader != null) {
            String[] mybatisHeaderValue = mapperHeader.split(",");
            return ExtenderUtil.getInstance().mappersByDataSource(mybatisHeaderValue);
        }

        return mapperClauses;
    }

    static Enumeration<URL> findFilesFromBundle(Bundle bundle, String packageName, String fileFilter) {
        String pkgLoc = packageName.replace(".", "/");
        return bundle.findEntries(pkgLoc, fileFilter, true);
    }

    static String mappingFileName(String fullFileNamePath) {

        int idxLastPath = fullFileNamePath.lastIndexOf("/");

        return fullFileNamePath.substring(idxLastPath + 1);
    }

    public String[] split(String headerValue, String delimeter) {

        if (delimeter == null) {
            delimeter = ",";
        }

        if (headerValue != null)
            return headerValue.split(delimeter.trim());

        return null;
    }

    public Map<String, List<String>> mappersByDataSource(String[] dsMapperValues) {

        NavigableMap<String, List<String>> dsMappers = new TreeMap<>();


        for (int i = 0; i < dsMapperValues.length; i++) {

            if (dsMapperValues[i].contains("=")) {

                String[] splitStr = split(dsMapperValues[i], "=");

                String dsName = splitStr[0];

                String[] splitMappers = split(splitStr[1], ",");

                dsMappers.put(dsName, Arrays.asList(splitMappers));
            } else {
                //Case where the value itself is comma separated values
                String[] splitStr = split(dsMapperValues[i], ",");
                ArrayList<String> values = new ArrayList(dsMappers.lastEntry().getValue());
                values.addAll(Arrays.asList(splitStr));
                String key = dsMappers.lastEntry().getKey();
                dsMappers.remove(key);
                dsMappers.put(key, values);
            }


        }

        logger.debug("Split Header Map " + dsMappers);

        return dsMappers;
    }

}
