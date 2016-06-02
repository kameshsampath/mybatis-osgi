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

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * @author kameshs
 */
public class MyBatisMapperRegistry {


    private static final Logger logger = LoggerFactory.getLogger(MyBatisMapperRegistry.class);

    private BundleContext bundleContext;

    private Set<Bundle> activeBundles = new HashSet();

    private Hashtable<String, Configuration> registeredConfigurations = new Hashtable<>();
    private ServiceTracker configurationServiceTracker;

    public MyBatisMapperRegistry(final BundleContext bundleContext) {

        this.bundleContext = bundleContext;

        //Open Service Tracker to grab the registered mybatis configurations

        configurationServiceTracker = new ServiceTracker(bundleContext, Configuration.class.getName(), new ServiceTrackerCustomizer() {
            @Override
            public Object addingService(ServiceReference serviceReference) {

                String configName = (String) serviceReference
                        .getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME);

                Configuration myBatisConfiguration = (Configuration) bundleContext.getService(serviceReference);

                if (!registeredConfigurations.containsKey(configName)) {
                    registeredConfigurations.put(configName, myBatisConfiguration);
                }

                logger.info("Adding Configuration {} to local cache ", configName);

                return myBatisConfiguration;
            }

            @Override
            public void modifiedService(ServiceReference serviceReference, Object o) {

            }

            @Override
            public void removedService(ServiceReference serviceReference, Object o) {

                String configName = (String) serviceReference
                        .getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME);

                if (registeredConfigurations.containsKey(configName)) {
                    registeredConfigurations.remove(configName);
                    logger.info("Removed Configuration {} from local cache ", configName);
                }
            }
        });

        configurationServiceTracker.open();
    }

    public void registerBundle(Bundle bundle) {

        Map<String, List<String>> dsMappers = ExtenderUtil.getMapperClauses(bundle);

        for (String dsName : dsMappers.keySet()) {

            Configuration configuration = registeredConfigurations.get(dsName);
            if (configuration != null) {

                List<String> packageNames = dsMappers.get(dsName);

                for (String packageName : packageNames) {

                    String cleanPackageName = packageName.trim();

                    logger.info("Adding mappers and xmls from  package {} to Configuration {}", cleanPackageName, dsName);

                    configuration.addMappers(cleanPackageName);

                    Enumeration<URL> mapperXmls = ExtenderUtil.findFilesFromBundle(bundle, packageName, "*.xml");

                    if (mapperXmls != null) {
                        while (mapperXmls.hasMoreElements()) {

                            URL mapperxmlUrl = mapperXmls.nextElement();

                            try {

                                String mappingFile = ExtenderUtil.mappingFileName(mapperxmlUrl.getFile());

                                logger.info("Adding Mapper XML {} to Config {} ", mappingFile, dsName);

                                InputStream in = mapperxmlUrl.openStream();

                                XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(in, configuration,
                                        mappingFile,
                                        configuration.getSqlFragments());
                                xmlMapperBuilder.parse();

                                in.close();

                            } catch (IOException e) {
                                logger.error("Unable to add mapper {} ", mapperxmlUrl.toString(), e);

                            }
                        }
                    }
                }
            }
        }

        logger.info("Registering Mapper Bundle {} ", bundle.getSymbolicName());

        activeBundles.add(bundle);

    }

    public void unregisterBundle(Bundle bundle) {

        logger.info("Unregistering Mapper Bundle {} ", bundle.getSymbolicName());

        activeBundles.remove(bundle);

        Map<String, List<String>> dsMappers = ExtenderUtil.getMapperClauses(bundle);

        for (String dsName : dsMappers.keySet()) {

            Configuration configuration = registeredConfigurations.get(dsName);

            if (configuration != null) {
                List<String> packageNames = dsMappers.get(dsName);
                for (String packageName : packageNames) {
                    logger.info("Removing mapped package {} from Configuration {}", packageName, dsName);
                    //FIXME how to remove the registered mappers
                }
            }
        }


    }

    public void close() {
        if (configurationServiceTracker != null) {
            configurationServiceTracker.close();
        }
    }


}
