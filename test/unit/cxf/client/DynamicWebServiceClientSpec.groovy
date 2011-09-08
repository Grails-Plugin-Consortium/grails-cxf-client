package cxf.client

import com.grails.cxf.client.DynamicWebServiceClient
import com.grails.cxf.client.WebServiceClientFactoryImpl
import spock.lang.Specification
import org.springframework.beans.factory.FactoryBeanNotInitializedException

/**
 */
class DynamicWebServiceClientSpec extends Specification {

    def "get a web service client"() {
        given:
        WebServiceClientFactoryImpl factory = new WebServiceClientFactoryImpl()
        DynamicWebServiceClient client = new DynamicWebServiceClient(
                clientInterface: cxf.client.mock.SimpleServicePortType,
                serviceName: "testService",
                serviceEndpointAddress: "http://localhost:8080/cxf-client",
                secured: false,
                securedName: "testService",
                webServiceClientFactory: factory)

        when:
        Object object = client.getObject()

        then:
        object != null
        factory.interfaceMap.containsKey("testService")
        factory.interfaceMap.get("testService") == cxf.client.mock.SimpleServicePortType
        factory.securityMap.containsKey("testService")
        factory.handlerMap.containsKey(cxf.client.mock.SimpleServicePortType)
    }

     def "get a web service client with invalid endpoint address"() {
        given:
        WebServiceClientFactoryImpl factory = new WebServiceClientFactoryImpl()
        DynamicWebServiceClient client = new DynamicWebServiceClient(
                clientInterface: cxf.client.mock.SimpleServicePortType,
                serviceName: "testService",
                serviceEndpointAddress: "",
                secured: false,
                securedName: "testService",
                webServiceClientFactory: factory)

        when:
        Object object = client.getObject()

        then:
        object == null
         FactoryBeanNotInitializedException exception = thrown()
         exception.message.contains("cannot be created")
        !factory.interfaceMap.containsKey("testService")
        !factory.securityMap.containsKey("testService")
        !factory.handlerMap.containsKey(cxf.client.mock.SimpleServicePortType)
    }

    def "get a web service client with invalid client interface name"() {
        given:
        WebServiceClientFactoryImpl factory = new WebServiceClientFactoryImpl()
        DynamicWebServiceClient client = new DynamicWebServiceClient(
                serviceName: "testService",
                serviceEndpointAddress: "http://localhost:8080/cxf-client",
                secured: false,
                securedName: "testService",
                webServiceClientFactory: factory)

        when:
        Object object = client.getObject()

        then:
        object == null
         FactoryBeanNotInitializedException exception = thrown()
         exception.message.contains("cannot be created")
        !factory.interfaceMap.containsKey("testService")
        !factory.securityMap.containsKey("testService")
        !factory.handlerMap.containsKey(cxf.client.mock.SimpleServicePortType)
    }
}
