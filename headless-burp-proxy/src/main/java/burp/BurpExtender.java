package burp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import java.io.IOException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

import static com.google.common.base.Throwables.getStackTraceAsString;

public class BurpExtender implements IBurpExtender, IProxyListener, IExtensionStateListener {

    @Option(name = "--proxy-port", usage = "Proxy port")
    private int proxyPort = 4646;

    @Option(name = "--shutdown-port", usage = "Shutdown port")
    private int shutdownPort = 4444;

    @Option(name = "--shutdown-key", usage = "Shutdown Key")
    private String shutdownKey = "SHUTDOWN";

    @Option(name = "-p", aliases = "--prompt", usage = "Indicates whether to prompt the user to confirm the shutdown", hidden = true)
    private boolean promptUserOnShutdown = false;

    @Option(name = "-v", aliases = "--verbose", usage = "Enable verbose output")
    private boolean verbose = false;

    private ObjectMapper objectMapper;

    private IBurpExtenderCallbacks callbacks;

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks extenderCallbacks) {
        this.callbacks = extenderCallbacks;
        this.objectMapper = new ObjectMapper();

        callbacks.setExtensionName("Headless Burp Proxy");

        callbacks.registerProxyListener(this);
        callbacks.registerExtensionStateListener(this);

        try {
            processCommandLineArguments(callbacks.getCommandLineArguments());
            callbacks.setProxyInterceptionEnabled(false);

            registerShutdownListenerAndWaitForShutdown();
        } catch (Exception e) {
            log("Could not run burp scan, quitting: " + getStackTraceAsString(e));
        }

        callbacks.exitSuite(promptUserOnShutdown);
    }

    /**
     * Parse and process the command line arguments and verify and load the configuration file supplied by the user.
     *
     * @param args The command line arguments that were passed to Burp on startup
     */
    private void processCommandLineArguments(String[] args) {
        ParserProperties parserProperties = ParserProperties.defaults().withUsageWidth(80);
        CmdLineParser parser = new CmdLineParser(this, parserProperties);

        try {
            log("Arguments to headless burp: " + Joiner.on(" ").join(args));
            parser.parseArgument(args);

            String configAsJson = callbacks.saveConfigAsJson();
            JsonNode config = objectMapper.readTree(configAsJson);

            ((ObjectNode) config.path("proxy").path("request_listeners").get(0)).put("listener_port", proxyPort);
            callbacks.loadConfigFromJson(config.toString());
            log("Using proxy 127.0.0.1:" + proxyPort);

            log("Headless Burp Proxy fully configured");
        } catch (CmdLineException e) {
            log("Could not parse commandline arguments, quitting: " + getStackTraceAsString(e));
            parser.printUsage(System.err);
        } catch (IOException e) {
            log("Could not parse burp configuration, quitting: " + getStackTraceAsString(e));
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
     */
    private void registerShutdownListenerAndWaitForShutdown() {
        ShutdownListener shutdownHandler = new ShutdownListener(shutdownPort, shutdownKey, () -> {
            log("Received request to stop proxy");
            log("Exiting burpsuite...");
            callbacks.exitSuite(promptUserOnShutdown);
        });

        //Start the shutdown listener
        shutdownHandler.start();
    }
}
