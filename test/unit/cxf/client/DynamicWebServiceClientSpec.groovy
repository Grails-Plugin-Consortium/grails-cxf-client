package cxf.client

import com.grails.cxf.client.DynamicWebServiceClient
import com.grails.cxf.client.WebServiceClientFactoryImpl
import org.springframework.beans.factory.FactoryBeanNotInitializedException
import spock.lang.Specification
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy

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
                allowChunking: false,
                timeouts: [receiveTimeout: 0, connectionTimeout: 0],
                webServiceClientFactory: factory,
                httpClientPolicy: new HTTPClientPolicy(allowChunking: true, connectionTimeout: 100, receiveTimeout: 200)
        )

        when:
        Object object = client.getObject()

        then:
        object != null
        factory.interfaceMap.containsKey("testService")
        factory.interfaceMap.get("testService").clientInterface == test.mock.SimpleServicePortType
        !factory.interfaceMap.get("testService").security.secured
        !factory.interfaceMap.get("testService").security.allowChunking
        factory.interfaceMap.get("testService").timeouts.receiveTimeout == 0
        factory.interfaceMap.get("testService").timeouts.connectionTimeout == 0
        factory.interfaceMap.get("testService").handler != null
        factory.interfaceMap.get("testService").httpClientPolicy != null
        factory.interfaceMap.get("testService").httpClientPolicy.allowChunking
        factory.interfaceMap.get("testService").httpClientPolicy.connectionTimeout == 100
        factory.interfaceMap.get("testService").httpClientPolicy.receiveTimeout == 200

    }

    def "get a web service client with no policy"() {
        given:
        WebServiceClientFactoryImpl factory = new WebServiceClientFactoryImpl()
        DynamicWebServiceClient client = new DynamicWebServiceClient(
                clientInterface: test.mock.SimpleServicePortType,
                serviceName: "testService",
                serviceEndpointAddress: "http://localhost:8080/cxf-client",
                secured: false,
                allowChunking: true,
                timeouts: [receiveTimeout: 0, connectionTimeout: 0],
                webServiceClientFactory: factory
        )

        when:
        Object object = client.getObject()

        then:
        object != null
        factory.interfaceMap.containsKey("testService")
        factory.interfaceMap.get("testService").clientInterface == test.mock.SimpleServicePortType
        !factory.interfaceMap.get("testService").security.secured
        factory.interfaceMap.get("testService").security.allowChunking
        factory.interfaceMap.get("testService").handler != null
        !factory.interfaceMap.get("testService").httpClientPolicy
        factory.interfaceMap.get("testService").timeouts.receiveTimeout == 0
        factory.interfaceMap.get("testService").timeouts.connectionTimeout == 0


    }

    def "get a web service client with invalid endpoint address"() {
        given:
        WebServiceClientFactoryImpl factory = new WebServiceClientFactoryImpl()
        DynamicWebServiceClient client = new DynamicWebServiceClient(
                clientInterface: test.mock.SimpleServicePortType,
                serviceName: "testService",
                serviceEndpointAddress: "",
                secured: false,
                timeouts: [receiveTimeout: 0, connectionTimeout: 0],
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
                timeouts: [receiveTimeout: 0, connectionTimeout: 0],
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
