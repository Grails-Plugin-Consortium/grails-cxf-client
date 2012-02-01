package com.grails.cxf.client

/**
 * Factory used to obtain web service clients as well as dynamically changing their WSDL document URLs.
 */
interface WebServiceClientFactory {

    /**
     * Get a web service client.  This should be invoked at startup to get a web service that can be injected
     * into all classes that depend on the web service client interface.
     *
     * @param clientInterface The interface the web service must implement (this is what will be injected into
     *                        other classes).
     * @param serviceEndpointAddress Url for the service endpoint
     * @param secured Indicate if the web service needs to be secured using digital certificate
     *                        authentication (<code>true</code>) or if it is does not require the certificate
     *                        usage (<code>false</code>), which is the default.
     * @param username Username to use for secured clients.  Ignored if secured is false.
     * @param password Password to use for secured clients.  Ignored if secured is false.
     * @return The web service client.  The returned object will proxy the clientInterface (allowing it
     *         to be injected into other classes as the interface).
     */
    Object getWebServiceClient(Class<?> clientInterface,
                               String serviceName,
                               String serviceEndpointAddress,
                               Boolean secured,
                               Boolean enableDefaultLoggingInterceptors,
                               Map timeouts,
                               List outInterceptors,
                               List inInterceptors,
                               List outFaultInterceptors)
}
