package burp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import eu.nets.burp.BurpConfiguration;
import eu.nets.burp.JUnitXmlGenerator;
import eu.nets.burp.config.Issue;
import eu.nets.burp.config.ReportType;
import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

import static com.google.common.base.Throwables.getStackTraceAsString;

public class BurpExtender implements IBurpExtender, IHttpListener, IScannerListener, IExtensionStateListener {

    @Option(name = "-p", aliases = "--prompt", usage = "Indicates whether to prompt the user to confirm the shutdown (useful for debugging)")
    private boolean promptUserOnShutdown = false;

    @Option(name = "-c", aliases = "--config", usage = "Configuration file", metaVar = "<file>", required = true)
    private File configurationFile = new File("configuration.txt");

    @Option(name = "-s", aliases = "--state", usage = "Burp state file", metaVar = "<file>")
    private File burpStateFile = new File("burpstate");

    @Option(name = "-t", aliases = "--thread", usage = "Number of scanner threads to run")
    private String scannerThreads = "25";

    @Option(name = "-v", aliases = "--verbose", usage = "Enable verbose output")
    private boolean verbose = false;

    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private Instant lastRequestTime;

    private volatile List<IScanQueueItem> scanQueueItems = Lists.newArrayList();
    private List<IScanIssue> scanIssues = Lists.newArrayList();
    private BurpConfiguration config;

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks extenderCallbacks) {
        callbacks = extenderCallbacks;
        helpers = callbacks.getHelpers();

        callbacks.setExtensionName("Headless Burp Scanner");

        callbacks.registerHttpListener(this);
        callbacks.registerScannerListener(this);
        callbacks.registerExtensionStateListener(this);

        try {
            if (!processCommandLineArguments(callbacks.getCommandLineArguments())) {
                return;
            }

            config.getExclusions().forEach(callbacks::excludeFromScope);

            scanSiteMap(config.getSiteMap());

            scanUrls(config.getUrls());

            lastRequestTime = Instant.now();
            monitorScanQueue();

            generateReport(config.getReportType(), config.getReportFile());
        } catch (Exception e) {
            log("Could not run burp scan, quitting: " + getStackTraceAsString(e));
        }

        callbacks.exitSuite(promptUserOnShutdown);
    }

    /**
     * Add {@link URL}s from the sitemap to scope and send them to the Burp scanner
     *
     * @param siteMapUrlPrefix Base URL to scan from
     */
    private void scanSiteMap(URL siteMapUrlPrefix) {
        if (siteMapUrlPrefix != null) {
            IHttpRequestResponse[] siteMapItems = callbacks.getSiteMap(config.getSiteMap().toString());
            log("Scanning [" + siteMapItems.length + "] from sitemap [" + config.getSiteMap().toString() + "]");
            for (IHttpRequestResponse siteMapItem : siteMapItems) {
                IRequestInfo requestInfo = helpers.analyzeRequest(siteMapItem);
                URL url = requestInfo.getUrl();
                if (verbose) {
                    try {
                        log("Scanning: " + requestInfo.getMethod() + " : " + url);
                    } catch (Exception e) {
                        //Ignore
                    }
                }

                if (!config.getExclusions().contains(url)) {
                    callbacks.includeInScope(url);
                    sendToScanner(siteMapItem);
                }
            }
        }
    }

    /**
     * Add the {@link URL}s to scope and send them to the Burp spider
     *
     * @param urls list of {@link URL}s to be scanned
     */
    private void scanUrls(List<URL> urls) {
        for (URL url : urls) {
            scanUrl(url);
        }
    }

    private void scanUrl(URL url) {
        if (!callbacks.isInScope(url) && !config.getExclusions().contains(url)) {
            callbacks.includeInScope(url);
        }

        lastRequestTime = Instant.now();
        callbacks.sendToSpider(url);
        log("Starting spider on " + url + " at " + lastRequestTime);
    }

    /**
     * Parse and process the command line arguments and verify and load the configuration file supplied by the user
     *
     * @param args The command line arguments that were passed to Burp on startup
     */
    private boolean processCommandLineArguments(String[] args) {
        ParserProperties parserProperties = ParserProperties.defaults().withUsageWidth(80);
        CmdLineParser parser = new CmdLineParser(this, parserProperties);

        try {
            log("Arguments to headless burp: " + Joiner.on(" ").join(args));
            parser.parseArgument(args);

            if (configurationFile == null || !configurationFile.exists()) {
                log("Could not find configuration file: [" + configurationFile + "]");
            }

            config = new BurpConfiguration(configurationFile);
            if (config.getUrls() == null && config.getSiteMap() == null) {
                throw new RuntimeException("Must provide either scope or sitemap");
            }

            if (burpStateFile != null && burpStateFile.exists()) {
                log("Restoring burp state from file: [" + burpStateFile.getName() + "]");
                callbacks.restoreState(burpStateFile);
            }

            Map<String, String> burpConfig = callbacks.saveConfig();
            burpConfig.put("suite.inScopeOnly", "true");
            if (scannerThreads != null) {
                log("Using number of scanner-threads:" + scannerThreads);
                burpConfig.put("scanner.numthreads", scannerThreads);
            }
            callbacks.loadConfig(burpConfig);

            log("Headless Burp Scanner loaded with:");
            log("configuration file: " + configurationFile.getName());
        } catch (CmdLineException cle) {
            log("No arguments found for Headless Burp, quitting");
            return false;
        } catch (Exception e) {
            log("Could not parse commandline arguments, quitting: " + getStackTraceAsString(e));
            parser.printUsage(System.err);
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Receive requests from the spider and send them to the scanner
     */
    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
        lastRequestTime = Instant.now();
        URL requestUrl = helpers.analyzeRequest(messageInfo).getUrl();
        if (messageIsRequest
                && toolFlag == IBurpExtenderCallbacks.TOOL_SPIDER
                && callbacks.isInScope(requestUrl)) {
            sendToScanner(messageInfo);
        }
    }

    /**
     * Send the request to the Burp Scanner tool to perform an active vulnerability scan.
     *
     * @param messageInfo Details of the request / response to be processed.
     *                    Extensions can call the setter methods on this object to update the current message and so modify Burp's behavior.
     */
    private void sendToScanner(IHttpRequestResponse messageInfo) {
        IHttpService httpService = messageInfo.getHttpService();
        boolean serviceIsHttps = "https".equals(httpService.getProtocol());

        URL url = helpers.analyzeRequest(messageInfo).getUrl();
        if (callbacks.isInScope(url) && !config.getExclusions().contains(url)) {
            log("Sending URL to scanner: " + url);
            IScanQueueItem scanQueueItem = callbacks.doActiveScan(httpService.getHost(), httpService.getPort(), serviceIsHttps, messageInfo.getRequest());
            scanQueueItems.add(scanQueueItem);
        } else if (verbose) {
            log("Skipping URL: " + url);
        }
    }

    private void monitorScanQueue() {
        log("Monitoring scanQueue, waiting for spider to complete");

        try {
            while (!scanQueueItems.isEmpty()) {
                Iterator<IScanQueueItem> iterator = scanQueueItems.iterator();
                while (iterator.hasNext()) {
                    IScanQueueItem scanQueueItem = iterator.next();
                    if (100 == scanQueueItem.getPercentageComplete()) {
                        iterator.remove();
                    }
                }

                log(scanQueueItems.size() + " remaining items in scan queue at " + Instant.now());

                Thread.yield();
                Thread.sleep(5000);
            }

            while (Instant.now().isBefore(lastRequestTime.plusSeconds(10))) {
                Thread.yield();
                Thread.sleep(5000);
            }

        } catch (Exception e) {
            log("Error when monitoring scan queue: " + getStackTraceAsString(e));
            callbacks.exitSuite(false);
        }

        log("Scanning complete at " + Instant.now());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void newScanIssue(IScanIssue issue) {
        if (isFalsePositive(issue)) {
            log("Excluding false positive: " + issue.getIssueName() + " at URL: " + issue.getUrl());
        } else {
            log("New scan issue found: " + issue.getIssueName() + " at URL: " + issue.getUrl());
            scanIssues.add(issue);
        }
    }

    /**
     * Check if the provided issue is in the configured falsePositives list
     *
     * @param issue Issue found by the scanner
     * @return {@code true} if Issue is in the configured falsePositives list, otherwise {@code false}
     */
    private boolean isFalsePositive(IScanIssue issue) {
        for (Issue falsePositive : config.getFalsePositives()) {
            if (falsePositive.getType() == issue.getIssueType() && issue.getUrl().getPath().matches(falsePositive.getPath())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Generate a report for issues found by the Scanner
     *
     * @param reportType     The format to be used in the report. Accepted values are HTML, XML and JUNIT.
     * @param burpReportFile The {@link File} to which the report will be saved
     */
    private void generateReport(ReportType reportType, File burpReportFile) {
        log("Generating scan report");
        callbacks.generateScanReport(config.getScanReportType(), scanIssues.toArray(new IScanIssue[scanIssues.size()]), burpReportFile);

        if (reportType == ReportType.JUNIT) {
            log("Generating JUnit report");

            File jUnitReportFile = new File(burpReportFile.getParentFile(), "TEST-burp-scan.xml");
            JUnitXmlGenerator.generateJUnitReportFromBurpReport(burpReportFile, jUnitReportFile);

            log("Generating HTML report");
            callbacks.generateScanReport("HTML", scanIssues.toArray(new IScanIssue[scanIssues.size()]), new File("burp-report.html"));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void extensionUnloaded() {
        log("Headless Burp POC Extension was unloaded");
    }

    @VisibleForTesting
    void setConfig(BurpConfiguration config) {
        this.config = config;
    }

    @VisibleForTesting
    List<IScanIssue> getScanIssues() {
        return scanIssues;
    }

    private void log(String message) {
        callbacks.issueAlert(message);
        callbacks.printOutput(message);
    }
}
