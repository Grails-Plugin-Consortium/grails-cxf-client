package com.grails.cxf.client.security

import org.apache.wss4j.dom.WSConstants
import org.apache.wss4j.dom.handler.WSHandlerConstants

import javax.security.auth.callback.CallbackHandler

import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor

import spock.lang.Specification

import com.grails.cxf.client.exception.CxfClientException

class DefaultSecurityOutInterceptorSpec extends Specification {

    def "construct default security out interceptor"() {
        given:
        DefaultSecurityOutInterceptor defaultSecurityOutInterceptor = new DefaultSecurityOutInterceptor(username: "bob", password: "pass")

        when:
        def wss4j = defaultSecurityOutInterceptor.create()

        then:
        wss4j instanceof WSS4JOutInterceptor
        wss4j.getOption(WSHandlerConstants.USER) == "bob"
        wss4j.getOption(WSHandlerConstants.PASSWORD_TYPE) == WSConstants.PW_TEXT
        wss4j.getOption(WSHandlerConstants.PW_CALLBACK_REF) instanceof CallbackHandler
    }

    def "set default security out interceptor with empty password"() {
        given:
        DefaultSecurityOutInterceptor defaultSecurityOutInterceptor = new DefaultSecurityOutInterceptor(username: "bob", password: "")

        when:
        def wss4j = defaultSecurityOutInterceptor.create()

        then:
        !wss4j
        thrown(CxfClientException)
    }

    def "set default security out interceptor with null password"() {
        given:
        DefaultSecurityOutInterceptor defaultSecurityOutInterceptor = new DefaultSecurityOutInterceptor(username: "bob", password: null)

        when:
        def wss4j = defaultSecurityOutInterceptor.create()

        then:
        !wss4j
        thrown(CxfClientException)
    }
}
