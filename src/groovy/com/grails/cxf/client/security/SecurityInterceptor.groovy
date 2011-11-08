package com.grails.cxf.client.security

/**
 */
interface SecurityInterceptor {

    org.apache.cxf.interceptor.Interceptor<? extends org.apache.cxf.message.Message> create()

}
