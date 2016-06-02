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

import org.apache.ibatis.jdbc.ScriptRunner;
import org.h2.jdbcx.JdbcDataSource;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

/**
 * @author kameshs
 */
public class KarafTestSupport {

    static final String KARAF_GROUP_ID = "org.apache.karaf";
    static final String KARAF_ARTIFACT_ID = "apache-karaf";
    static final String KARAF_VERSION = "2.4.0";
    static final String PAXJDBC_FEATURES_GROUP_ID = "org.ops4j.pax.jdbc";
    static final String PAXJDBC_FEATURE = "pax-jdbc-features";
    static final String PAX_JDBC_VERSION = "0.8.0";

    protected Option[] setupContainer() {
        MavenArtifactUrlReference karafUrl = maven()
                .groupId(KARAF_GROUP_ID)
                .artifactId(KARAF_ARTIFACT_ID)
                .version(KARAF_VERSION)
                .type("zip");

        MavenUrlReference paxJdbcFeaturesRepo = maven()
                .groupId(PAXJDBC_FEATURES_GROUP_ID)
                .artifactId(PAXJDBC_FEATURE)
                .classifier("features")
                .type("xml")
                .version(PAX_JDBC_VERSION);
        //.versionAsInProject(); //this is not working ???

        File jdbcConfigFile = new File("src/test/resources/demodbDS.properties");

        //Setup pax-jdbc datasource
        Option[] configOptions = editConfigurationFilePut("etc/org.ops4j.datasource-demodbDS.cfg", jdbcConfigFile);

        Option[] baseOptions = options(karafDistributionConfiguration()
                        .frameworkUrl(karafUrl)
                        .unpackDirectory(new File("target/exam"))
                        .useDeployFolder(false)
                        .karafVersion(KARAF_VERSION)
                        .name("Apache Karaf"),
                configureConsole().ignoreLocalConsole(),
                logLevel(LogLevelOption.LogLevel.INFO),
                keepRuntimeFolder(),
                wrappedBundle(maven().artifactId("mybatis").groupId("org.mybatis").version("3.2.8")),
                //Features
                features(paxJdbcFeaturesRepo, "transaction", "jndi", "pax-jdbc-config", "pax-jdbc-h2")
        );

        return combine(configOptions, baseOptions);
    }

    void createAndDropH2Table(DataSource demodbDS) throws Exception {
        Connection connection = demodbDS.getConnection();
        ScriptRunner scriptRunner = new ScriptRunner(connection);
        InputStreamReader sqlStreamReader = new InputStreamReader(getClass().getResourceAsStream("/h2_demodb.sql"));

        if (sqlStreamReader != null) {
            scriptRunner.runScript(sqlStreamReader);
        } else {
            throw new Exception("SQL File not found");
        }
    }
}
