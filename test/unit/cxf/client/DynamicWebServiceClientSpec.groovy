package cxf.client

import com.grails.cxf.client.DynamicWebServiceClient
import com.grails.cxf.client.WebServiceClientFactoryImpl
import spock.lang.Specification
import org.springframework.beans.factory.FactoryBeanNotInitializedException

import grails.test.mixin.TestFor

/**
 */
class DynamicWebServiceClientSpec extends Specification {

    def "get a web service client"() {
        given:
        WebServiceClientFactoryImpl factory = new WebServiceClientFactoryImpl()
        DynamicWebServiceClient client = new DynamicWebServiceClient(
                clientInterface: test.mock.SimpleServicePortType,
                serviceName: "testService",
                serviceEndpointAddress: "http://localhost:8080/cxf-client",
                secured: false,
                webServiceClientFactory: factory)

        when:
        Object object = client.getObject()

        then:
        object != null
        factory.interfaceMap.containsKey("testService")
        factory.interfaceMap.get("testService").clientInterface == test.mock.SimpleServicePortType
        !factory.interfaceMap.get("testService").security.secured
        factory.interfaceMap.get("testService").handler != null
    }

     def "get a web service client with invalid endpoint address"() {
        given:
        WebServiceClientFactoryImpl factory = new WebServiceClientFactoryImpl()
        DynamicWebServiceClient client = new DynamicWebServiceClient(
                clientInterface: test.mock.SimpleServicePortType,
                serviceName: "testService",
                serviceEndpointAddress: "",
                secured: false,
                webServiceClientFactory: factory)

        when:
        Object object = client.getObject()

        then:
        object == null
         FactoryBeanNotInitializedException exception = thrown()
         exception.message.contains("cannot be created")
        !factory.interfaceMap.containsKey("testService")
    }

    def "get a web service client with invalid client interface name"() {
        given:
        WebServiceClientFactoryImpl factory = new WebServiceClientFactoryImpl()
        DynamicWebServiceClient client = new DynamicWebServiceClient(
                serviceName: "testService",
                serviceEndpointAddress: "http://localhost:8080/cxf-client",
                secured: false,
                webServiceClientFactory: factory)

        when:
        Object object = client.getObject()

        then:
        object == null
         FactoryBeanNotInitializedException exception = thrown()
         exception.message.contains("cannot be created")
        !factory.interfaceMap.containsKey("testService")
    }
}
