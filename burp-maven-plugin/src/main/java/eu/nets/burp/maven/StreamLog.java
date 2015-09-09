package eu.nets.burp.maven;

import java.io.PrintStream;
import org.apache.maven.plugin.logging.Log;

/**
 * a Simple Maven Log that outputs to a Stream
 */
class StreamLog implements Log {

    static final int DEBUG = 0;

    static final int INFO = 1;

    static final int WARN = 2;

    static final int ERROR = 3;

    private int level = INFO;

    public void setLevel(int level) {
        if (level < DEBUG || level > ERROR) {
            throw new IllegalStateException("invalid level: " + level);
        }
        this.level = level;
    }

    private final PrintStream s;

    StreamLog(PrintStream s) {
        this.s = s;
    }

    @Override
    public void debug(CharSequence content) {
        if (isDebugEnabled()) {
            s.println(content);
        }
    }

    @Override
    public void debug(CharSequence content, Throwable error) {
        if (isDebugEnabled()) {
            s.println(content);
        }
    }

    @Override
    public void debug(Throwable error) {
        if (isDebugEnabled()) {
            error.printStackTrace(s);
        }
    }

    @Override
    public void error(CharSequence content) {
        if (isErrorEnabled()) {
            s.println(content);
        }
    }

    @Override
    public void error(CharSequence content, Throwable error) {
        error(content);
        error(error);
    }

    @Override
    public void error(Throwable error) {
        if (isErrorEnabled()) {
            error.printStackTrace(s);
        }
    }

    @Override
    public void info(CharSequence content) {
        if (isInfoEnabled()) {
            s.println(content);
        }
    }

    @Override
    public void info(CharSequence content, Throwable error) {
        info(content);
        info(error);
    }

    @Override
    public void info(Throwable error) {
        if (isInfoEnabled()) {
            error.printStackTrace(s);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return level >= DEBUG;
    }

    @Override
    public boolean isErrorEnabled() {
        return level >= ERROR;
    }

    @Override
    public boolean isInfoEnabled() {
        return level >= INFO;
    }

    @Override
    public boolean isWarnEnabled() {
        return level >= WARN;
    }

    @Override
    public void warn(CharSequence content) {
        if (isWarnEnabled()) {
            s.println(content);
        }
    }

    @Override
    public void warn(CharSequence content, Throwable error) {
        warn(content);
        warn(error);
    }

    @Override
    public void warn(Throwable error) {
        if (isWarnEnabled()) {
            error.printStackTrace(s);
        }
    }
}