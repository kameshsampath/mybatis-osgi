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
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import java.util.Hashtable;

/**
 * @author kameshs
 */
public class DataSourceServiceTracker extends ServiceTracker {

    private final TransactionFactory transactionFactory;


    private Hashtable<String, MyBatisEnvRegistration> mybatisEnvConfigSvcRegs = new Hashtable<>();

    public DataSourceServiceTracker(BundleContext context, Filter filter, TransactionFactory transactionFactory) {
        super(context, filter, null);
        this.transactionFactory = transactionFactory;
    }

    @Override
    public Object addingService(ServiceReference reference) {

        String dataSourceName = (String) reference.getProperty("dataSourceName");

        MyBatisEnvRegistration myBatisEnvRegistration = null;

        if (!mybatisEnvConfigSvcRegs.containsKey(dataSourceName)) {
            myBatisEnvRegistration =
                    new MyBatisEnvRegistration(context, transactionFactory, reference);
            mybatisEnvConfigSvcRegs.put(dataSourceName, myBatisEnvRegistration);
        }

        return myBatisEnvRegistration;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        removedService(reference, service);
        addingService(reference);
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        String dataSourceName = (String) reference.getProperty("dataSourceName");

        MyBatisEnvRegistration myBatisEnvRegistration = (MyBatisEnvRegistration) service;
        if (mybatisEnvConfigSvcRegs.containsKey(dataSourceName)) {
            mybatisEnvConfigSvcRegs.put(dataSourceName, myBatisEnvRegistration);
        }

        myBatisEnvRegistration.close();
    }
}
