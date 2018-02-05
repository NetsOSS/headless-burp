package burp;

import com.google.common.base.Charsets;
import com.google.common.io.Closeables;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShutdownListener {

    private String host = "localhost";
    private int port;
    private String shutdownKey;
    private ShutdownCallback shutdownCallback;
    private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    private final AtomicBoolean shutdownComplete = new AtomicBoolean(false);

    public ShutdownListener(int port, String shutdownKey, ShutdownCallback shutdownCallback) {
        this.port = port;
        this.shutdownKey = shutdownKey;
        this.shutdownCallback = shutdownCallback;
    }

    public final void start() {
        //Register a shutdown handler
        Thread shutdownHook = new Thread(this::shutdown, "headless-burp-proxy Shutdown Hook");
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        ShutdownSocketListener shutdownSocketListener = new ShutdownSocketListener(host, port);
        shutdownSocketListener.start();
    }

    /**
     * Calls shutdown hook and cleans up shutdown listener code, notifies all waiting threads on completion.
     */
    private void shutdown() {
        final boolean shuttingDown = this.shutdownRequested.getAndSet(true);
        if (shuttingDown) {
            return;
        }

        shutdownCallback.shutdown();
        this.shutdownComplete.set(true);
    }

    /**
     * Runnable for waiting on connections to the shutdown socket and handling them.
     */
    private class ShutdownSocketListener {
        private final ServerSocket shutdownSocket;
        private final InetAddress bindHost;
        private final int port;

        private ShutdownSocketListener(String host, int port) {
            this.port = port;
            try {
                this.bindHost = InetAddress.getByName(host);
                this.shutdownSocket = new ServerSocket(this.port, 10, this.bindHost);
            } catch (UnknownHostException uhe) {
                throw new RuntimeException("Failed to create InetAddress for host '" + host + "'", uhe);
            } catch (IOException ioe) {
                throw new RuntimeException("Failed to create shutdown socket on '" + host + "' and " + this.port, ioe);
            }
        }

        public void start() {
            try {
                while (!shutdownSocket.isClosed()) {
                    try {
                        Socket shutdownConnection = shutdownSocket.accept();

                        final BufferedReader reader = new BufferedReader(new InputStreamReader(shutdownConnection.getInputStream(), Charsets.UTF_8));
                        try {
                            final String receivedCommand = reader.readLine();
                            if (shutdownKey.equals(receivedCommand)) {
                                shutdown();
                            }
                        } finally {
                            Closeables.closeQuietly(reader);
                        }
                    } catch (Exception e) {
                        //Ignore
                    }
                }
            } finally {
                try {
                    Closeables.close(shutdownSocket, true);
                } catch (IOException e) {
                    // impossible
                }
            }
        }

        @Override
        public String toString() {
            return "ShutdownSocketListener [bindHost=" + bindHost + ", port=" + port + "]";
        }
    }

    public interface ShutdownCallback {

        /**
         * Called when the application is shutting down, should block until the class is completely shut down.
         */
        void shutdown();
    }
}
