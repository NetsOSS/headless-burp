Headless Burp Proxy
===================

Provides an extension to Burp that allows you to run, stop and capture results from the Burp proxy tool in headless mode.

### Features
* Starts the burp proxy on a provided port (default `4646`)
* Register a shutdown listener and wait for a shutdown request (default `"SHUTDOWN"`) on port (default `4444`).
* On receiving a shutdown request, saves the burp project file along with all the information regarding the proxied requests and responses, and finally shuts down Burp

### Start Burp Proxy

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

#### Commandline Options

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

### Stop Burp Proxy

```
echo SHUTDOWN >> /dev/tcp/127.0.0.1/4444
or
echo SHUTDOWN | netcat 127.0.0.1 4444
or
echo SHUTDOWN | ncat 127.0.0.1 4444
```