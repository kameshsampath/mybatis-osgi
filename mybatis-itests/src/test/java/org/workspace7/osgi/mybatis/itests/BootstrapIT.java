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

package org.workspace7.osgi.mybatis.itests;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;

/**
 * @author kameshs
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class BootstrapIT extends KarafTestSupport {

    @Inject
    @Filter("(dataSourceName=demodbDS)")
    DataSource demodbDS;

    @Inject
    BundleContext bundleContext;

    MavenUrlReference mybatisOSGiFeaturesRepo = maven()
            .groupId("org.workspace7.osgi.mybatis")
            .artifactId("mybatis-osgi-features")
            .classifier("features")
            .type("xml")
            .version("0.1.0");

    @org.ops4j.pax.exam.Configuration
    public Option[] config() {

        Option[] containerOpts = setupContainer();

        Option[] projectBundles = options(
                //Application Features
                features(mybatisOSGiFeaturesRepo, "mybatis-config", "mybatis-extender")
        );

        return combine(containerOpts, projectBundles);
    }

    @Before
    public void setupDB() throws Exception {
        createAndDropH2Table(demodbDS);
    }

    @Test
    public void testDataSource() throws SQLException {
        assertNotNull(demodbDS);
        Connection dbCon = demodbDS.getConnection();
        Statement st = dbCon.createStatement();
        boolean isExecuted = st.execute("SELECT 1 ");
        assertTrue(isExecuted);
    }


    @Test
    public void testMyBatisEnv() throws SQLException {
        try {
            ServiceReference[] mybatisEnvSRefs = bundleContext.getServiceReferences(Environment.class.getName(), "(dataSourceName=demodbDS)");
            assertNotNull(mybatisEnvSRefs);
            assertEquals(1, mybatisEnvSRefs.length);
            Environment mybatisEnv = (Environment) bundleContext.getService(mybatisEnvSRefs[0]);
            assertNotNull(mybatisEnv);
        } catch (InvalidSyntaxException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testMyBatisConfig() throws SQLException {
        try {
            ServiceReference[] mybatisConfigRefs = bundleContext.getServiceReferences(Configuration.class.getName(), "(dataSourceName=demodbDS)");
            assertNotNull(mybatisConfigRefs);
            assertEquals(1, mybatisConfigRefs.length);
            Configuration mybatisConfigRef = (Configuration) bundleContext.getService(mybatisConfigRefs[0]);
            assertNotNull(mybatisConfigRef);
        } catch (InvalidSyntaxException e) {
            fail(e.getMessage());
        }
    }

    //FIXME - Add service unregister tests

}
