package com.grails.cxf.client

/**
 */
import org.apache.cxf.interceptor.ClientFaultConverter;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;

/**
 * Overriding implementation of the org.apache.cxf.interceptor.ClientFaultConverter
 * that allows for the fault itself to be null.
 */
public class CxfClientFaultConverter extends ClientFaultConverter {

    @Override
	public void handleMessage(Message message) {

        Fault fault = (Fault) message.getContent(Exception.class);

        if(fault?.detail) {
            processFaultDetail(fault, message);
        }
    }
}