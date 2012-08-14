package com.grails.cxf.client

import org.apache.cxf.transports.http.configuration.HTTPClientPolicy
import com.grails.cxf.client.exception.UpdateServiceEndpointException

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
    Object getWebServiceClient(String wsdlURL, 
                               String wsdlServiceName, 
                               String wsdlEndpointName, 
                               Class<?> clientInterface,
                               String serviceName,
                               String serviceEndpointAddress,
                               Boolean enableDefaultLoggingInterceptors,
                               Map clientPolicyMap,
                               List outInterceptors,
                               List inInterceptors,
                               List outFaultInterceptors,
                               HTTPClientPolicy httpClientPolicy,
                               String proxyFactoryBindingId)

	/**
	 * Method to allow updating endpoint and refreshing proxy reference
	 * @param serviceName The name of the service to update
	 * @param serviceEndpointAddress The new address to use
	 * @throws com.grails.cxf.client.exception.UpdateServiceEndpointException If endpoint can not be updated
	 */
	void updateServiceEndpointAddress(String serviceName, String serviceEndpointAddress) throws UpdateServiceEndpointException

	/**
	 * Return the endpoint address this service is currently using.
	 * @param serviceName name of the service
	 * @return the service's current endpoint address (or null if not found)
	 */
	String getServiceEndpointAddress(String serviceName)
}
