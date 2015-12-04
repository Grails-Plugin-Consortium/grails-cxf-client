package org.grails.cxf.client.security

import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor
import org.apache.wss4j.common.ext.WSPasswordCallback
import org.apache.wss4j.dom.WSConstants
import org.apache.wss4j.dom.handler.WSHandlerConstants
import org.grails.cxf.client.CxfClientInterceptor
import org.grails.cxf.client.exception.CxfClientException

import javax.security.auth.callback.Callback
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.callback.UnsupportedCallbackException

class DefaultSecurityOutInterceptor implements CxfClientInterceptor {

    String password
    String username

    WSS4JOutInterceptor create() {
        if(!username?.trim() || !password) {
            throw new CxfClientException('Username and password are not configured for calling secure web services')
        }

        Map<String, Object> outProps = [:]
        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN)
        outProps.put(WSHandlerConstants.USER, username)
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT)
        outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler() {

            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                WSPasswordCallback pc = callbacks[0]
                pc.password = password
            }
        })
        new WSS4JOutInterceptor(outProps)
    }
}
