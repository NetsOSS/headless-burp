Headless Burp Scanner
=====================

Provides an extension to Burp that allows you to run the Burp Scanner tool in headless mode.

## Setup Instructions


## Configuration

#### Sample configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://nets.eu/burp/config">
    <reportType>JUNIT</reportType>
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

For an example configuration file, see [config.xml]

### Run Burp Scan

On *nix:

    /usr/java/latest8/bin/java -Xmx1G -Djava.awt.headless=true -classpath headless-burp-scanner-master-SNAPSHOT-jar-with-dependencies.jar:burpsuite_pro_v1.6.26.jar burp.StartBurp -c config.xml

On Cygwin:
 
    java -Xmx1G -Djava.awt.headless=true -classpath "headless-burp-scanner-master-SNAPSHOT-jar-with-dependencies.jar;burpsuite_pro_v1.6.26.jar" burp.StartBurp -c config.xml -s burp_state

### Commandline Options

    -c (--config) <file>          Configuration file
    -p (--prompt)                 Indicates whether to prompt the user to confirm the shutdown (useful for debugging)
    -v (--verbose)                Enable verbose output
    
    --diagnostics                 Print diagnostic information
    --use-defaults                Start with default settings
    --collaborator-server         Run in Collaborator server mode
    --collaborator-config         Specify Collaborator server configuration file; defaults to collaborator.config
    --project-file                Open the specified project file; this will be created as a new project if the file does not exist
    --config-file                 Load the specified project configuration file(s); this option may be repeated to load multiple files
    --user-config-file            Load the specified user configuration file(s); this option may be repeated to load multiple files
    --auto-repair                 Automatically repair a corrupted project file specified by the --project-file option

### Running against an SSL enabled application


#### Generate java keystore

    `keytool.exe -import -keystore burp.jks -file test.nets.no.crt -alias test.nets.no`

#### Add commandline args

    -Djavax.net.ssl.keyStore=burp.jks -Djavax.net.ssl.keyStorePassword=headlessburp -Djavax.net.ssl.keyStoreType=jks


[config.xml]: ../blob/master/headless-burp-scanner/src/test/resources/config.xml