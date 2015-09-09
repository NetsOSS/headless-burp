package eu.nets.burp.maven;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

@Mojo(name = "start-proxy", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class StartBurpProxyMojo extends AbstractBurpMojo {

    /**
     * Burp proxy port.
     */
    @Parameter(defaultValue = "4646")
    private int proxyPort;

    /**
     * Burp proxy shutdown port.
     */
    @Parameter(defaultValue = "4444")
    private int shutdownPort;

    @Override
    protected String getBurpExtenderToRun() {
        return "headless-burp-proxy";
    }

    @Override
    protected boolean shouldWaitForProcessToComplete() {
        return false;
    }

    @Override
    protected List<String> createBurpCommandLine() throws MojoExecutionException {
        List<String> command = Lists.newArrayList();
        command.add("--proxyPort");
        command.add(String.valueOf(proxyPort));

        command.add("--shutdownPort");
        command.add(String.valueOf(shutdownPort));

        if (getBurpState() != null) {
            command.add("-s");
            command.add("\"" + getBurpState().getAbsolutePath() + "\"");
        }
        if (isPromptOnExit()) {
            command.add("-p");
        }
        if (isVerbose()) {
            command.add("-v");
        }

        return command;
    }

    @Override
    protected void execute(ProcessBuilder processBuilder) {
        getLog().info("Starting headless burp proxy...");
        getLog().info(StringUtils.join(processBuilder.command().iterator(), " "));

        executeAndRedirectOutput(processBuilder);
    }
}
