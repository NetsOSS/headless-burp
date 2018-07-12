Headless Burp
=============

Provides a suite of extensions and a maven plugin to automate security tests using [Burp Suite].

[![Build Status][travis-build-status]][travis-url]
[![Code Coverage][codecov-status]][codecov-url]
[![Maintainability][codeclimate-rating]][codeclimate-url]

Full documentation for the project is available at https://netsoss.github.io/headless-burp/

---

* [Headless Burp Proxy](#headless-burp-proxy)
  * [Features](#features)
  * [Usage](#usage)
	* [Start Burp Proxy](#start-burp-proxy)
	* [Command-line Options](#command-line-options)
	* [Stop Burp Proxy](#stop-burp-proxy)
* [Headless Burp Scanner](#headless-burp-scanner)
  * [Usage](#usage-1)
    * [Configuration](#configuration)
    * [Command-line options](#command-line-options-1)
  * [Scenarios](#scenarios)
    * [Scenario A: Scan URL(s) for security issues using Burp](#scenario-a-scan-urls-for-security-issues-using-burp)
    * [Scenario B: Scan URL(s) for security issues using Burp but exclude scanning of certain paths](#scenario-b-scan-urls-for-security-issues-using-burp-but-exclude-scanning-of-certain-paths)
    * [Scenario C: Scan URL(s) for security issues using Burp but suppress false positives from the scan report](#scenario-c-scan-urls-for-security-issues-using-burp-but-suppress-false-positives-from-the-scan-report)
    * [Scenario D: Scan more than just GET requests. Use data derived from running functional tests as input to the scan](#scenario-d-scan-more-than-just-get-requests-use-data-derived-from-running-functional-tests-as-input-to-the-scan)
  * [tl;dr;](#tldr)
* [Burp Maven Plugin](#burp-maven-plugin)
  * [Full example](#full-example)

Headless Burp Proxy
=====================

Provides an extension to Burp that allows you to run, stop and capture results from the Burp proxy tool in headless mode.

### Features
* Starts the burp proxy on a provided port (default `4646`)
* Register a shutdown listener and wait for a shutdown request (default `"SHUTDOWN"`) on port (default `4444`).
* On receiving a shutdown request, saves the burp project file along with all the information regarding the proxied requests and responses, and finally shuts down Burp

### Usage

#### Start Burp Proxy

On *nix:

```
java -Xmx1G -Djava.awt.headless=true \
-classpath headless-burp-proxy-master-SNAPSHOT-jar-with-dependencies.jar:burpsuite_pro_v1.7.31.jar burp.StartBurp \
--project-file=project.burp
```

On Cygwin:

```
java -Xmx1G -Djava.awt.headless=true \
-classpath "headless-burp-proxy-master-SNAPSHOT-jar-with-dependencies.jar;burpsuite_pro_v1.7.31.jar" burp.StartBurp \
--project-file=project.burp
```

#### Command-line Options

```
--project-file=VAL          Open the specified project file; this will be created as a new project if the file does not exist (mandatory)
--proxyPort VAL             Proxy port
--shutdownPort VAL          Shutdown port
--shutdownKey VAL           Shutdown key
-p (--prompt)               Indicates whether to prompt the user to confirm the shutdown (useful for debugging)
-v (--verbose)              Enable verbose output

--diagnostics               Print diagnostic information
--use-defaults              Start with default settings
--collaborator-server       Run in Collaborator server mode
--collaborator-config=VAL   Specify Collaborator server configuration file; defaults to collaborator.config
--config-file=VAL           Load the specified project configuration file(s); this option may be repeated to load multiple files
--user-config-file=VAL      Load the specified user configuration file(s); this option may be repeated to load multiple files
--auto-repair               Automatically repair a corrupted project file specified by the --project-file option
```

#### Stop Burp Proxy

```
echo SHUTDOWN >> /dev/tcp/127.0.0.1/4444
or
echo SHUTDOWN | netcat 127.0.0.1 4444
or
echo SHUTDOWN | ncat 127.0.0.1 4444
```

Headless Burp Scanner
=====================

Provides an extension to Burp that allows you to run Burp Suite's spider and scanner tools in headless mode via command-line.

However, it can do more!
It can produce a JUnit like report which in turn could instruct the CI server (maybe [Jenkins]) to mark the build as "failed" whenever any vulnerabilities are found. You can also mark some issues as false positives and those will not be reported anymore on the next scan reports. 

### Usage

On *nix:

```sh
java -Xmx1G -Djava.awt.headless=true \
-classpath headless-burp-scanner-master-SNAPSHOT-jar-with-dependencies.jar:burpsuite_pro_v1.7.31.jar burp.StartBurp \
--project-file=project.burp -c config.xml
```

On Cygwin:

```sh
java -Xmx1G -Djava.awt.headless=true \
-classpath "headless-burp-scanner-master-SNAPSHOT-jar-with-dependencies.jar;burpsuite_pro_v1.7.31.jar" burp.StartBurp \
--project-file=project.burp -c config.xml
```

#### Configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://nets.eu/burp/config">
    <reportType>HTML</reportType> <!-- JUNIT|HTML|XML -->
    <targetSitemap><![CDATA[http://localhost:5432]]></targetSitemap>
    <scope>
        <url><![CDATA[http://localhost:5432/]]></url>
        <exclusions>
            <exclusion><![CDATA[localhost:5432/#/logout]]></exclusion>
        </exclusions>
    </scope>
    <false-positives>
        <issue>
            <type>5244416</type>
            <path>.*</path>
        </issue>
        <issue>
            <type>5247488</type>
            <path>.*bower_components.*</path>
        </issue>
    </false-positives>
</config>
```

For an example configuration file, see [config.xml] and [headless-burp-scanner-config.xsd] for the xsd

#### Command-line options

```
--project-file=VAL          Open the specified project file; this will be created as a new project if the file does not exist (mandatory)
-c (--config) <file>        Configuration file (mandatory)
-p (--prompt)               Indicates whether to prompt the user to confirm the shutdown (useful for debugging)
-v (--verbose)              Enable verbose output

--diagnostics               Print diagnostic information
--use-defaults              Start with default settings
--collaborator-server       Run in Collaborator server mode
--collaborator-config=VAL   Specify Collaborator server configuration file; defaults to collaborator.config
--config-file=VAL           Load the specified project configuration file(s); this option may be repeated to load multiple files
--user-config-file=VAL      Load the specified user configuration file(s); this option may be repeated to load multiple files
--auto-repair               Automatically repair a corrupted project file specified by the --project-file option
```

### Scenarios

The extension has been designed to be versatile and support several scenarios

#### Scenario A: Scan URL(s) for security issues using Burp

1. Create a file - config.xml like below and add the URL(s) to be scanned to the scope.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://nets.eu/burp/config">
    <reportType>HTML</reportType>
    <targetSitemap><![CDATA[http://localhost:5432]]></targetSitemap>
    <scope>
        <url><![CDATA[http://localhost:5432/auth]]></url>
        <url><![CDATA[http://localhost:5432/users]]></url>
        <url><![CDATA[http://localhost:5432/users/1]]></url>
        <url><![CDATA[http://localhost:5432/users?search=asd]]></url>
        <url><![CDATA[http://localhost:5432/bar/foo]]></url>
    </scope>
</config>
```

2. Run as shown in the [usage](#usage-1) section

#### Scenario B: Scan URL(s) for security issues using Burp but exclude scanning of certain paths 

1. Add an _exclusions_ block to the configuration file.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://nets.eu/burp/config">
    <reportType>HTML</reportType>
    <targetSitemap><![CDATA[http://localhost:5432]]></targetSitemap>
    <scope>
        <url><![CDATA[http://localhost:5432/auth]]></url>
        <url><![CDATA[http://localhost:5432/users]]></url>
        <url><![CDATA[http://localhost:5432/users/1]]></url>
        <url><![CDATA[http://localhost:5432/users?search=asd]]></url>
        <url><![CDATA[http://localhost:5432/bar/foo]]></url>
        <exclusions>
            <exclusion><![CDATA[localhost:5432/#/logout]]></exclusion>
            <exclusion><![CDATA[localhost:5432/#/users/delete]]></exclusion>
            <exclusion><![CDATA[localhost:5432/#/creepy/crawly]]></exclusion>
        </exclusions>
    </scope>
</config>
```

2. Run as shown in the [usage](#usage-1) section

#### Scenario C: Scan URL(s) for security issues using Burp but suppress false positives from the scan report

1. Add a _false-positives_ block with the issue type and path _(these can be retrieved from a burp scan report)_ to the configuration file.
You can find more details about [Issue Definitions here]

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://nets.eu/burp/config">
    <reportType>HTML</reportType>
    <targetSitemap><![CDATA[http://localhost:5432]]></targetSitemap>
    <scope>
        <url><![CDATA[http://localhost:5432/auth]]></url>
        <url><![CDATA[http://localhost:5432/users]]></url>
        <url><![CDATA[http://localhost:5432/users/1]]></url>
        <url><![CDATA[http://localhost:5432/users?search=asd]]></url>
        <url><![CDATA[http://localhost:5432/bar/foo]]></url>
        <exclusions>
            <exclusion><![CDATA[localhost:5432/#/logout]]></exclusion>
            <exclusion><![CDATA[localhost:5432/#/users/delete]]></exclusion>
            <exclusion><![CDATA[localhost:5432/#/creepy/crawly]]></exclusion>
        </exclusions>
        <false-positives>
            <issue>
                <type>5244416</type>
                <path>.*</path>
            </issue>
            <issue>
                <type>5247488</type>
                <path>.*bower_components.*</path>
            </issue>
        </false-positives>
    </scope>
</config>
```

2. Run as shown in the [usage](#usage-1) section

#### Scenario D: Scan more than just GET requests. Use data derived from running functional tests as input to the scan

Sometimes, just spidering a target scope and and performing on a scope of URLs doesnt give much value. 
For e.g. when scanning a web application where routing is handled using JavaScript. 
Burp scans can discover more if it can scan more "real-world" requests and responses. 
This way, it can attack the target URLs more effectively and potentially discover more than a _shot in the dark_ spider + scan approach. 


To handle such cases, it would be best to let the burp proxy intercept some real traffic to the target and build up a sitemap for itself. 
The [Headless Burp Proxy] extension provides an simple way to achieve this.

1. Follow instructions at [Headless Burp Proxy] and start up burp proxy and remember to set the `--project-file` option. This is where the "seed" data for scanning is going to be stored.
2. Configure your functional/integration tests to go through the burp proxy (defaults to `4646` if you use the extension) by setting HTTP_PROXY or similar.
3. Run the functional/integration tests against the target.
4. Create a config.xml with the targetSitemap  (typically, the base URL of the application), scope, exclusions, false-positives etc.


```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://nets.eu/burp/config">
    <reportType>HTML</reportType>
    <targetSitemap><![CDATA[http://localhost:5432]]></targetSitemap>
    <scope>
        <url><![CDATA[http://localhost:5432]]></url>
        <exclusions>
            <exclusion><![CDATA[localhost:5432/#/logout]]></exclusion>
        </exclusions>
        <false-positives>
            <issue>
                <type>5244416</type>
                <path>.*</path>
            </issue>
        </false-positives>
    </scope>
</config>
```

5. Run as shown in the [usage](#usage-1) section and remember to set the `--project-file` option

### tl;dr;

The headless burp scanner plugin can do these

* Run burp scan in headless or GUI mode
* Specify target sitemap and add URL(s) to Burp's target scope
* Use the "seed" request/response data generated by any integration/functional tests you might have
* Mark issues as false positives, these will not be reported in the scan report anymore.
* Spider the target scope.
* Actively scan the target scope.
* Generate a scan report in JUnit/HTML/XML format.
* Shut down Burp


Burp Maven Plugin
=================

Maven plugin that allows you to run Burp Suite's Proxy and Scanner tools in headless mode.

The plugin is essentially a wrapper around the [Headless Burp Proxy] and [Headless Burp Scanner] extensions. It offers easy way to integrate security testing using [Burp Suite] into the project build lifecycle.

### Full example
    
```xml
<build>
    ...
    <plugins>
        ...
        <plugin>
            <groupId>eu.nets.burp</groupId>
            <artifactId>burp-maven-plugin</artifactId>
            <version>master-SNAPSHOT</version>
            <configuration>
                <burpSuite>burp/burpsuite_pro_v1.7.31.jar</burpSuite>
                <burpProjectFile>target/headless-burp-project.burp</burpProjectFile>
                <burpConfig>burp/config.xml</burpConfig>
                <headless>true</headless>
                <promptOnExit>false</promptOnExit>
                <verbose>true</verbose>
                <skip>false</skip>
            </configuration>
            <executions>
                <execution>
                    <id>start-burp-proxy</id>
                    <phase>pre-integration-test</phase>
                    <goals>
                        <goal>start-proxy</goal>
                    </goals>
                </execution>
                <execution>
                    <id>stop-burp-proxy</id>
                    <phase>post-integration-test</phase>
                    <goals>
                        <goal>stop-proxy</goal>
                    </goals>
                </execution>
                <execution>
                    <id>start-burp-scan</id>
                    <phase>verify</phase>
                    <goals>
                        <goal>start-scan</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        ...
    </plugins>
    ...
</build>
```

[Burp Suite]: https://portswigger.net/burp

[Jenkins]: https://jenkins.io/
[issue definitions here]: https://portswigger.net/kb/issues
[config.xml]: https://github.com/NetsOSS/headless-burp/blob/master/headless-burp-scanner/src/test/resources/config.xml
[headless-burp-scanner-config.xsd]: https://github.com/NetsOSS/headless-burp/blob/master/headless-burp-scanner/src/main/resources/headless-burp-scanner-config.xsd
[Headless Burp Proxy]: #headless-burp-proxy
[Headless Burp Scanner]: #headless-burp-scanner


[travis-build-status]: https://travis-ci.org/NetsOSS/headless-burp.svg?branch=master
[travis-url]: https://travis-ci.org/NetsOSS/headless-burp
[codecov-status]: https://codecov.io/gh/NetsOSS/headless-burp/branch/master/graph/badge.svg
[codecov-url]: https://codecov.io/gh/NetsOSS/headless-burp
[codeclimate-rating]: https://codeclimate.com/github/NetsOSS/headless-burp.svg
[codeclimate-url]: https://codeclimate.com/github/NetsOSS/headless-burp/maintainability 
