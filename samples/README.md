# Camel Sample
This is a Camel Demo application that demonstrates on how to deploy and check the mybatis-config and extender using  Camel

### Run

### DB Setup


### Running in Apache Karaf / ServiceMix

You can install this example from the shell using this example's `features.xml`
for easy provisioning.

__Install Camel Features__

 `features:addurl mvn:org.apache.camel.karaf/apache-camel/2.15.1.redhat-621084/xml/features
  features:install camel camel-mybatis`camel-netty4-http camel-gson

__Install PAX-JDBC Features__

  `features:addurl mvn:org.ops4j.pax.jdbc/pax-jdbc-features/0.8.0/xml/features
  features:install transaction jndi pax-jdbc-config pax-jdbc-mariadb`

__Install MyBatis-OSGi features__

  `features:addurl mvn:org.workspace7.osgi.mybatis/mybatis-osgi-features/${project.version}/xml/features
  features:install mybatis-config mybatis-extender`

__Install MyBatis-OSGi Camel Demo App Features__

  `features:addurl mvn:org.workspace7.osgi.mybatis.samples/mybatis-camel-demo-features/${project.version}/xml/features
  features:install mybatis-camel-demo-mappers mybatis-camel-demo-app`

### Running in JBoss Fuse


### Accessing the Application

* to get list of all available persons, [http://localhost:8181/mybatis/persons](http://localhost:8181/mybatis/persons)
