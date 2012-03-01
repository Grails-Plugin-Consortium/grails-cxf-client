package test.mock

import javax.jws.WebMethod
import javax.jws.WebParam
import javax.jws.WebResult
import javax.jws.WebService
import javax.xml.ws.RequestWrapper
import javax.xml.ws.ResponseWrapper

/**
 */
@WebService(targetNamespace = 'http://mock.client.cxf/', name = 'SimpleServicePortType')
interface SimpleServicePortType {

    @WebResult(name = 'return', targetNamespace = '')
    @RequestWrapper(localName = 'simpleMethod2', targetNamespace = 'http://mock.client.cxf/', className = 'test.mock.SimpleMethod2')
    @WebMethod
    @ResponseWrapper(localName = 'simpleMethod2Response', targetNamespace = 'http://mock.client.cxf/', className = 'test.mock.SimpleMethod2Response')
    SimpleResponse simpleMethod2(
            @WebParam(name = 'request', targetNamespace = '')
            SimpleRequest request
    )



    @WebResult(name = 'return', targetNamespace = '')
    @RequestWrapper(localName = 'simpleMethod1', targetNamespace = 'http://mock.client.cxf/', className = 'test.mock.SimpleMethod1')
    @WebMethod
    @ResponseWrapper(localName = 'simpleMethod1Response', targetNamespace = 'http://mock.client.cxf/', className = 'test.mock.SimpleMethod1Response')
    SimpleResponse simpleMethod1(
            @WebParam(name = 'request', targetNamespace = '')
            SimpleRequest request
    )


}