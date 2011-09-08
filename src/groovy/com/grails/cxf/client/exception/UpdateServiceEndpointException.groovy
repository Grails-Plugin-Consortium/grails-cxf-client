package com.grails.cxf.client.exception

/**
 */
public class UpdateServiceEndpointException extends Throwable implements Serializable {

    /**
     * Serialization Version ID compatibility - indication of what version may be restored.
     */
    private static final long serialVersionUID = 12244556899L

    /**
     * Creates a new instance of UpdateException
     */
    public UpdateServiceEndpointException() {
        super()
    }

    public UpdateServiceEndpointException(String message, Throwable cause) {
        super(message, cause)
    }

    public UpdateServiceEndpointException(Throwable cause) {
        super(cause)
    }


    public UpdateServiceEndpointException(String msg) {
        super(msg)
    }
}