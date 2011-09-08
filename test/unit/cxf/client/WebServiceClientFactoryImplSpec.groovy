package cxf.client

import com.grails.cxf.client.WebServiceClientFactoryImpl
import spock.lang.Specification
import com.grails.cxf.client.exception.UpdateServiceEndpointException

/**
 */
class WebServiceClientFactoryImplSpec extends Specification {

    def "create web service client using factory method"() {
        given:
        WebServiceClientFactoryImpl factory = new WebServiceClientFactoryImpl()

        when:
        Object webServiceClient = factory.getWebServiceClient(cxf.client.mock.SimpleServicePortType, "testService", "http://localhost:8080/cxf-client", false, "testUser", "testPassword")

        then:
        webServiceClient != null
        factory.interfaceMap.containsKey("testService")
        factory.interfaceMap.get("testService") == cxf.client.mock.SimpleServicePortType
        factory.securityMap.containsKey("testService")
        factory.handlerMap.containsKey(cxf.client.mock.SimpleServicePortType)
    }

    def "create web service client using factory method and change url"() {
        given:
        WebServiceClientFactoryImpl factory = new WebServiceClientFactoryImpl()

        when: "we create an initial service"
        Object webServiceClient = factory.getWebServiceClient(cxf.client.mock.SimpleServicePortType, "testService", "http://localhost:8080/cxf-client/old", false, "testUser", "testPassword")

        then: "we should have some stuff hooked up here"
        webServiceClient != null
        factory.interfaceMap.containsKey("testService")
        factory.interfaceMap.get("testService") == cxf.client.mock.SimpleServicePortType
        factory.securityMap.containsKey("testService")
        factory.handlerMap.containsKey(cxf.client.mock.SimpleServicePortType)
        factory.handlerMap.get(cxf.client.mock.SimpleServicePortType).cxfProxy.h.client.currentRequestContext.get("org.apache.cxf.message.Message.ENDPOINT_ADDRESS") == "http://localhost:8080/cxf-client/old"

        when: "change the url to something new"
        factory.updateServiceEndpointAddress("testService", "http://localhost:8080/cxf-client/new", false)

        then: "all things should still remain in cache, but the url should have changed"
        factory.interfaceMap.containsKey("testService")
        factory.interfaceMap.get("testService") == cxf.client.mock.SimpleServicePortType
        factory.securityMap.containsKey("testService")
        factory.handlerMap.containsKey(cxf.client.mock.SimpleServicePortType)
        factory.handlerMap.get(cxf.client.mock.SimpleServicePortType).cxfProxy.h.client.currentRequestContext.get("org.apache.cxf.message.Message.ENDPOINT_ADDRESS") == "http://localhost:8080/cxf-client/new"
    }

    def "create web service client using factory method and change url on invalid name"() {
        given:
        WebServiceClientFactoryImpl factory = new WebServiceClientFactoryImpl()

        when: "we create an initial service"
        Object webServiceClient = factory.getWebServiceClient(cxf.client.mock.SimpleServicePortType, "testService", "http://localhost:8080/cxf-client/old", false, "testUser", "testPassword")

        then: "we should have some stuff hooked up here"
        webServiceClient != null
        factory.interfaceMap.containsKey("testService")
        factory.interfaceMap.get("testService") == cxf.client.mock.SimpleServicePortType
        factory.securityMap.containsKey("testService")
        factory.handlerMap.containsKey(cxf.client.mock.SimpleServicePortType)
        factory.handlerMap.get(cxf.client.mock.SimpleServicePortType).cxfProxy.h.client.currentRequestContext.get("org.apache.cxf.message.Message.ENDPOINT_ADDRESS") == "http://localhost:8080/cxf-client/old"

        when: "change the url to something new using invalid name"
        factory.updateServiceEndpointAddress("unknownService", "http://localhost:8080/cxf-client/new", false)

        then: "all things should still remain in cache and the url should not have changed and exception should be thrown"
        UpdateServiceEndpointException exception = thrown()
        exception.message.contains("Must provide a service name")
        !factory.interfaceMap.containsKey("unknownService")
        !factory.securityMap.containsKey("unknownService")
        factory.interfaceMap.containsKey("testService")
        factory.interfaceMap.get("testService") == cxf.client.mock.SimpleServicePortType
        factory.securityMap.containsKey("testService")
        factory.handlerMap.containsKey(cxf.client.mock.SimpleServicePortType)
        factory.handlerMap.get(cxf.client.mock.SimpleServicePortType).cxfProxy.h.client.currentRequestContext.get("org.apache.cxf.message.Message.ENDPOINT_ADDRESS") == "http://localhost:8080/cxf-client/old"
    }
}


