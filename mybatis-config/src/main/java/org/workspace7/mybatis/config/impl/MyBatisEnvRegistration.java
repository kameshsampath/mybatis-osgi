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

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.transaction.TransactionFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @author kameshs
 */
public class MyBatisEnvRegistration implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(MyBatisEnvRegistration.class);

    private ServiceRegistration envServiceReg;


    public MyBatisEnvRegistration(BundleContext bundleContext,
                                  TransactionFactory transactionFactory, ServiceReference dsReference) {

        if (dsReference != null) {

            DataSource ds = (DataSource) bundleContext.getService(dsReference);
            String dataSourceName = (String) dsReference.getProperty(DataSourceFactory.JDBC_DATASOURCE_NAME);


            Dictionary<String, String> serviceProps = new Hashtable<>();
            serviceProps.put("dataSourceName", dataSourceName);

            Environment myBatisEnv = new Environment(dataSourceName, transactionFactory, ds);

            envServiceReg = bundleContext.registerService(Environment.class.getName(),
                    myBatisEnv, serviceProps);

            logger.info("Created and registered MyBatis Environment for Datasource {}", dataSourceName);


        }
    }

    @Override
    public void close() {

        if (envServiceReg != null) {
            envServiceReg.unregister();
        }
    }
}
