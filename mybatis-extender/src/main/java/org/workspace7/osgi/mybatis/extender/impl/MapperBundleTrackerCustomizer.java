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
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import java.util.List;
import java.util.Map;

/**
 * @author kameshs
 */
public class MapperBundleTrackerCustomizer implements BundleTrackerCustomizer {

    private final MyBatisMapperRegistry myBatisMapperRegistry;


    public MapperBundleTrackerCustomizer(MyBatisMapperRegistry myBatisMapperRegistry) {
        this.myBatisMapperRegistry = myBatisMapperRegistry;
    }

    @Override
    public Object addingBundle(Bundle bundle, BundleEvent bundleEvent) {
        Map<String, List<String>> mybatisMappers = ExtenderUtil.getMapperClauses(bundle);
        if (!mybatisMappers.isEmpty()) {
            myBatisMapperRegistry.registerBundle(bundle);
            return bundle;
        }
        return null;
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, Object o) {
        //This is done to ensure if any new mappers are added or removed
        myBatisMapperRegistry.unregisterBundle(bundle);
        myBatisMapperRegistry.registerBundle(bundle);
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent bundleEvent, Object o) {
        myBatisMapperRegistry.unregisterBundle(bundle);
    }
}
