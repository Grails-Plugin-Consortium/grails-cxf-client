package com.grails.cxf.client

/**
 */
interface CxfClientInterceptor {

    org.apache.cxf.interceptor.Interceptor<? extends org.apache.cxf.message.Message> create()

}
