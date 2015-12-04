package org.grails.cxf.client.exception

class UpdateServiceEndpointException extends Throwable {

    /**
     * Serialization Version ID compatibility - indication of what version may be restored.
     */
    private static final long serialVersionUID = 12244556899L

    /**
     * Creates a new instance of UpdateException
     */
    UpdateServiceEndpointException() {
        super()
    }

    UpdateServiceEndpointException(String message, Throwable cause) {
        super(message, cause)
    }

    UpdateServiceEndpointException(Throwable cause) {
        super(cause)
    }


    UpdateServiceEndpointException(String msg) {
        super(msg)
    }
}
