package com.grails.cxf.client.security

import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor
import org.apache.ws.security.handler.WSHandlerConstants
import spock.lang.Specification

/**
 */
class DefaultSecurityOutInterceptorSpec extends Specification {

    def "default security out interceptor injection test"() {
        given:
        DefaultSecurityOutInterceptor defaultSecurityOutInterceptor = new DefaultSecurityOutInterceptor(username: "bob", password: "pass")

        when:
        def wss4j = defaultSecurityOutInterceptor.create()

        then:
        wss4j instanceof WSS4JOutInterceptor
        wss4j.getOption(WSHandlerConstants.USER) == "bob"
        wss4j.getOption(WSHandlerConstants.PASSWORD_TYPE) == org.apache.ws.security.WSConstants.PW_TEXT
    }
}
