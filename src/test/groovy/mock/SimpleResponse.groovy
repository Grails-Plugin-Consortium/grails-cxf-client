package mock

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = 'simpleResponse', propOrder = ['isOld', 'status'])
class SimpleResponse {

    Boolean isOld
    String status
}
