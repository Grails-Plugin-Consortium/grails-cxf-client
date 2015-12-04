package org.grails.cxf.client

import grails.test.mixin.integration.Integration
import net.webservicex.StockQuoteSoap
import org.grails.cxf.client.exception.UpdateServiceEndpointException
import spock.lang.Ignore
import spock.lang.Specification

import javax.xml.ws.WebServiceException

@Integration
class StockQuoteClientSpec extends Specification {

    StockQuoteSoap stockQuoteClient
    StockQuoteSoap stockQuoteClient2
    WebServiceClientFactory webServiceClientFactory

    @Ignore('Service provider having issues out of disk space 2014-01-21')
    def "test the normal invocation of a client"() {
        when:
        def response = stockQuoteClient.getQuote(symbol)
        def xml = new XmlSlurper().parseText(response)

        then:
        response
        xml.Stock.Symbol.text() == symbol
        Double.parseDouble(xml.Stock.Last.text()) > 0.0d //dear lord!

        where:
        symbol << ["GOOG", "AAPL", "BBY"]
    }

    def "test the normal invocation of the same client different config"() {
        when:
        def response = stockQuoteClient2.getQuote(symbol)

        then:
        thrown(WebServiceException)

        where:
        symbol << ["GOOG", "AAPL", "BBY"]
    }

    def "test the contentType xml of live service"() {
        when:
        Map map = webServiceClientFactory.getServiceMap('stockQuoteClient')
        println map

        then:
        map.clientPolicyMap.contentType == 'text/xml; charset=UTF-8'
        map.clientPolicyMap.connectionTimeout == 120000
        map.clientPolicyMap.receiveTimeout == 120000
        map.clientPolicyMap.allowChunking == false
        map.requestContext == ['requestKey': 'requestValue']
    }

    def "test the contentType soap of live service"() {
        when:
        Map map = webServiceClientFactory.getServiceMap('stockQuoteClient2')

        then:
        map.clientPolicyMap.contentType == 'application/soap+xml; charset=UTF-8'
        map.clientPolicyMap.connectionTimeout == 2000 as String
        map.clientPolicyMap.receiveTimeout == 2000 as String
        map.clientPolicyMap.allowChunking == false
        map.requestContext == ['requestKey2': 'requestValue2']
    }

    @Ignore('Service provider having issues out of disk space 2014-01-21')
    def "test changing the url of the service"(){
        when:
        def response = stockQuoteClient.getQuote('GOOG')
        def xml = new XmlSlurper().parseText(response)

        then:
        response
        xml.Stock.Symbol.text() == 'GOOG'
        Double.parseDouble(xml.Stock.Last.text()) > 0.0d //dear lord!

        when: 'change the url to something that will break'
        webServiceClientFactory.updateServiceEndpointAddress('stockQuoteClient','http://www.webservicex.net/broken.asmx')
        stockQuoteClient.getQuote('GOOG')

        then:
        thrown(WebServiceException)


        when: 'change it back so that it works again'
        webServiceClientFactory.updateServiceEndpointAddress('stockQuoteClient','http://www.webservicex.net/stockquote.asmx')
        response = stockQuoteClient.getQuote('GOOG')

        then:
        response
        xml.Stock.Symbol.text() == 'GOOG'
        Double.parseDouble(xml.Stock.Last.text()) > 0.0d //dear lord!
    }

    def "test changing the url of an invalid service"(){
        when:
        webServiceClientFactory.updateServiceEndpointAddress('stockQuoteClient_broken','http://www.webservicex.net/stockquote.asmx')

        then:
        thrown(UpdateServiceEndpointException)
    }
}
