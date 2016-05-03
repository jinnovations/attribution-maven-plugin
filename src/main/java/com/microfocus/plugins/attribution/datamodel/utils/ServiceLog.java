package com.microfocus.plugins.attribution.datamodel.utils;

import org.apache.maven.plugin.logging.SystemStreamLog;

public class ServiceLog extends SystemStreamLog {
    public enum LogLevel {
        NONE(0), DEBUG(1), INFO(2), WARN(3), ERROR(4);

        private int rank;

        private LogLevel(int rank) {
            this.rank = rank;
        }

        public boolean equalsOrIsMoreRestrictiveThan(LogLevel logLevel) {
            return (this.rank >= logLevel.rank);
        }
    }

    private LogLevel currentLogLevel = LogLevel.INFO;

    public void setLogLevel(LogLevel logLevel) {
        this.currentLogLevel = logLevel;
    }

    public LogLevel getLogLevel() {
        return this.currentLogLevel;
    }

    @Override
    public boolean isDebugEnabled() {
        return currentLogLevel.equalsOrIsMoreRestrictiveThan(LogLevel.DEBUG);
    }

    @Override
    public boolean isInfoEnabled() {
        return currentLogLevel.equalsOrIsMoreRestrictiveThan(LogLevel.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return currentLogLevel.equalsOrIsMoreRestrictiveThan(LogLevel.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return currentLogLevel.equalsOrIsMoreRestrictiveThan(LogLevel.ERROR);
    }

    @Override
    public void debug(CharSequence content) {
        if (isDebugEnabled()) {
            super.debug(content);
        }
    }

    @Override
    public void debug(CharSequence content, Throwable error) {
        if (isDebugEnabled()) {
            super.debug(content, error);
        }
    }

    @Override
    public void debug(Throwable error) {
        if (isDebugEnabled()) {
            super.debug(error);
        }
    }

    @Override
    public void info(CharSequence content) {
        if (isInfoEnabled()) {
            super.info(content);
        }
    }

    @Override
    public void info(CharSequence content, Throwable error) {
        if (isInfoEnabled()) {
            super.info(content, error);
        }
    }

    @Override
    public void info(Throwable error) {
        if (isInfoEnabled()) {
            super.info(error);
        }
    }

    @Override
    public void warn(CharSequence content) {
        if (isWarnEnabled()) {
            super.warn(content);
        }
    }

    @Override
    public void warn(CharSequence content, Throwable error) {
        if (isWarnEnabled()) {
            super.warn(content, error);
        }
    }

    @Override
    public void warn(Throwable error) {
        if (isWarnEnabled()) {
            super.warn(error);
        }
    }

    @Override
    public void error(CharSequence content) {
        if (isErrorEnabled()) {
            super.error(content);
        }
    }

    @Override
    public void error(CharSequence content, Throwable error) {
        if (isErrorEnabled()) {
            super.error(content, error);
        }
    }

    @Override
    public void error(Throwable error) {
        if (isErrorEnabled()) {
            super.error(error);
        }
    }
}
