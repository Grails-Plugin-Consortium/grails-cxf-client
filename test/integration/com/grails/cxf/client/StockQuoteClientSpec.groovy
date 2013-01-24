package com.grails.cxf.client

import grails.plugin.spock.IntegrationSpec
import net.webservicex.StockQuoteSoap
import org.bouncycastle.asn1.x509.qualified.TypeOfBiometricData
import javax.xml.ws.BindingProvider
import org.apache.cxf.jaxws.JaxWsClientProxy
import javax.xml.namespace.QName

/**
 */
class StockQuoteClientSpec extends IntegrationSpec {

    StockQuoteSoap stockQuoteClient

    def "test the normal invocation of a client"() {
        when:
        def response = stockQuoteClient.getQuote(symbol)
        def xml = new XmlSlurper().parseText(response)

        println stockQuoteClient

        then:
        response
        xml.Stock.Symbol.text() == symbol
        Double.parseDouble(xml.Stock.Last.text()) > 0.0d //dear lord!

        where:
        symbol << ["GOOG","APPL","T"]
    }
}
