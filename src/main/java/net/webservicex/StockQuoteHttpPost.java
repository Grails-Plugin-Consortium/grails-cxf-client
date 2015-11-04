package net.webservicex;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

@WebService(targetNamespace = "http://www.webserviceX.NET/", name = "StockQuoteHttpPost")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface StockQuoteHttpPost {

    @WebResult(name = "string", targetNamespace = "http://www.webserviceX.NET/", partName = "Body")
    @WebMethod(operationName = "GetQuote")
    public java.lang.String getQuote(
            @WebParam(partName = "symbol", name = "symbol", targetNamespace = "http://www.webserviceX.NET/")
            java.lang.String symbol
    );
}
