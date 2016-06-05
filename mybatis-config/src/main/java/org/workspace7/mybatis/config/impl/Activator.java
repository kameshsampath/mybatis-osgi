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

package org.workspace7.mybatis.config.impl;

import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @author kameshs
 */
public class Activator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration transactionFactorySvcReg;
    private ServiceTracker dsServiceTracker;
    private TransactionFactory transactionFactory;

    @Override
    public void start(final BundleContext bundleContext) throws Exception {

        logger.debug("Activating mybatis-config");

        ServiceReference[] ibatisTransactionFactoryRefs = bundleContext.
                getAllServiceReferences(TransactionFactory.class.getName(), null);


        //Register JDBC Transaction Factory as default when no other service refrences are found
        if (ibatisTransactionFactoryRefs == null || ibatisTransactionFactoryRefs.length == 0) {
            Dictionary<String, String> props = new Hashtable<>();
            props.put("name", "jdbc");
            logger.info("No MyBatis Transaction Factory found registering default JDBC one");
            transactionFactory = new JdbcTransactionFactory();
            transactionFactorySvcReg = bundleContext.registerService(TransactionFactory.class.getName(),
                    transactionFactory, props);
        } else {
            //TODO Assign exisiting reference
        }

        Filter dsFilter = bundleContext.createFilter("(objectClass=" + DataSource.class.getName() + ")");

        dsServiceTracker = new DataSourceServiceTracker(bundleContext, dsFilter, transactionFactory);

        dsServiceTracker.open();


    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (transactionFactorySvcReg != null) {
            transactionFactorySvcReg.unregister();
            transactionFactorySvcReg = null;
        }

        dsServiceTracker.close();
        dsServiceTracker = null;
    }


    private class MyBatisEnvConfigCustomizer implements ServiceTrackerCustomizer<DataSource, MyBatisEnvRegistration> {

        private BundleContext bundleContext;

        public MyBatisEnvConfigCustomizer(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }

        @Override
        public MyBatisEnvRegistration addingService(ServiceReference<DataSource> serviceReference) {
            logger.info("Adding MyBatis Env and Config for datasource {}",
                    serviceReference.getProperty("dataSourceName"));
            return new MyBatisEnvRegistration(bundleContext, transactionFactory, serviceReference);
        }

        @Override
        public void modifiedService(ServiceReference<DataSource> serviceReference,
                                    MyBatisEnvRegistration myBatisEnvRegistration) {
            removedService(serviceReference, myBatisEnvRegistration);
            addingService(serviceReference);
        }

        @Override
        public void removedService(ServiceReference<DataSource> serviceReference,
                                   MyBatisEnvRegistration myBatisEnvRegistration) {
            logger.info("Removing MyBatis Env and Config for datasource {}",
                    serviceReference.getProperty("dataSourceName"));
            myBatisEnvRegistration.close();

        }
    }

}
