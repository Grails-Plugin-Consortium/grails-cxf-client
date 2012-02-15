package test.mock

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlType

/**
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = 'simpleResponse', propOrder = [
'isOld',
'status'
])
class SimpleResponse {

    protected Boolean isOld
    protected String status

    /**
     * Gets the value of the isOld property.
     *
     * @return
     * possible object is
     * {@link Boolean}
     *
     */
    Boolean isIsOld() {
        isOld
    }

    /**
     * Sets the value of the isOld property.
     *
     * @param value
     *     allowed object is
     * {@link Boolean}
     *
     */
    void setIsOld(Boolean value) {
        this.isOld = value
    }

    /**
     * Gets the value of the status property.
     *
     * @return
     * possible object is
     * {@link String}
     *
     */
    String getStatus() {
        status
    }

    /**
     * Sets the value of the status property.
     *
     * @param value
     *     allowed object is
     * {@link String}
     *
     */
    void setStatus(String value) {
        this.status = value
    }

}
