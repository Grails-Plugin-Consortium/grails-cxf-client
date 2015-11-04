
package net.webservicex;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getQuoteResult"
})
@XmlRootElement(name = "GetQuoteResponse")
public class GetQuoteResponse {

    @XmlElement(name = "GetQuoteResult")
    protected String getQuoteResult;

    /**
     * Gets the value of the getQuoteResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGetQuoteResult() {
        return getQuoteResult;
    }

    /**
     * Sets the value of the getQuoteResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGetQuoteResult(String value) {
        this.getQuoteResult = value;
    }

}
