package com.grails.cxf.client

import com.grails.cxf.client.exception.CxfClientException
import com.grails.cxf.client.exception.UpdateServiceEndpointException
import groovy.transform.Synchronized
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import org.apache.commons.logging.LogFactory
import org.apache.cxf.BusFactory
import org.apache.cxf.endpoint.Client
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.interceptor.Interceptor
import org.apache.cxf.interceptor.LoggingInInterceptor
import org.apache.cxf.interceptor.LoggingOutInterceptor
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.transport.Conduit
import org.apache.cxf.transport.http.HTTPConduit
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy

class WebServiceClientFactoryImpl implements WebServiceClientFactory {

    private static final Log = LogFactory.getLog(this)
    private static final int ZERO = 0
    def interfaceMap = [:]

    /**
     * create and cache the reference to the web service client proxy object
     * @param serviceInterface cxf generated port interface to use for service contract
     * @param serviceName the name of the service for using in cache key
     * @param serviceEndpointAddress url to use when invoking service
     * @param secured whether service is secured
     * @return
     */
    @Synchronized Object getWebServiceClient(Class<?> clientInterface, String serviceName,
                                             String serviceEndpointAddress,
                                             Boolean secured, Boolean allowChunking,
                                             Boolean enableDefaultLoggingInterceptors,
                                             Map timeouts,
                                             List outInterceptors,
                                             List inInterceptors,
                                             List outFaultInterceptors,
                                             HTTPClientPolicy httpClientPolicy) {
        WSClientInvocationHandler handler = new WSClientInvocationHandler(clientInterface)
        Object clientProxy = Proxy.newProxyInstance(clientInterface.classLoader, [clientInterface] as Class[], handler)

        if(serviceEndpointAddress) {
            try {
                if(Log.isDebugEnabled()) { Log.debug("Creating endpoint for service $serviceName using endpoint address $serviceEndpointAddress is secured $secured") }
                assignCxfProxy(clientInterface, serviceEndpointAddress, secured, allowChunking,
                               enableDefaultLoggingInterceptors, timeouts, handler, outInterceptors,
                               inInterceptors, outFaultInterceptors, httpClientPolicy)
            } catch (Exception exception) {
                CxfClientException cxfClientException = new CxfClientException(
                        "Could not create web service client for interface $clientInterface with Service Endpoint Address at $serviceEndpointAddress. Make sure Endpoint URL exists and is accessible.", exception)
                if(Log.isErrorEnabled()) { Log.error(cxfClientException.message, cxfClientException) }
                throw cxfClientException
            }

        } else {
            CxfClientException cxfClientException = new CxfClientException("Web service client failed to initialize with url: $serviceEndpointAddress using secured: $secured")
            if(Log.isErrorEnabled()) { Log.error(cxfClientException.message, cxfClientException) }
            throw cxfClientException
        }

        if(Log.isDebugEnabled()) { Log.debug("Created service $serviceName, caching reference to allow changing url later.") }
        def serviceMap = [clientInterface: clientInterface,
                outInterceptors: outInterceptors,
                inInterceptors: inInterceptors,
                outFaultInterceptors: outFaultInterceptors,
                enableDefaultLoggingInterceptors: enableDefaultLoggingInterceptors,
                timeouts: timeouts,
                handler: handler,
                httpClientPolicy: httpClientPolicy,
                security: [secured: secured, allowChunking: allowChunking]]
        interfaceMap.put(serviceName, serviceMap)

        clientProxy
    }

    /**
     * Method to allow updating endpoint and refreshing proxy reference
     * @param serviceName The name of the service to update
     * @param serviceEndpointAddress The new address to use
     * @throws UpdateServiceEndpointException If endpoint can not be updated
     */
    @Synchronized void updateServiceEndpointAddress(String serviceName, String serviceEndpointAddress)
    throws UpdateServiceEndpointException {
        if(Log.isDebugEnabled()) {
            Log.debug("Changing the service $serviceName endpoint address to $serviceEndpointAddress")
        }

        if(!serviceName || !interfaceMap.containsKey(serviceName)) {
            throw new UpdateServiceEndpointException('Can not update address for service. Must provide a service name.')
        }

        Class<?> clientInterface = interfaceMap.get(serviceName).clientInterface
        def security = interfaceMap.get(serviceName).security
        if(clientInterface) {
            WSClientInvocationHandler handler = interfaceMap.get(serviceName).handler
            List outInterceptors = interfaceMap.get(serviceName).outInterceptors
            List inInterceptors = interfaceMap.get(serviceName).inInterceptors
            List outFaultInterceptors = interfaceMap.get(serviceName).outFaultInterceptors
            Boolean enableDefaultLoggingInterceptors = interfaceMap.get(serviceName).enableDefaultLoggingInterceptors
            HTTPClientPolicy httpClientPolicy = interfaceMap.get(serviceName).httpClientPolicy
            Map timeouts = interfaceMap.get(serviceName).timeouts
            try {
                assignCxfProxy(clientInterface, serviceEndpointAddress,
                               security?.secured ?: false, security?.allowChunking ?: false,
                               enableDefaultLoggingInterceptors,
                               timeouts ?: [receiveTimeout: 60000, connectionTimeout: 30000],
                               handler, outInterceptors, inInterceptors, outFaultInterceptors, httpClientPolicy)
                if(Log.isDebugEnabled()) { Log.debug("Successfully changed the service $serviceName endpoint address to $serviceEndpointAddress") }
            } catch (Exception exception) {
                handler.cxfProxy = null
                throw new UpdateServiceEndpointException("Could not create web service client for Service Endpoint Address at $serviceEndpointAddress.  Make sure Endpoint URL exists and is accessible.", exception)
            }
        } else {
            if(Log.isDebugEnabled()) {
                Log.debug("Unable to find existing client proxy matching name ${serviceName}")
            }
        }
    }

    /**
     * Create the actual cxf client proxy
     * @param serviceInterface cxf generated port interface to use for service contract
     * @param serviceEndpointAddress url to use when invoking service
     * @param secured whether service is secured
     * @param handler ws client invocation handler for the proxy
     */
    private void assignCxfProxy(Class<?> serviceInterface,
                                String serviceEndpointAddress,
                                Boolean secured,
                                Boolean allowChunking,
                                Boolean enableDefaultLoggingInterceptors,
                                Map timeouts,
                                WSClientInvocationHandler handler,
                                List outInterceptors,
                                List inInterceptors,
                                List outFaultInterceptors,
                                HTTPClientPolicy httpClientPolicy) {
        JaxWsProxyFactoryBean clientProxyFactory = new JaxWsProxyFactoryBean(serviceClass: serviceInterface,
                                                                             address: serviceEndpointAddress,
                                                                             bus: BusFactory.defaultBus)
        Object cxfProxy = clientProxyFactory.create()
        addInterceptors(cxfProxy, allowChunking, enableDefaultLoggingInterceptors, timeouts,
                        outInterceptors, inInterceptors, outFaultInterceptors, httpClientPolicy)
        handler.cxfProxy = cxfProxy
    }

    /**
     * Add default interceptors to the client proxy
     * @param cxfProxy proxy class you wish to intercept
     */
    private void addInterceptors(Object cxfProxy, Boolean allowChunking,
                                 Boolean enableDefaultLoggingInterceptors,
                                 Map timeouts, List outInterceptors,
                                 List inInterceptors, List outFaultInterceptors,
                                 HTTPClientPolicy httpClientPolicy) {
        Client client = ClientProxy.getClient(cxfProxy)

        configureReceiveTimeout(client, timeouts, allowChunking, httpClientPolicy)

        //Only provide the default interceptors when no others are defined
        if((outFaultInterceptors?.size() ?: ZERO) == ZERO) {
            client.outFaultInterceptors.add(new CxfClientFaultConverter())
        }

        if(enableDefaultLoggingInterceptors) {
            client.inInterceptors.add(new LoggingInInterceptor())
            client.outInterceptors.add(new LoggingOutInterceptor())
        }

        //add custom interceptors here
        addInterceptors(client.outFaultInterceptors, outFaultInterceptors)
        addInterceptors(client.inInterceptors, inInterceptors)
        addInterceptors(client.outInterceptors, outInterceptors)
    }

    /**
     * Takes a list of generic interceptors and applies them to the appropriate interceptor chain
     * @param clientInterceptors interceptors attached to a Client object (in/out/etc)
     * @param cxfInterceptors interceptors that are configured to be used
     */
    private void addInterceptors(List clientInterceptors, List cxfInterceptors) {
        cxfInterceptors.each {
            if(it instanceof Interceptor || it instanceof CxfClientInterceptor) {
                clientInterceptors.add((it instanceof CxfClientInterceptor) ? it.create() : it)
            }
        }
    }

    /**
     * Applies the Client policy on the Http Conduit.
     * Notes: <br>
     * Conduit handles both the Http and Https protocols. <br>
     * Policies apply to the Conduit. Chunking is applied on the policy to control the mode of transmission.
     * In non-chunking mode, the message is expected to be sent as a single block. In chunking mode, the message
     * can be sent to the server while its being constructed. The server and client are expected to work in
     * parallel during message transmission. <br>CXF by default runs in chunking mode. In addition,
     * chunking mode is not supported by some web services. Thus, Chunking was disabled on the Client policy.
     *
     * @param client the client object
     * @param timeouts map of receive and connection timeout params
     * @param allowChunking boolean for allowing chunking or not on service
     */
    private void configureReceiveTimeout(Client client, Map timeouts, Boolean allowChunking = false, HTTPClientPolicy httpClientPolicy = null) {
        Conduit c = client.conduit
        if(c instanceof HTTPConduit) {
            HTTPConduit conduit = (HTTPConduit) c
            conduit.client = getHttpClientPolicy(httpClientPolicy, timeouts, allowChunking)
        }
    }

    /**
     * Method that will either return the configured HTTPClientPolicy from the config or it will create one using timeouts and chunking.
     * @param httpClientPolicy An existing client policy to use
     * @param timeouts timeout map for receive and connection
     * @param allowChunking allow chunking in the policy
     * @return HTTPClientPolicy
     */
    private HTTPClientPolicy getHttpClientPolicy(HTTPClientPolicy httpClientPolicy, Map timeouts, boolean allowChunking) {
        httpClientPolicy ?: new HTTPClientPolicy(
                receiveTimeout: timeouts.receiveTimeout,
                connectionTimeout: timeouts.connectionTimeout,
                allowChunking: allowChunking
        )
    }

    /**
     * Internal class to invoke the proxy
     */
    private class WSClientInvocationHandler implements InvocationHandler {

        Object cxfProxy
        String clientName

        WSClientInvocationHandler(Class<?> clientInterface) {
            this.clientName = clientInterface.name
        }

        Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(!cxfProxy) {
                String message = "Error invoking method ${method.name} on interface $clientName. Proxy must have failed to initialize."
                if(Log.isErrorEnabled()) { Log.error message }
                throw new CxfClientException(message)
            }

            try {
                method.invoke(cxfProxy, args)
            } catch (Exception e) {
                if(Log.isErrorEnabled()) { Log.error e.message }
                throw new CxfClientException(e)
            }
        }
    }
}
