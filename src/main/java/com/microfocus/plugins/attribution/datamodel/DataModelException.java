package com.microfocus.plugins.attribution.datamodel;

public class DataModelException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataModelException() {
    }

    public DataModelException(String message) {
        super(message);
    }

    public DataModelException(Throwable cause) {
        super(cause);
    }

    public DataModelException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataModelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
