package eu.nets.burp.maven;

import com.google.common.base.Charsets;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.IOUtil;

@Mojo(name = "stop-proxy", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class StopBurpProxyMojo extends AbstractMojo {

    /**
     * Shutdown port.
     */
    @Parameter(defaultValue = "4444")
    private int shutdownPort;

    /**
     * Shutdown key.
     */
    @Parameter(defaultValue = "SHUTDOWN")
    private String shutdownKey;

    /**
     * Skip starting burp proxy.
     */
    @Parameter(defaultValue = "false")
    private boolean skip;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().debug("Skipping execution.");
            return;
        }

        shutdown(shutdownPort, shutdownKey);
    }

    private void shutdown(int port, String key) {
        getLog().info("Stopping Burp proxy...");
        Socket socket = null;
        BufferedWriter outputWriter = null;
        try {
            socket = new Socket("localhost", port);
            outputWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8));

            outputWriter.write(key);
            getLog().info("Burp proxy stopped.");
        } catch (IOException e) {
            getLog().error("Could not connect to localhost:" + port, e);
            getLog().error("Could not stop Burp proxy");
        } finally {
            try {
                IOUtil.close(outputWriter);
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                getLog().error("Could not close socket: " + e);
            }
        }
    }
}
