package eu.nets.burp.maven;

import com.google.common.base.Charsets;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

public abstract class AbstractBurpMojo extends AbstractMojo {

    private final Log consoleLog = new StreamLog(System.out);

    /**
     * Location of the burpSuite jar.
     */
    @Parameter(required = true)
    private File burpSuite;

    /**
     * Location of the burpSuite project file.
     */
    @Parameter(required = true)
    private File burpProjectFile;

    /**
     * Run headless.
     */
    @Parameter(defaultValue = "true")
    private boolean headless;

    /**
     * Skip starting burp proxy.
     */
    @Parameter(defaultValue = "false")
    private boolean promptOnExit;

    /**
     * Enable verbose output.
     */
    @Parameter(defaultValue = "false")
    private boolean verbose;

    /**
     * Skip starting burp proxy.
     */
    @Parameter(defaultValue = "false")
    private boolean skip;

    /**
     * groupId of the plugin.
     */
    @Parameter(defaultValue = "${plugin.groupId}", readonly = true)
    private String pluginGroupId;

    /**
     * The artifacts for the plugin itself.
     */
    @Parameter(defaultValue = "${plugin.artifacts}", readonly = true)
    private List<Artifact> pluginArtifacts;

    /**
     * Get the name of the Burp tool in scope for this Mojo.
     *
     * @return name of the burp tool
     */
    protected abstract String getBurpExtenderToRun();

    protected abstract boolean shouldWaitForProcessToComplete();

    /**
     * Build Commandline arguments for the burp extension.
     *
     * @return Commandline arguments for the burp extension
     */
    protected abstract List<String> createBurpExtensionCommandLineArguments();

    protected abstract void execute(ProcessBuilder processBuilder);

    @Override
    public void execute() {
        if (skip) {
            getLog().debug("Skipping execution.");
            return;
        }
        if (burpSuite == null || !burpSuite.exists()) {
            getLog().error("Could not find burpSuite jar");
            getLog().debug("Skipping execution.");
            return;
        }

        ProcessBuilder burp = createBurpProcessBuilder();
        execute(burp);
    }

    protected ProcessBuilder createBurpProcessBuilder() {
        List<String> arguments = createBurpExtensionCommandLineArguments();
        arguments.add("--project-file=" + burpProjectFile.getAbsolutePath());
        arguments.add("--unpause-spider-and-scanner");
        if (promptOnExit) {
            arguments.add("-p");
        }
        if (verbose) {
            arguments.add("-v");
        }

        ProcessBuilder builder = new ProcessBuilder("java", "-Xmx1G", "-Djava.awt.headless=" + headless, "-classpath", getBurpExtensionClasspath(), "burp.StartBurp");
        builder.command().addAll(arguments);
        builder.redirectErrorStream(true);

        return builder;
    }

    private String getBurpExtensionClasspath() {
        Utils.ClassPathBuilder builder = Utils.ClassPathBuilder.newInstance();
        builder.path(burpSuite.getAbsolutePath());

        pluginArtifacts.stream()
                .filter(artifact -> Artifact.SCOPE_RUNTIME.equals(artifact.getScope())
                        && pluginGroupId.equals(artifact.getGroupId())
                        && artifact.getArtifactId().contains(getBurpExtenderToRun()))
                .forEach(artifact -> {
                    builder.path(artifact.getFile().getAbsolutePath());
                    getLog().debug("Adding paths artifact: " + artifact);
                });

        return builder.build();
    }

    protected void executeAndRedirectOutput(ProcessBuilder processBuilder) {
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new ProcessExecutionException(e);
        }

        Thread infoLogThread = new StreamLogHandler(process.getInputStream());
        infoLogThread.start();
        Thread errorLogThread = new StreamLogHandler(process.getErrorStream());
        errorLogThread.start();

        if (shouldWaitForProcessToComplete()) {
            try {
                int exitValue = process.waitFor();
                infoLogThread.join();
                errorLogThread.join();

                if (exitValue != 0) {
                    throw new ProcessExecutionException("Error when running " + StringUtils.join(processBuilder.command().iterator(), " "));
                }

            } catch (InterruptedException e) {
                throw new ProcessExecutionException(e);
            }
        }
    }

    private class StreamLogHandler extends Thread {

        private final InputStream inputStream;

        private StreamLogHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8));
            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    consoleLog.info(line);
                }
            } catch (IOException e) {
                getLog().error("There was an error reading the output.", e);
            } finally {
                IOUtil.close(inputStream);
            }
        }

    }

    private static class ProcessExecutionException extends RuntimeException {
        ProcessExecutionException(String message) {
            super(message);
        }

        ProcessExecutionException(Throwable cause) {
            super(cause);
        }
    }

}
