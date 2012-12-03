package com.grails.cxf.client

import org.apache.cxf.interceptor.Interceptor
import org.apache.cxf.message.Message

interface CxfClientInterceptor {

    Interceptor<? extends Message> create()
}
