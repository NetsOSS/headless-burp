## Headless Burp Proxy

Provides an extension to Burp that allows you to run, stop and capture results from the Burp Proxy tool in headless mode.

### Features
* Starts the burp proxy on a provided port (default `4646`)
* Register a shutdown listener and wait for a shutdown request (default `"SHUTDOWN"`) on port (default `4444`).
* On receiving a shutdown request, saves the burp state to a file (headless-burp-proxy.state) and shutsdown Burp

### Start Burp Proxy

On *nix:

    /usr/java/latest8/bin/java -Xmx1G -Djava.awt.headless=true -classpath headless-burp-scanner-master-SNAPSHOT-jar-with-dependencies.jar:burpsuite_pro_v1.6.26.jar burp.StartBurp

On Cygwin:
 
    java -Xmx1G -Djava.awt.headless=true -classpath "headless-burp-scanner-master-SNAPSHOT-jar-with-dependencies.jar;burpsuite_pro_v1.6.26.jar" burp.StartBurp

#### Commandline Options

    -p (--prompt)        : Indicates whether to prompt the user to confirm the
                           shutdown (useful for debugging)
    -s (--state) <file>  : Burp state file
    -v (--verbose)       : Enable verbose output
    --proxyPort VAL      : Proxy port
    --shutdownPort VAL   : Shutdown port
    --shutdownKey VAL    : Shutdown key

### Stop Burp Proxy

    echo SHUTDOWN >> /dev/tcp/127.0.0.1/4444
    or
    echo SHUTDOWN | netcat 127.0.0.1 4444
    or
    echo SHUTDOWN | ncat 127.0.0.1 4444
