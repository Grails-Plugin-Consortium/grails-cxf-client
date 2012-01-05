package test.mock

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlType

/**
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "simpleResponse", propOrder = [
"isOld",
"status"
])
public class SimpleResponse {

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
    public Boolean isIsOld() {
        return isOld
    }

    /**
     * Sets the value of the isOld property.
     *
     * @param value
     *     allowed object is
     * {@link Boolean}
     *
     */
    public void setIsOld(Boolean value) {
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
    public String getStatus() {
        return status
    }

    /**
     * Sets the value of the status property.
     *
     * @param value
     *     allowed object is
     * {@link String}
     *
     */
    public void setStatus(String value) {
        this.status = value
    }

}
