log4j = {
    appenders {
        console name: 'stdout'
    }

    root {
        info('stdout')
    }

    error 'org.codehaus.groovy.grails',
          'org.springframework',
          'org.hibernate',
          'net.sf.ehcache.hibernate'

    info 'com.grails.cxf.client',
         'com.grails.cxf.client.security',
         'com.grails.cxf.client.exception'
}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"

cxf {
    client {
        //Another real service to use against wsd2java script
        stockQuoteClient {
            wsdl = "http://www.webservicex.net/stockquote.asmx?WSDL"
            clientInterface = net.webservicex.StockQuoteSoap
            serviceEndpointAddress = "http://www.webservicex.net/stockquote.asmx"
            receiveTimeout = 120000 //2min
            connectionTimeout = 120000 //2min
            requestContext = ["requestKey": "requestValue"]
        }

        stockQuoteClient2 {
            wsdl = "http://www.webservicex.net/stockquote.asmx?WSDL"
            clientInterface = net.webservicex.StockQuoteSoap
            serviceEndpointAddress = "http://www.webservicex.net/stockquote.asmx"
            receiveTimeout = 2000
            connectionTimeout = 2000
            requestContext = ["requestKey2": "requestValue2"]
            contentType = 'application/soap+xml; charset=UTF-8'
        }
    }
}
