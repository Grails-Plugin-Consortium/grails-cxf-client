package com.grails.cxf.client

import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.FactoryBeanNotInitializedException
import org.springframework.context.MessageSource

/**
 * Class used to provide web service clients.  Supports dynamically changing the wsdl document url
 * at runtime, as well as initializing the url from system settings.
 */
public class DynamicWebServiceClient implements FactoryBean<Object> {

    Class<?> clientInterface
    boolean secured
    String serviceEndpointAddress
    String serviceName
    //todo: need to get this working with auto injection from the plugin
    WebServiceClientFactory webServiceClientFactory
    MessageSource messageSource

    public Object getObject() throws FactoryBeanNotInitializedException, MalformedURLException {
        if(!clientInterface || !serviceEndpointAddress) {
            throw new FactoryBeanNotInitializedException("Web service client cannot be created before setting the clientInterface=${clientInterface} and serviceEndpointAddress=${serviceEndpointAddress} properties")
        }
        webServiceClientFactory.getWebServiceClient(clientInterface, serviceName, serviceEndpointAddress, secured)
    }

    public Class<?> getObjectType() {
        clientInterface
    }

    public boolean isSingleton() {
        true
    }
}
