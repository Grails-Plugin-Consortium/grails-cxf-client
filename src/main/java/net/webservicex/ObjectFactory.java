
package net.webservicex;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    private final static QName _String_QNAME = new QName("http://www.webserviceX.NET/", "string");

    public ObjectFactory() {
    }

    public GetQuote createGetQuote() {
        return new GetQuote();
    }

    public GetQuoteResponse createGetQuoteResponse() {
        return new GetQuoteResponse();
    }

    @XmlElementDecl(namespace = "http://www.webserviceX.NET/", name = "string")
    public JAXBElement<String> createString(String value) {
        return new JAXBElement<String>(_String_QNAME, String.class, null, value);
    }

}
