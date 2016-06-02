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

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
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
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;

/**
 * @author kameshs
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
//FIXME some error is thrown while contianer shutdown -- check
public class MyBatisExtenderIT extends KarafTestSupport {

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

        Option[] bundleOptions = options(
                //Application Features
                features(mybatisOSGiFeaturesRepo, "mybatis-config", "mybatis-extender"),
                //Mapper bundle used to test MyBatis-Extender
                mavenBundle("org.workspace7.osgi.mybatis", "mybatis-itests-mappers", "0.1.0")
        );

        return combine(containerOpts, bundleOptions);
    }

    @Test
    public void testExtender() throws SQLException {

        try {
            createAndDropH2Table(demodbDS);
            ServiceReference[] mybatisConfigRefs = bundleContext.getServiceReferences(Configuration.class.getName(), "(dataSourceName=demodbDS)");
            assertNotNull(mybatisConfigRefs);
            assertEquals(1, mybatisConfigRefs.length);
            Configuration mybatisConfig = (Configuration) bundleContext.getService(mybatisConfigRefs[0]);
            assertNotNull(mybatisConfig);
            boolean hasStatement = mybatisConfig.hasStatement("selectAllPersons");
            assertTrue(hasStatement);
        } catch (InvalidSyntaxException e) {
            fail(e.getMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSqlSessionQuery() throws SQLException {
        try {
            createAndDropH2Table(demodbDS);
            ServiceReference[] mybatisConfigRefs = bundleContext.getServiceReferences(Configuration.class.getName(), "(dataSourceName=demodbDS)");
            assertNotNull(mybatisConfigRefs);
            assertEquals(1, mybatisConfigRefs.length);
            Configuration mybatisConfig = (Configuration) bundleContext.getService(mybatisConfigRefs[0]);
            assertNotNull(mybatisConfig);
            boolean hasStatement = mybatisConfig.hasStatement("selectAllPersons");
            assertTrue(hasStatement);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisConfig);
            List<Object> listOfPersons = sqlSessionFactory.openSession().selectList("selectAllPersons");
            assertFalse(listOfPersons.isEmpty());
            assertEquals(4, listOfPersons.size());

        } catch (InvalidSyntaxException e) {
            fail(e.getMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
