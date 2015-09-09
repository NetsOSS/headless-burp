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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.IOUtil;

public abstract class AbstractBurpMojo extends AbstractMojo {

    private Log consoleLog = new StreamLog(System.out);

    /**
     * Location of the burpsuite jar.
     */
    @Parameter(required = true)
    private File burpSuite;

    /**
     * Location of the burpsuite state file to restore to.
     */
    @Parameter
    private File burpState;

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
     * groupId of the plugin
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
     * Build Commandline arguments for the burp extension
     *
     * @return Commandline arguments for the burp extension
     * @throws MojoExecutionException
     */
    protected abstract List<String> createBurpCommandLine() throws MojoExecutionException;

    protected abstract void execute(ProcessBuilder processBuilder) throws MojoExecutionException;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().debug("Skipping execution.");
            return;
        }

        ProcessBuilder burp = createBurpProcessBuilder();
        execute(burp);

        /*getLog().info("Starting headless burp " + getBurpExtenderToRun() + "...");
        getLog().info(StringUtils.join(burp.command().iterator(), " "));
        executeAndRedirectOutput(burp);*/
    }

    protected ProcessBuilder createBurpProcessBuilder() throws MojoExecutionException {
        List<String> burpCommandLine = createBurpCommandLine();

        String extensionClassPath = getBurpExtensionClasspath();
        ProcessBuilder builder = new ProcessBuilder("java", "-Xmx1G", "-Djava.awt.headless=" + headless, "-classpath", extensionClassPath, "burp.StartBurp");
        builder.command().addAll(burpCommandLine);
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

    public void executeAndRedirectOutput(ProcessBuilder processBuilder) {
         /*final InputStream is = burp.getInputStream();
        Thread infoLogThread = new Thread(() -> {
            try {
                BufferedReader burpOutputReader = new BufferedReader(new InputStreamReader(burp.getInputStream()));

                for (String line = burpOutputReader.readLine(); line != null; line = burpOutputReader.readLine()) {
                    getLog().info(line);
                    getLog().info("\033[0m ");
                }
            } catch (IOException e) {
                throw new RuntimeException("There was an error reading the output from Burp.", e);
            } finally {
                IOUtil.close(is);
            }
        });*/

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
                    throw new ProcessExecutionException("Error when ");
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
        public ProcessExecutionException(String message) {
            super(message);
        }

        public ProcessExecutionException(Throwable cause) {
            super(cause);
        }
    }

    public File getBurpSuite() {
        return burpSuite;
    }

    public File getBurpState() {
        return burpState;
    }

    public boolean isHeadless() {
        return headless;
    }

    public boolean isPromptOnExit() {
        return promptOnExit;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isSkip() {
        return skip;
    }
}
