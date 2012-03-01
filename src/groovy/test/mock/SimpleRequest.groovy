package test.mock

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlType

/**
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = 'simpleRequest', propOrder = [
'name',
'age'
])
class SimpleRequest {

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
    String getName() {
        name
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     * {@link String}
     *
     */
    void setName(String value) {
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
    Integer getAge() {
        age
    }

    /**
     * Sets the value of the age property.
     *
     * @param value
     *     allowed object is
     * {@link Integer}
     *
     */
    void setAge(Integer value) {
        this.age = value
    }

}
