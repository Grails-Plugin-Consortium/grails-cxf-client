package com.grails.cxf.client

import org.apache.cxf.message.Message
import org.apache.cxf.message.MessageImpl
import spock.lang.Specification

class CxfClientFaultConverterSpec extends Specification {

    def "attempt to handle a message"() {
        given:
        CxfClientFaultConverter converter = new CxfClientFaultConverter()

        when:
        Message message = new MessageImpl();
        message.put(Message.HTTP_REQUEST_METHOD, "GET");
        converter.handleMessage(message)

        then:
        message
        converter
    }

    def "attempt to handle a null message"() {
        given:
        CxfClientFaultConverter converter = new CxfClientFaultConverter()

        when:
        Message message = new MessageImpl()
        converter.handleMessage(message)

        then:
        !message
        converter
    }
}
