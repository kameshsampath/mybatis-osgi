# Overview
The aim of this project is to enable working with [MyBatis](http://www.mybatis.org/mybatis-3/) in an OSGi environment.
Currently a lot of boilerplate code is needed to get the mybatis SQLSession typically as explained [here](http://www.mybatis.org/mybatis-3/getting-started.html).

The bundles `mybatis-config` and `mybatis-extender` will help in removing the boilerplate code and configures the myBatis
Environment and Configuration with sane defaults apart from auto linking the Configuration with Datasource.

The datasource could be made available to OSGi container as a service, a pretty good approch to do that is via [PAX-JDBC](https://ops4j1.jira.com/wiki/display/PAXJDBC/Pax+JDBC).

# Getting Started

Checkout or clone the project from [mybatis-osgi](https://github.com/kameshsampath/mybatis-osgi) and do

    `mvn clean install`


Refer to samples folder to various samples on how to use this bundle with OSGi contianers like [Karaf](http://karaf.apache.org/) and [Camel](http://camel.apache.org/).

## Deploying Bundles

The bundle could be deployed in Apache Karaf using the following command,
```
feature:repo-add mvn:org.workspace7.osgi.mybatis/mybatis-osgi-features/0.1.0/xml/features
feature:install mybatis-config mybatis-extender
```

### Configuring Mybatis Mapper Bundles

The main feature of these bundles is to do auto-register/de-register of the mappers using OSGi Extender Model. To deploy
new mapper(s), you need to create a plain OSGi bundle with your mapper xmls and mapper classes in respective packages, and
deploy them to OSGi container.

The `mybatis-extender` bundle will look for MANIFEST header like *MyBatis-Mappers*, which is defined as follows

```
MyBatis-Mappers ::= dsname=packages *

dsname ::= < a valid datasource (javax.sql.DataSource)
            service that will be available as OSGi service
            with a property dataSourceName=dsname >
packages ::= < the java package names that will be exported by the mapper bundle
                and which holds the MyBatis Mapper classes or xmls >
```

#### Sample MANIFEST.MF

```
Manifest-Version: 1.0
Bnd-LastModified: 1464849163651
Bundle-ManifestVersion: 2
Bundle-Name: MyBatis Mappers - Used to test Extender
Bundle-SymbolicName: org.workspace7.osgi.mybatis.itests.mappers
Bundle-Version: 0.1.0
Created-By: 1.8.0_73 (Oracle Corporation)
Export-Package: org.workspace7.osgi.mybatis.itests.mappers;version="0.
 1.0"
MyBatis-Mappers: demodbDS=org.workspace7.osgi.mybatis.itests.mappers
Require-Capability: osgi.ee;filter:="(&(osgi.ee=JavaSE)(version=1.7))"
Tool: Bnd-3.2.0.201605172007

```

### Lookup Environment and Configuraiton

The Environment and Configuration deployed with `mybatis-config` and `mybatis-extender` can be looked up as follows,

* Environment Service Lookup Filter

    `&((objectclass=org.apache.ibatis.mapping.Environment)(dataSourceName=demodbDS))`

* Configuration Service Lookup Filter

    `&((objectclass=org.apache.ibatis.session.Configuration)(dataSourceName=demodbDS))`

As noted above there will be one `Environment` and `Configuration` for each dataSource deployed.

## Issues and Enhacements
   [Github](https://github.com/kameshsampath/mybatis-osgi/issues)

## LICENSE

	Copyright 2016 Kamesh Sampath.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
    implied.

    See the License for the specific language governing permissions and
    limitations under the License.