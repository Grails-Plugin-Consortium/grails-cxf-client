package com.grails.cxf.client.exception

/**
 */
class CxfClientException extends Throwable implements Serializable {

    /**
     * Serialization Version ID compatibility - indication of what version may be restored.
     */
    static final long serialVersionUID = 13312399123L

    /**
     * Creates a new instance of UpdateException
     */
    public CxfClientException() {
        super()
    }

    public CxfClientException(String message, Throwable cause) {
        super(message, cause)
    }

    public CxfClientException(Throwable cause) {
        super(cause)
    }


    public CxfClientException(String msg) {
        super(msg)
    }
}
