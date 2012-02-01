package cxf.client

import com.grails.cxf.client.CxfClientFaultConverter
import com.grails.cxf.client.WebServiceClientFactoryImpl
import com.grails.cxf.client.exception.UpdateServiceEndpointException
import org.apache.cxf.interceptor.LoggingInInterceptor
import org.apache.cxf.interceptor.LoggingOutInterceptor
import spock.lang.Specification

/**
 */
class WebServiceClientFactoryImplSpec extends Specification {

    def "create web service client using factory method"() {
        given:
        WebServiceClientFactoryImpl factory = new WebServiceClientFactoryImpl()

        when:
        Object webServiceClient = factory.getWebServiceClient(test.mock.SimpleServicePortType, "testService", "http://localhost:8080/cxf-client", false, false, [receiveTimeout: 0, connectionTimeout: 0], [new LoggingOutInterceptor()], [new LoggingInInterceptor()], [new CxfClientFaultConverter()])

        then:
        webServiceClient != null
        factory.interfaceMap.containsKey("testService")
        factory.interfaceMap.get("testService").clientInterface == test.mock.SimpleServicePortType
        factory.interfaceMap.get("testService").inInterceptors instanceof List
        factory.interfaceMap.get("testService").outInterceptors instanceof List
        factory.interfaceMap.get("testService").outFaultInterceptors instanceof List
        factory.interfaceMap.get("testService").inInterceptors.size() > 0
        factory.interfaceMap.get("testService").outInterceptors.size() > 0
        factory.interfaceMap.get("testService").outFaultInterceptors.size() > 0
        factory.interfaceMap.get("testService").timeouts.connectionTimeout == 0
        factory.interfaceMap.get("testService").timeouts.receiveTimeout == 0
        !factory.interfaceMap.get("testService").security.secured
        factory.interfaceMap.get("testService").handler != null
    }

    def "create web service client using factory method and change url"() {
        given:
        WebServiceClientFactoryImpl factory = new WebServiceClientFactoryImpl()

        when: "we create an initial service"
        Object webServiceClient = factory.getWebServiceClient(test.mock.SimpleServicePortType, "testService", "http://localhost:8080/cxf-client/old", false, false, [receiveTimeout: 0, connectionTimeout: 0], [new LoggingOutInterceptor()], [new LoggingInInterceptor()], [new CxfClientFaultConverter()])

        then: "we should have some stuff hooked up here"
        webServiceClient != null
        factory.interfaceMap.containsKey("testService")
        factory.interfaceMap.get("testService").clientInterface == test.mock.SimpleServicePortType
        factory.interfaceMap.get("testService").inInterceptors instanceof List
        factory.interfaceMap.get("testService").outInterceptors instanceof List
        factory.interfaceMap.get("testService").outFaultInterceptors instanceof List
        factory.interfaceMap.get("testService").inInterceptors.size() > 0
        factory.interfaceMap.get("testService").outInterceptors.size() > 0
        factory.interfaceMap.get("testService").outFaultInterceptors.size() > 0
        factory.interfaceMap.get("testService").timeouts.connectionTimeout == 0
        factory.interfaceMap.get("testService").timeouts.receiveTimeout == 0
        !factory.interfaceMap.get("testService").security.secured
        factory.interfaceMap.get("testService").handler != null
        factory.interfaceMap.get("testService").handler.cxfProxy.h.client.currentRequestContext.get("org.apache.cxf.message.Message.ENDPOINT_ADDRESS") == "http://localhost:8080/cxf-client/old"

        when: "change the url to something new"
        factory.updateServiceEndpointAddress("testService", "http://localhost:8080/cxf-client/new")

        then: "all things should still remain in cache, but the url should have changed"
        factory.interfaceMap.containsKey("testService")
        factory.interfaceMap.get("testService").clientInterface == test.mock.SimpleServicePortType
        factory.interfaceMap.get("testService").inInterceptors instanceof List
        factory.interfaceMap.get("testService").outInterceptors instanceof List
        factory.interfaceMap.get("testService").outFaultInterceptors instanceof List
        factory.interfaceMap.get("testService").inInterceptors.size() > 0
        factory.interfaceMap.get("testService").outInterceptors.size() > 0
        factory.interfaceMap.get("testService").outFaultInterceptors.size() > 0
        factory.interfaceMap.get("testService").timeouts.connectionTimeout == 0
        factory.interfaceMap.get("testService").timeouts.receiveTimeout == 0
        !factory.interfaceMap.get("testService").security.secured
        factory.interfaceMap.get("testService").handler != null
        factory.interfaceMap.get("testService").handler.cxfProxy.h.client.currentRequestContext.get("org.apache.cxf.message.Message.ENDPOINT_ADDRESS") == "http://localhost:8080/cxf-client/new"
    }

    def "create web service client using factory method and change url on invalid name"() {
        given:
        WebServiceClientFactoryImpl factory = new WebServiceClientFactoryImpl()

        when: "we create an initial service"
        Object webServiceClient = factory.getWebServiceClient(test.mock.SimpleServicePortType, "testService", "http://localhost:8080/cxf-client/old", false, false, [receiveTimeout: 0, connectionTimeout: 0], [new LoggingOutInterceptor()], [new LoggingInInterceptor()], [new CxfClientFaultConverter()])

        then: "we should have some stuff hooked up here"
        webServiceClient != null
        factory.interfaceMap.containsKey("testService")
        factory.interfaceMap.get("testService").clientInterface == test.mock.SimpleServicePortType
        factory.interfaceMap.get("testService").inInterceptors instanceof List
        factory.interfaceMap.get("testService").outInterceptors instanceof List
        factory.interfaceMap.get("testService").outFaultInterceptors instanceof List
        factory.interfaceMap.get("testService").inInterceptors.size() > 0
        factory.interfaceMap.get("testService").outInterceptors.size() > 0
        factory.interfaceMap.get("testService").outFaultInterceptors.size() > 0
        factory.interfaceMap.get("testService").timeouts.connectionTimeout == 0
        factory.interfaceMap.get("testService").timeouts.receiveTimeout == 0
        !factory.interfaceMap.get("testService").security.secured
        factory.interfaceMap.get("testService").handler != null
        factory.interfaceMap.get("testService").handler.cxfProxy.h.client.currentRequestContext.get("org.apache.cxf.message.Message.ENDPOINT_ADDRESS") == "http://localhost:8080/cxf-client/old"

        when: "change the url to something new using invalid name"
        factory.updateServiceEndpointAddress("unknownService", "http://localhost:8080/cxf-client/new")

        then: "all things should still remain in cache and the url should not have changed and exception should be thrown"
        UpdateServiceEndpointException exception = thrown()
        exception.message.contains("Must provide a service name")
        !factory.interfaceMap.containsKey("unknownService")
        webServiceClient != null
        factory.interfaceMap.containsKey("testService")
        factory.interfaceMap.get("testService").clientInterface == test.mock.SimpleServicePortType
        factory.interfaceMap.get("testService").inInterceptors instanceof List
        factory.interfaceMap.get("testService").outInterceptors instanceof List
        factory.interfaceMap.get("testService").outFaultInterceptors instanceof List
        factory.interfaceMap.get("testService").inInterceptors.size() > 0
        factory.interfaceMap.get("testService").outInterceptors.size() > 0
        factory.interfaceMap.get("testService").outFaultInterceptors.size() > 0
        factory.interfaceMap.get("testService").timeouts.connectionTimeout == 0
        factory.interfaceMap.get("testService").timeouts.receiveTimeout == 0
        !factory.interfaceMap.get("testService").security.secured
        factory.interfaceMap.get("testService").handler != null
        factory.interfaceMap.get("testService").handler.cxfProxy.h.client.currentRequestContext.get("org.apache.cxf.message.Message.ENDPOINT_ADDRESS") == "http://localhost:8080/cxf-client/old"
    }
}


