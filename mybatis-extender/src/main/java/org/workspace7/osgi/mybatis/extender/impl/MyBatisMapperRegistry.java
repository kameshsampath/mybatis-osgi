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
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
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

    private Hashtable<String, Environment> registeredEnvironments = new Hashtable<>();
    private Hashtable<String, ServiceRegistration> registeredSqlSessionFactories = new Hashtable<>();
    private ServiceTracker environmentServiceTracker;

    public MyBatisMapperRegistry(final BundleContext bundleContext) {

        this.bundleContext = bundleContext;

        //Open Service Tracker to grab the registered mybatis environments

        environmentServiceTracker = new ServiceTracker(bundleContext, Environment.class.getName(), new ServiceTrackerCustomizer() {
            @Override
            public Object addingService(ServiceReference serviceReference) {

                String configName = (String) serviceReference
                        .getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME);

                Environment myBatisEnv = (Environment) bundleContext.getService(serviceReference);

                if (!registeredEnvironments.containsKey(configName)) {
                    registeredEnvironments.put(configName, myBatisEnv);
                }

                logger.info("Adding Environment {} to local cache ", configName);

                return myBatisEnv;
            }

            @Override
            public void modifiedService(ServiceReference serviceReference, Object o) {

            }

            @Override
            public void removedService(ServiceReference serviceReference, Object o) {

                String configName = (String) serviceReference
                        .getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME);

                if (registeredEnvironments.containsKey(configName)) {
                    registeredEnvironments.remove(configName);
                    logger.info("Removed Configuration {} from local cache ", configName);
                }
            }
        });

        environmentServiceTracker.open();
    }

    public void registerBundle(Bundle bundle) {

        Map<String, List<String>> dsMappers = ExtenderUtil.getMapperClauses(bundle);

        for (String dsName : dsMappers.keySet()) {

            Environment environment = registeredEnvironments.get(dsName);

            Configuration configuration = new Configuration(environment);

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

                                Collection<String> mappedStatements = configuration.getMappedStatementNames();
                                for (String mappedStmt : mappedStatements) {
                                    logger.info(" Added Mapped Statement: {}", mappedStmt);
                                }

                                unregisterSqlSessionFactory(dsName);

                                SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
                                Dictionary<String, String> props = new Hashtable<>();
                                props.put("dataSourceName", dsName);
                                ServiceRegistration serviceRegistration = bundleContext.
                                        registerService(SqlSessionFactory.class.getName(), sqlSessionFactory, props);

                                registeredSqlSessionFactories.put(dsName, serviceRegistration);

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

        logger.info("UnRegistering Mapper Bundle {} ", bundle.getSymbolicName());

        activeBundles.remove(bundle);

        Map<String, List<String>> dsMappers = ExtenderUtil.getMapperClauses(bundle);

        for (String dsName : dsMappers.keySet()) {

            unregisterSqlSessionFactory(dsName);
        }


    }

    public void close() {
        if (environmentServiceTracker != null) {
            environmentServiceTracker.close();
        }
    }

    private void unregisterSqlSessionFactory(String dsName) {
        if (registeredSqlSessionFactories.containsKey(dsName)) {
            logger.info("Unregistering SqlSession Factory {}:", dsName);
            ServiceRegistration serviceRegistration = registeredSqlSessionFactories.get(dsName);
            serviceRegistration.unregister();
            registeredSqlSessionFactories.remove(dsName);
        }
    }


}
