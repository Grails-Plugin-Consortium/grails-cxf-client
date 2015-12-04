package org.grails.cxf.client

import org.grails.cxf.client.exception.UpdateServiceEndpointException
import spock.lang.Specification

class UpdateServiceEndpointExceptionSpec extends Specification {

    def "normal exception behavior test"() {
        when:
        throwException()

        then:
        UpdateServiceEndpointException e = thrown()
        e.message == "detailMessage"
    }

    def throwException() throws UpdateServiceEndpointException {
        throw new UpdateServiceEndpointException("detailMessage")
    }
}
