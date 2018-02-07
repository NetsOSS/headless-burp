Headless Burp Scanner
=====================

Provides an extension to Burp that allows you to run the Burp scanner tool in headless mode.

### Configuration

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

For an example configuration file, see [config.xml]

### Run Burp Scan

On *nix:

    java -Xmx1G -Djava.awt.headless=true \
    -classpath headless-burp-scanner-master-SNAPSHOT-jar-with-dependencies.jar:burpsuite_pro_v1.7.31.jar burp.StartBurp \
    --project-file=project.burp -c config.xml

On Cygwin:
 
    java -Xmx1G -Djava.awt.headless=true \
    -classpath "headless-burp-scanner-master-SNAPSHOT-jar-with-dependencies.jar;burpsuite_pro_v1.7.31.jar" burp.StartBurp \
    --project-file=project.burp -c config.xml

### Commandline Options

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

[config.xml]: https://github.com/NetsOSS/headless-burp/blob/master/headless-burp-scanner/src/test/resources/config.xml