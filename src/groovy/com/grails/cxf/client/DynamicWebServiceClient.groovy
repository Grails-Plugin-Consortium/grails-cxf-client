package com.grails.cxf.client

import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.FactoryBeanNotInitializedException
import org.springframework.context.MessageSource

/**
 * Provides web service clients.  Supports dynamically changing the wsdl document url
 * at runtime, as well as initializing the url from system settings.
 */
class DynamicWebServiceClient implements FactoryBean<Object> {

    String wsdlURL
    String wsdlServiceName
    String wsdlEndpointName
    Class<?> clientInterface
    Boolean enableDefaultLoggingInterceptors
    def clientPolicyMap = [:]
    String serviceEndpointAddress
    String serviceName
    WebServiceClientFactory webServiceClientFactory
    MessageSource messageSource
    def outInterceptors = []
    def inInterceptors = []
    def inFaultInterceptors = []
    def outFaultInterceptors = []
    def httpClientPolicy
    String proxyFactoryBindingId
    String secureSocketProtocol

    Object getObject() throws FactoryBeanNotInitializedException, MalformedURLException {
        if(!clientInterface || !serviceEndpointAddress) {
            throw new FactoryBeanNotInitializedException("""Web service client cannot be created
before setting the clientInterface=${clientInterface} and
serviceEndpointAddress=${serviceEndpointAddress} properties""")
        }
        webServiceClientFactory.getWebServiceClient(wsdlURL,
                                                    wsdlServiceName,
                                                    wsdlEndpointName,
                                                    clientInterface,
                                                    serviceName,
                                                    serviceEndpointAddress,
                                                    enableDefaultLoggingInterceptors,
                                                    clientPolicyMap,
                                                    outInterceptors,
                                                    inInterceptors,
                                                    inFaultInterceptors,
                                                    outFaultInterceptors,
                                                    httpClientPolicy,
                                                    proxyFactoryBindingId,
                                                    secureSocketProtocol)
    }

    Class<?> getObjectType() {
        clientInterface
    }

    boolean isSingleton() {
        true
    }
}
