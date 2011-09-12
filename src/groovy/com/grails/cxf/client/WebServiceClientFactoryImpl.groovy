package com.grails.cxf.client

import com.grails.cxf.client.exception.CxfClientException
import com.grails.cxf.client.exception.UpdateServiceEndpointException
import groovy.transform.Synchronized
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import javax.security.auth.callback.Callback
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.callback.UnsupportedCallbackException
import org.apache.commons.logging.LogFactory
import org.apache.cxf.BusFactory
import org.apache.cxf.endpoint.Client
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.interceptor.LoggingInInterceptor
import org.apache.cxf.interceptor.LoggingOutInterceptor
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.transport.Conduit
import org.apache.cxf.transport.http.HTTPConduit
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor
import org.apache.ws.security.WSPasswordCallback
import org.apache.ws.security.handler.WSHandlerConstants

class WebServiceClientFactoryImpl implements WebServiceClientFactory {

    private static final log = LogFactory.getLog(this)
    def interfaceMap = [:]

    /**
     * create and cache the reference to the web service client proxy object
     * @param serviceInterface cxf generated port interface to use for service contract
     * @param serviceName the name of the service for using in cache key
     * @param serviceEndpointAddress url to use when invoking service
     * @param secured whether service is secured with username and password
     * @param username username when using secured=true
     * @param password password when using secured=true
     * @return
     */
    @Synchronized Object getWebServiceClient(Class<?> clientInterface, String serviceName,
                                             String serviceEndpointAddress, boolean secured,
                                             String username, String password) {
        WSClientInvocationHandler handler = new WSClientInvocationHandler(clientInterface)
        Object clientProxy = Proxy.newProxyInstance(clientInterface.classLoader, [clientInterface] as Class[], handler)

        if(serviceEndpointAddress) {
            try {
                if(log.isDebugEnabled()) { log.debug("Creating endpoint for service $serviceName using endpoint address $serviceEndpointAddress is secured $secured") }
                createCxfProxy(clientInterface, serviceEndpointAddress, secured,
                               serviceName, username, password, handler)
            } catch (Exception exception) {
                CxfClientException cxfClientException = new CxfClientException("Could not create web service client for interface $clientInterface with Service Endpoint Address at $serviceEndpointAddress. Make sure Endpoint URL exists and is accessible.", exception)
                if(log.isErrorEnabled()) { log.error(cxfClientException.message, cxfClientException) }
                throw cxfClientException
            }

        } else {
            CxfClientException cxfClientException = new CxfClientException("Web service client failed to initialize with url: $serviceEndpointAddress using secured: $secured")
            if(log.isErrorEnabled()) { log.error(cxfClientException.message, cxfClientException) }
            throw cxfClientException
        }

        if(log.isDebugEnabled()) { log.debug("Created service $serviceName, caching reference to allow changing url later.") }
        def serviceMap = [clientInterface: clientInterface,
                handler: handler,
                security: [secured: secured, username: username, password: password]]
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
        if(log.isDebugEnabled()) {
            log.debug("Changing the service $serviceName endpoint address to $serviceEndpointAddress")
        }

        if(!serviceName || !interfaceMap.containsKey(serviceName)) {
            throw new UpdateServiceEndpointException('Can not update address for service. Must provide a service name.')
        }

        Class<?> clientInterface = interfaceMap.get(serviceName).clientInterface
        def security = interfaceMap.get(serviceName).security
        if(clientInterface) {
            WSClientInvocationHandler handler = interfaceMap.get(serviceName).handler
            // is used only in secure mode to extract the username/password
            try {
                createCxfProxy(clientInterface, serviceEndpointAddress,
                               security?.secured ?: false, serviceName,
                               security.username, security.password, handler)
                if(log.isDebugEnabled()) { log.debug("Successfully changed the service $serviceName endpoint address to $serviceEndpointAddress") }
            } catch (Exception exception) {
                handler.cxfProxy = null
                throw new UpdateServiceEndpointException("Could not create web service client for Service Endpoint Address at $serviceEndpointAddress.  Make sure Endpoint URL exists and is accessible.", exception)
            }
        } else {
            if(log.isDebugEnabled()) {
                log.debug("Unable to find existing client proxy matching name ${serviceName}")
            }
        }
    }

    /**
     * Create the actual cxf client proxy
     * @param serviceInterface cxf generated port interface to use for service contract
     * @param serviceEndpointAddress url to use when invoking service
     * @param secured whether service is secured with username and password
     * @param serviceName the name of the service for using in cache key
     * @param username username when using secured=true
     * @param password password when using secured=true
     * @param handler ws client invocation handler for the proxy
     */
    private void createCxfProxy(Class<?> serviceInterface, String serviceEndpointAddress,
                                boolean secured, String serviceName, String username,
                                String password, WSClientInvocationHandler handler) {
        JaxWsProxyFactoryBean clientProxyFactory = new JaxWsProxyFactoryBean(serviceClass: serviceInterface,
                                                                             address: serviceEndpointAddress,
                                                                             bus: BusFactory.defaultBus)
        Object cxfProxy = clientProxyFactory.create()
        if(secured) {
            secureClient(cxfProxy, username, password)
        }
        addInterceptors(cxfProxy)
        handler.cxfProxy = cxfProxy
    }

    /**
     * Add default interceptors to the client proxy
     * @param cxfProxy proxy class you wish to intercept
     */
    private void addInterceptors(Object cxfProxy) {
        Client client = ClientProxy.getClient(cxfProxy)
        client.outFaultInterceptors.add(new CxfClientFaultConverter())
        client.inInterceptors.add(new LoggingInInterceptor())
        client.outInterceptors.add(new LoggingOutInterceptor())
    }

    /**
     * Method used to secure client using wss4j.  
     * @param cxfProxy proxy class you wish to secure
     * @param username username to use
     * @param password password to use
     * @see "http://cxf.apache.org/docs/ws-security.html"
     */
    private void secureClient(Object cxfProxy, String username, String password) {
        if(username?.trim()?.length() < 1 || password?.length() < 1) {
            throw new CxfClientException('Username and password are not configured for calling secure web services')
        }

        Client client = ClientProxy.getClient(cxfProxy)
        // applies the policy to the request
        configurePolicy(client)

        Map<String, Object> outProps = [:]
        outProps.put(WSHandlerConstants.ACTION, org.apache.ws.security.handler.WSHandlerConstants.USERNAME_TOKEN)
        // User in keystore
        outProps.put(WSHandlerConstants.USER, username)
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, org.apache.ws.security.WSConstants.PW_TEXT)
        outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler() {

            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[0]
                pc.password = password
            }
        })
        // This callback is used to specify password for given user for keystore
        //outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, PasswordHandler.class.getName())
        // Configuration for accessing private key in keystore
        //outProps.put(WSHandlerConstants.SIG_PROP_FILE, "resources/SecurityOut.properties")
        //outProps.put(WSHandlerConstants.SIG_KEY_ID, "DirectReference")

        //        client.getOutInterceptors().add(new DOMOutputHandler())
        client.outInterceptors.add(new WSS4JOutInterceptor(outProps))
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
     */
    private void configurePolicy(Client client) {
        Conduit c = client.conduit
        if(c instanceof HTTPConduit) {
            HTTPConduit conduit = (HTTPConduit) c
            conduit.client = new HTTPClientPolicy(connectionTimeout: 0, allowChunking: false)
        }
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
                if(log.isErrorEnabled()) { log.error message }
                throw new CxfClientException(message)

            }

            try {
                method.invoke(cxfProxy, args)
            } catch (Exception e) {
                if(log.isErrorEnabled()) { log.error e.message }
                throw new CxfClientException("Error invoking method ${method.name} on interface $clientName. Make sure valid clientInterface and serviceEndpointAddress are set.", e)
            }
        }
    }
}
