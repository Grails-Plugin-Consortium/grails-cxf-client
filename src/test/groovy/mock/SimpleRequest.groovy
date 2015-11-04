package mock

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = 'simpleRequest', propOrder = ['name', 'age'])
class SimpleRequest {

    String name
    Integer age
}
