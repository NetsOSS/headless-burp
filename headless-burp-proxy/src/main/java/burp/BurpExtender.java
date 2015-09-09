package burp;

import com.google.common.base.Joiner;
import java.io.File;
import java.util.Map;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

import static com.google.common.base.Throwables.getStackTraceAsString;

public class BurpExtender implements IBurpExtender, IProxyListener, IExtensionStateListener {

    @Option(name = "-p", aliases = "--prompt", usage = "Indicates whether to prompt the user to confirm the shutdown", hidden = true)
    private boolean promptUserOnShutdown = false;

    @Option(name = "-s", aliases = "--state", usage = "Burp state file", metaVar = "<file>")
    private File burpStateFile = new File("burpstate");

    @Option(name = "-v", aliases = "--verbose", usage = "Enable verbose output")
    private boolean verbose = false;

    @Option(name = "--proxyPort", usage = "Proxy port")
    private int proxyPort = 4646;

    @Option(name = "--shutdownPort", usage = "Shutdown port")
    private int shutdownPort = 4444;

    @Option(name = "--shutdownKey", usage = "Shutdown Key")
    private String shutdownKey = "SHUTDOWN";

    private IBurpExtenderCallbacks callbacks;

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks extenderCallbacks) {
        this.callbacks = extenderCallbacks;

        callbacks.setExtensionName("Headless Burp Proxy");

        callbacks.registerProxyListener(this);
        callbacks.registerExtensionStateListener(this);

        try {
            if (callbacks.getCommandLineArguments().length == 0) {
                log("No arguments found for Headless Burp, quitting");
                return;
            }

            processCommandLineArguments(callbacks.getCommandLineArguments());
            callbacks.setProxyInterceptionEnabled(false);

            registerShutdownListenerAndWaitForShutdown();
        } catch (Exception e) {
            log("Could not run burp scan, quitting: " + getStackTraceAsString(e));
        }

        callbacks.exitSuite(promptUserOnShutdown);
    }

    /**
     * Parse and process the command line arguments and verify and load the configuration file supplied by the user
     *
     * @param args The command line arguments that were passed to Burp on startup
     */
    private void processCommandLineArguments(String[] args) {
        ParserProperties parserProperties = ParserProperties.defaults().withUsageWidth(80);
        CmdLineParser parser = new CmdLineParser(this, parserProperties);

        try {
            log("Arguments to headless burp: " + Joiner.on(" ").join(args));
            parser.parseArgument(args);

            if (burpStateFile != null && burpStateFile.exists()) {
                log("Restoring burp state from file: [" + burpStateFile.getName() + "]");
                callbacks.restoreState(burpStateFile);
            }

            Map<String, String> burpConfig = callbacks.saveConfig();
            burpConfig.put("suite.inScopeOnly", "true");
            log("Using proxy 127.0.0.1:" + proxyPort);
            burpConfig.put("proxy.listener", null);
            burpConfig.put("proxy.listener0", "1." + proxyPort + ".1.0..0.0.1.0..0..0..0.");
            callbacks.loadConfig(burpConfig);

            log("Headless Burp Proxy fully configured");
        } catch (Exception e) {
            log("Could not parse commandline arguments, quitting: " + getStackTraceAsString(e));
            parser.printUsage(System.err);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void extensionUnloaded() {
        log("Headless Burp Proxy was unloaded");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processProxyMessage(boolean messageIsRequest, IInterceptedProxyMessage message) {
        if (verbose && messageIsRequest) {
            IRequestInfo requestInfo = callbacks.getHelpers().analyzeRequest(message.getMessageInfo());
            log("Proxy request to " + requestInfo.getUrl());
        }
    }

    private void log(String message) {
        callbacks.issueAlert(message);
        callbacks.printOutput(message);
    }

    /**
     * Register a shutdown listener and wait for a shutdown request.
     *
     * @throws Exception
     */
    private void registerShutdownListenerAndWaitForShutdown() throws Exception {
        ShutdownListener shutdownHandler = new ShutdownListener(shutdownPort, shutdownKey, () -> {
            File saveState = new File("headless-burp-proxy.state");

            log("Received request to stop proxy");
            log("Saving the state to " + saveState.getAbsolutePath());
            callbacks.saveState(saveState);

            log("Exiting burpsuite...");
            callbacks.exitSuite(promptUserOnShutdown);
        });

        //Start the shutdown listener
        shutdownHandler.start();
    }
}
