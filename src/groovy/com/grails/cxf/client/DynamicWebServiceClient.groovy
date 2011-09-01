package com.grails.cxf.client

import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.FactoryBeanNotInitializedException

/**
 * Class used to provide web service clients.  Supports dynamically changing the wsdl document url
 * at runtime, as well as initializing the url from system settings.
 */
public class DynamicWebServiceClient implements FactoryBean<Object> {

    Class<?> clientInterface
    boolean secured
    String serviceEndpointAddress
    String serviceName
    WebServiceClientFactory webServiceClientFactory

    public Object getObject() throws FactoryBeanNotInitializedException, MalformedURLException {
        if(!clientInterface) {
            throw new FactoryBeanNotInitializedException("Web service client cannot be created before setting the clientClass and urlSettingField properties.")
        }
        webServiceClientFactory.getWebServiceClient(clientInterface, serviceName, serviceEndpointAddress,  secured)
    }

    public Class<?> getObjectType() {
        clientInterface
    }

    public boolean isSingleton() {
        true
    }
}
