package com.grails.cxf.client.exception

class CxfClientException extends Throwable {

    /**
     * Serialization Version ID compatibility - indication of what version may be restored.
     */
    private static final long serialVersionUID = 13312399123L

    /**
     * Creates a new instance of UpdateException
     */
    CxfClientException() {
        super()
    }

    CxfClientException(String message, Throwable cause) {
        super(message, cause)
    }

    CxfClientException(Throwable cause) {
        super(cause)
    }

    CxfClientException(String msg) {
        super(msg)
    }
}
