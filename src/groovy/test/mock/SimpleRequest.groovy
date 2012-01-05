package test.mock

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlType

/**
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "simpleRequest", propOrder = [
"name",
"age"
])
public class SimpleRequest {

    protected String name
    protected Integer age

    /**
     * Gets the value of the name property.
     *
     * @return
     * possible object is
     * {@link String}
     *
     */
    public String getName() {
        return name
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     * {@link String}
     *
     */
    public void setName(String value) {
        this.name = value
    }

    /**
     * Gets the value of the age property.
     *
     * @return
     * possible object is
     * {@link Integer}
     *
     */
    public Integer getAge() {
        return age
    }

    /**
     * Sets the value of the age property.
     *
     * @param value
     *     allowed object is
     * {@link Integer}
     *
     */
    public void setAge(Integer value) {
        this.age = value
    }

}
