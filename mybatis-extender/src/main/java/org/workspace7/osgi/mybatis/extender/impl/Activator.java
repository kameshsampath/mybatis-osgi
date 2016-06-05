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
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.BundleTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kameshs
 */
public class Activator implements BundleActivator {

    private static final String FACTORY_PID = "org.workspace7.mybatis";

    private BundleTracker mapperBundleTracker;

    private MyBatisMapperRegistry myBatisMapperRegistry;

    @Override
    public void start(BundleContext bundleContext) throws Exception {

        myBatisMapperRegistry = new MyBatisMapperRegistry(bundleContext);

        mapperBundleTracker = new BundleTracker(bundleContext,
                Bundle.ACTIVE | Bundle.STARTING, new MapperBundleTrackerCustomizer(myBatisMapperRegistry));

        mapperBundleTracker.open();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        myBatisMapperRegistry.close();
        mapperBundleTracker.close();
    }

}
