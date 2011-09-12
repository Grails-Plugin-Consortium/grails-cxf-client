package cxf.client

import com.grails.cxf.client.exception.CxfClientException
import spock.lang.Specification

class CxfClientExceptionSpec extends Specification {

    def "normal exception behavior test with throwable and message"() {
        given:
        String message = "detailMessage"
        Throwable throwable = new Throwable(message)

        when:
        throwException(throwable, message)

        then:
        CxfClientException e = thrown()
        e.message == "detailMessage"
    }

    def "normal exception behavior test with throwable"() {
        given:
        Throwable throwable = new Throwable("some throwable")

        when:
        throwException(throwable)

        then:
        CxfClientException e = thrown()
        e.message == "java.lang.Throwable: some throwable"
    }

    def "normal exception behavior test no message"() {
        when:
        throwException()

        then:
        CxfClientException e = thrown()
        !e.message
    }

    def "normal exception behavior test"() {
        given:
        String message = "detailMessage"

        when:
        throwException(message)

        then:
        CxfClientException e = thrown()
        e.message == message
    }

    def throwException() throws CxfClientException {
        throw new CxfClientException()
    }

    def throwException(String message) throws CxfClientException {
        throw new CxfClientException(message)
    }

    def throwException(Throwable throwable) throws CxfClientException {
        throw new CxfClientException(throwable)
    }

    def throwException(Throwable throwable, String message) throws CxfClientException {
        throw new CxfClientException(message, throwable)
    }
}
