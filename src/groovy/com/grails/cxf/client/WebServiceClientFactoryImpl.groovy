package com.grails.cxf.client

import com.grails.cxf.client.exception.CxfClientException
import com.grails.cxf.client.exception.UpdateServiceEndpointException
import groovy.transform.Synchronized
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.cxf.BusFactory
import org.apache.cxf.configuration.jsse.TLSClientParameters
import org.apache.cxf.configuration.security.FiltersType
import org.apache.cxf.endpoint.Client
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.interceptor.Interceptor
import org.apache.cxf.interceptor.LoggingInInterceptor
import org.apache.cxf.interceptor.LoggingOutInterceptor
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.transport.Conduit
import org.apache.cxf.transport.http.HTTPConduit
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy

import javax.xml.namespace.QName
import javax.xml.ws.BindingProvider
import java.lang.reflect.*

class WebServiceClientFactoryImpl implements WebServiceClientFactory {

    private static final int ZERO = 0
    private static final int RECEIVE_TIMEOUT = 60000
    private static final int CONNECTION_TIMEOUT = 30000
    def interfaceMap = [:]
    private static final Log log = LogFactory.getLog(this)

    /**
     * Create and cache the reference to the web service client proxy object
     * @param wsdlURL
     * @param wsdlServiceName
     * @param wsdlEndpointName
     * @param clientInterface cxf generated port interface to use for service contract
     * @param serviceName the name of the service for using in cache key
     * @param serviceEndpointAddress url to use when invoking service
     * @param enableDefaultLoggingInterceptors
     * @param clientPolicyMap
     * @param outInterceptors
     * @param inInterceptors
     * @param inFaultInterceptors
     * @param outFaultInterceptors
     * @param httpClientPolicy
     * @param proxyFactoryBindingId
     * @param secureSocketProtocol
     * @param requestContext
     * @param tlsClientParameters
     * @return
     */
    @Synchronized Object getWebServiceClient(String wsdlURL, String wsdlServiceName, String wsdlEndpointName,
                                             Class<?> clientInterface, String serviceName,
                                             String serviceEndpointAddress,
                                             Boolean enableDefaultLoggingInterceptors,
                                             Map clientPolicyMap,
                                             List outInterceptors,
                                             List inInterceptors,
                                             List inFaultInterceptors,
                                             List outFaultInterceptors,
                                             HTTPClientPolicy httpClientPolicy,
                                             String proxyFactoryBindingId,
                                             String secureSocketProtocol,
                                             Map<String, Object> requestContext,
                                             Map tlsClientParameters) {
        WSClientInvocationHandler handler = new WSClientInvocationHandler(clientInterface)
        Object clientProxy = Proxy.newProxyInstance(clientInterface.classLoader, [clientInterface, BindingProvider.class] as Class[], handler)

        if(serviceEndpointAddress) {
            try {
                log.debug("Creating endpoint for service $serviceName using endpoint address $serviceEndpointAddress")
                assignCxfProxy(wsdlURL, wsdlServiceName, wsdlEndpointName, clientInterface, serviceEndpointAddress,
                               enableDefaultLoggingInterceptors, clientPolicyMap, handler, outInterceptors,
                               inInterceptors, inFaultInterceptors, outFaultInterceptors, httpClientPolicy, proxyFactoryBindingId, secureSocketProtocol,
                               requestContext, tlsClientParameters)
            } catch(Exception exception) {
                CxfClientException cxfClientException = new CxfClientException(
                        "Could not create web service client for interface $clientInterface with Service Endpoint Address at $serviceEndpointAddress. Make sure Endpoint URL exists and is accessible.", exception)
                log.error(cxfClientException.message, cxfClientException)
                throw cxfClientException
            }

        } else {
            CxfClientException cxfClientException = new CxfClientException("Web service client failed to initialize with url: $serviceEndpointAddress")
            log.error(cxfClientException.message, cxfClientException)
            throw cxfClientException
        }

        log.debug("Created service $serviceName, caching reference to allow changing url later.")
        def serviceMap = [wsdlURL: wsdlURL,
                wsdlServiceName: wsdlServiceName,
                wsdlEndpointName: wsdlEndpointName,
                clientInterface: clientInterface,
                outInterceptors: outInterceptors,
                inInterceptors: inInterceptors,
                inFaultInterceptors: inFaultInterceptors,
                outFaultInterceptors: outFaultInterceptors,
                enableDefaultLoggingInterceptors: enableDefaultLoggingInterceptors,
                clientPolicyMap: clientPolicyMap,
                handler: handler,
                httpClientPolicy: httpClientPolicy,
                proxyFactoryBindingId: proxyFactoryBindingId,
                secureSocketProtocol: secureSocketProtocol,
                requestContext: requestContext,
                tlsClientParameters: tlsClientParameters]
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
        log.debug("Changing the service $serviceName endpoint address to $serviceEndpointAddress")

        if(!serviceName || !interfaceMap.containsKey(serviceName)) {
            throw new UpdateServiceEndpointException("Can not update address for service $serviceName. Must provide a service name.")
        }

        Class<?> clientInterface = interfaceMap.get(serviceName).clientInterface
        if(clientInterface) {
            assignCxfProxyFromInterfaceMap(serviceName, clientInterface, serviceEndpointAddress)
        } else {
            log.debug("Unable to find existing client proxy matching name ${serviceName}")
            throw new UpdateServiceEndpointException("Unable to find existing client proxy matching name ${serviceName}")
        }
    }

    Map getServiceMap(String serviceName) {
        interfaceMap[serviceName]
    }

    String getServiceEndpointAddress(String serviceName) {
        def cxfProxy = interfaceMap[serviceName]?.handler?.cxfProxy
        cxfProxy ? ClientProxy.getClient(cxfProxy)?.conduit?.target?.address?.value : null
    }

    private void assignCxfProxyFromInterfaceMap(String serviceName, Class<?> clientInterface, String serviceEndpointAddress) {
        String wsdlURL = interfaceMap.get(serviceName).wsdlURL
        String wsdlServiceName = interfaceMap.get(serviceName).wsdlServiceName
        String wsdlEndpointName = interfaceMap.get(serviceName).wsdlEndpointName
        WSClientInvocationHandler handler = interfaceMap.get(serviceName).handler
        List outInterceptors = interfaceMap.get(serviceName).outInterceptors
        List inInterceptors = interfaceMap.get(serviceName).inInterceptors
        List inFaultInterceptors = interfaceMap.get(serviceName).inFaultInterceptors
        List outFaultInterceptors = interfaceMap.get(serviceName).outFaultInterceptors
        Boolean enableDefaultLoggingInterceptors = interfaceMap.get(serviceName).enableDefaultLoggingInterceptors
        HTTPClientPolicy httpClientPolicy = interfaceMap.get(serviceName).httpClientPolicy
        String proxyFactoryBindingId = interfaceMap.get(serviceName).proxyFactoryBindingId
        Map clientPolicyMap = interfaceMap.get(serviceName).clientPolicyMap
        String secureSocketProtocol = interfaceMap.get(serviceName).secureSocketProtocol
        Map requestContext = interfaceMap.get(serviceName).requestContext
        Map tlsClientParameters = interfaceMap.get(serviceName).tlsClientParameters
        try {
            assignCxfProxy(wsdlURL, wsdlServiceName, wsdlEndpointName, clientInterface, serviceEndpointAddress,
                           enableDefaultLoggingInterceptors,
                           clientPolicyMap ?: [receiveTimeout: RECEIVE_TIMEOUT, connectionTimeout: CONNECTION_TIMEOUT, allowChunking: true, contentType: 'text/xml; charset=UTF-8'],
                           handler, outInterceptors, inInterceptors, inFaultInterceptors, outFaultInterceptors, httpClientPolicy, proxyFactoryBindingId,
                           secureSocketProtocol, requestContext, tlsClientParameters)
            log.debug("Successfully changed the service $serviceName endpoint address to $serviceEndpointAddress")
        } catch(Exception exception) {
            handler.cxfProxy = null
            def message = "Could not create web service client for Service Endpoint Address at $serviceEndpointAddress.  Make sure Endpoint URL exists and is accessible."
            log.error exception
            throw new UpdateServiceEndpointException(message, exception)
        }
    }

    /**
     * Create the actual cxf client proxy
     * @param wsdlURL
     * @param wsdlServiceName
     * @param wsdlEndpointName
     * @param serviceInterface
     * @param serviceEndpointAddress
     * @param enableDefaultLoggingInterceptors
     * @param clientPolicyMap
     * @param handler
     * @param outInterceptors
     * @param inInterceptors
     * @param inFaultInterceptors
     * @param outFaultInterceptors
     * @param httpClientPolicy
     * @param proxyFactoryBindingId
     * @param secureSocketProtocol
     */
    private void assignCxfProxy(String wsdlURL,
                                String wsdlServiceName,
                                String wsdlEndpointName,
                                Class<?> serviceInterface,
                                String serviceEndpointAddress,
                                Boolean enableDefaultLoggingInterceptors,
                                Map clientPolicyMap,
                                WSClientInvocationHandler handler,
                                List outInterceptors,
                                List inInterceptors,
                                List inFaultInterceptors,
                                List outFaultInterceptors,
                                HTTPClientPolicy httpClientPolicy,
                                String proxyFactoryBindingId,
                                String secureSocketProtocol,
                                Map<String, Object> requestContext,
                                Map tlsClientParameters) {
        JaxWsProxyFactoryBean clientProxyFactory = new JaxWsProxyFactoryBean(serviceClass: serviceInterface,
                                                                             address: serviceEndpointAddress,
                                                                             bus: BusFactory.defaultBus)
        if(wsdlURL) {
            clientProxyFactory.wsdlURL = wsdlURL
        }
        if(wsdlServiceName) {
            clientProxyFactory.serviceName = QName.valueOf(wsdlServiceName)
        }
        if(wsdlEndpointName) {
            clientProxyFactory.endpointName = QName.valueOf(wsdlEndpointName)
        }
        if(proxyFactoryBindingId) {
            clientProxyFactory.bindingId = proxyFactoryBindingId
        }

        Object cxfProxy = clientProxyFactory.create()
        addInterceptors(cxfProxy, enableDefaultLoggingInterceptors, clientPolicyMap,
                        outInterceptors, inInterceptors, inFaultInterceptors, outFaultInterceptors, httpClientPolicy)
        if(secureSocketProtocol || tlsClientParameters?.secureSocketProtocol != null) {
            setSsl(cxfProxy, secureSocketProtocol, tlsClientParameters)
        }

        assignContexts(cxfProxy, requestContext)

        handler.cxfProxy = cxfProxy

    }

    private static void assignContexts(Object cxfProxy, Map<String, Object> requestContext) {
        if(requestContext?.size() > 0) {
            cxfProxy.requestContext.putAll(requestContext)
        }
    }


    private static void setSsl(Object cxfProxy, String secureSocketProtocol, Map tlsClientParameters) {
        //secureSocketProtocol should be one in Constants file, but let them set it to whatever
        if(![CxfClientConstants.SSL_PROTOCOL_SSLV3, CxfClientConstants.SSL_PROTOCOL_TLSV1].contains(secureSocketProtocol)) {
            log.info "The provided secureSocketProtocol of $secureSocketProtocol might not be recognized"
        }

        Client client = ClientProxy.getClient(cxfProxy);
        Conduit c = client.getConduit();
        if(c instanceof HTTPConduit) {
            HTTPConduit conduit = (HTTPConduit) c;
            TLSClientParameters parameters = conduit.tlsClientParameters;
            if(parameters == null) {
                parameters = new TLSClientParameters();
            }

            //todo: still need to vet out how to do keymanager/trustmanagers

            if(tlsClientParameters?.useHttpsURLConnectionDefaultSslSocketFactory != null) {
                parameters.useHttpsURLConnectionDefaultSslSocketFactory = tlsClientParameters.useHttpsURLConnectionDefaultSslSocketFactory
            }

            if(tlsClientParameters?.cipherSuitesFilter != null) {
                parameters.cipherSuitesFilter = new FiltersType()
                if(tlsClientParameters?.cipherSuitesFilter?.exclude) {
                    parameters.cipherSuitesFilter.exclude.addAll(tlsClientParameters.cipherSuitesFilter.exclude.collect())
                }

                if(tlsClientParameters?.cipherSuitesFilter?.include) {
                    parameters.cipherSuitesFilter.include.addAll(tlsClientParameters.cipherSuitesFilter.include.collect())
                }
            }

            if(tlsClientParameters?.useHttpsURLConnectionDefaultHostnameVerifier != null) {
                parameters.useHttpsURLConnectionDefaultHostnameVerifier = tlsClientParameters.useHttpsURLConnectionDefaultHostnameVerifier
            }

            if(tlsClientParameters?.disableCNCheck != null) {
                parameters.disableCNCheck = tlsClientParameters.disableCNCheck
            }
            if(tlsClientParameters?.sslCacheTimeout != null) {
                parameters.sslCacheTimeout = tlsClientParameters.sslCacheTimeout
            }

            parameters.setSecureSocketProtocol(secureSocketProtocol ?: tlsClientParameters.secureSocketProtocol);
            conduit.tlsClientParameters = parameters
        }
    }

    /**
     * Add default interceptors to the client proxy
     * @param cxfProxy proxy class you wish to intercept
     */
    private static void addInterceptors(Object cxfProxy,
                                        Boolean enableDefaultLoggingInterceptors,
                                        Map clientPolicyMap,
                                        List outInterceptors,
                                        List inInterceptors,
                                        List inFaultInterceptors,
                                        List outFaultInterceptors,
                                        HTTPClientPolicy httpClientPolicy) {
        Client client = ClientProxy.getClient(cxfProxy)

        configureReceiveTimeout(client, clientPolicyMap, httpClientPolicy)

        try {
            wireDefaultInterceptors(client, outFaultInterceptors, inFaultInterceptors, enableDefaultLoggingInterceptors)
        } catch(Exception e) {
            println e
        }

        //add custom interceptors here
        addInterceptors(client.outFaultInterceptors, outFaultInterceptors)
        addInterceptors(client.inFaultInterceptors, inFaultInterceptors)
        addInterceptors(client.inInterceptors, inInterceptors)
        addInterceptors(client.outInterceptors, outInterceptors)
    }

    /**
     * Wire the default interceptors as necessary
     * @param client The client to wire for
     * @param outFaultInterceptors Out Fault Interceptors
     * @param inFaultInterceptors In Fault Interceptors
     * @param enableDefaultLoggingInterceptors Enable the default logging interceptors in addition to any custom ones
     */
    private static void wireDefaultInterceptors(Client client, List outFaultInterceptors, List inFaultInterceptors, Boolean enableDefaultLoggingInterceptors) {
//Only provide the default interceptors when no others are defined
        if((outFaultInterceptors?.size() ?: ZERO) == ZERO) {
            client.outFaultInterceptors.add(new CxfClientFaultConverter())
        }

        if((inFaultInterceptors?.size() ?: ZERO) == ZERO) {
            client.inFaultInterceptors.add(new CxfClientFaultConverter())
        }

        if(enableDefaultLoggingInterceptors) {
            client.inInterceptors.add(new LoggingInInterceptor())
            client.outInterceptors.add(new LoggingOutInterceptor())
        }
    }

    /**
     * Takes a list of generic interceptors and applies them to the appropriate interceptor chain
     * @param clientInterceptors interceptors attached to a Client object (in/out/etc)
     * @param cxfInterceptors interceptors that are configured to be used
     */
    private static void addInterceptors(List clientInterceptors, List cxfInterceptors) {
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
     * @param clientPolicyMap map of receive and connection timeout/chunking params
     */
    private static void configureReceiveTimeout(Client client, Map clientPolicyMap, HTTPClientPolicy httpClientPolicy = null) {
        Conduit c = client.conduit
        if(c instanceof HTTPConduit) {
            HTTPConduit conduit = (HTTPConduit) c
            conduit.client = getHttpClientPolicy(httpClientPolicy, clientPolicyMap)
        }
    }

    /**
     * Method that will either return the configured HTTPClientPolicy from the config or it will create one using clientPolicyMap and chunking.
     * @param httpClientPolicy An existing client policy to use
     * @param clientPolicyMap timeout/chunking map for receive and connection
     * @return HTTPClientPolicy
     */
    private static HTTPClientPolicy getHttpClientPolicy(HTTPClientPolicy httpClientPolicy, Map clientPolicyMap) {
        httpClientPolicy ?: new HTTPClientPolicy(
                receiveTimeout: clientPolicyMap.receiveTimeout,
                connectionTimeout: clientPolicyMap.connectionTimeout,
                allowChunking: clientPolicyMap.allowChunking,
                contentType: clientPolicyMap.contentType
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

        /**
         * invoke the service method on the proxy
         * @param proxy the proxy object
         * @param method method to invoke
         * @param args any method params
         * @return response from the method
         * @throws Throwable any exceptions that occur
         */
        Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(!cxfProxy) {
                String message = "Error invoking method ${method.name} on interface $clientName. Proxy must have failed to initialize."
                log.error message
                throw new CxfClientException(message)
            }

            try {
                method.invoke(cxfProxy, args)
            } catch(InvocationTargetException e) {
                log.error e.targetException.message
                throw e.targetException
            } catch(UndeclaredThrowableException e) {
                log.error e.cause.message
                throw e.cause
            } catch(Exception e) {
                log.error e.message
                throw e
            }
        }
    }
}
