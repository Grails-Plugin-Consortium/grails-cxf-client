package com.grails.cxf.client.security

import com.grails.cxf.client.exception.CxfClientException
import javax.security.auth.callback.Callback
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.callback.UnsupportedCallbackException
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor
import org.apache.ws.security.WSPasswordCallback
import org.apache.ws.security.handler.WSHandlerConstants

/**
 */
class DefaultSecurityOutInterceptor implements SecurityInterceptor {

    def password
    def username

    WSS4JOutInterceptor create() {
        if(username?.trim()?.length() < 1 || password?.length() < 1) {
            throw new CxfClientException('Username and password are not configured for calling secure web services')
        }

        Map<String, Object> outProps = [:]
        outProps.put(WSHandlerConstants.ACTION, org.apache.ws.security.handler.WSHandlerConstants.USERNAME_TOKEN)
        outProps.put(WSHandlerConstants.USER, username)
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, org.apache.ws.security.WSConstants.PW_TEXT)
        outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler() {

            void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[0]
                pc.password = password
            }
        })
        new WSS4JOutInterceptor(outProps)
    }
}
