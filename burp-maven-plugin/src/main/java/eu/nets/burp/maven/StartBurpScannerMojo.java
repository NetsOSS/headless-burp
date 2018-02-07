package eu.nets.burp.maven;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

import static eu.nets.burp.maven.Utils.encloseInDoubleQuotes;

/**
 * Maven plugin that allows you to run the Burp Scanner tool in headless mode.
 */
@Mojo(name = "start-scan")
public class StartBurpScannerMojo extends AbstractBurpMojo {

    /**
     * Location of the burp headless configuration file.
     */
    @Parameter(required = true)
    private File burpConfig;

    @Override
    protected String getBurpExtenderToRun() {
        return "headless-burp-scanner";
    }

    @Override
    protected boolean shouldWaitForProcessToComplete() {
        return true;
    }

    @Override
    protected List<String> createBurpExtensionCommandLineArguments() {
        List<String> command = Lists.newArrayList();
        if (burpConfig != null) {
            command.add("-c");
            command.add(encloseInDoubleQuotes(burpConfig.getAbsolutePath()));
        }
        return command;
    }

    @Override
    protected void execute(ProcessBuilder processBuilder) {
        getLog().info("Starting headless burp scanner...");
        getLog().info(StringUtils.join(processBuilder.command().iterator(), " "));

        executeAndRedirectOutput(processBuilder);
    }

}
