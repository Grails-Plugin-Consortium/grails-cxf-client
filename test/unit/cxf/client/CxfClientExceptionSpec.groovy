package cxf.client

import spock.lang.Specification
import com.grails.cxf.client.exception.CxfClientException

class CxfClientExceptionSpec extends Specification {

    def "normal exception behavior test"() {
        when:
        throwException()

        then:
        CxfClientException e = thrown()
        e.message == "detailMessage"
    }

    def throwException() throws CxfClientException {
        throw new CxfClientException("detailMessage")
    }
}
