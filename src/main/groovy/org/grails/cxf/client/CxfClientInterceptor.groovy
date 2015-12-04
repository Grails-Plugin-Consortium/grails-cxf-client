package org.grails.cxf.client

import org.apache.cxf.message.Message

interface CxfClientInterceptor {

    org.apache.cxf.interceptor.Interceptor<? extends Message> create()
}
