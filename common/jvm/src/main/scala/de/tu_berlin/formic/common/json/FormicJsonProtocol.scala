package de.tu_berlin.formic.common.json

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.message.{CreateResponse, FormicMessage}
import spray.json._

/**
  * This class represents the server-side JSON de-/serialization logic. Because all the shared
  * libraries present couldn't provide the desired capabilties related to polymorphism,
  * two separate implementations for client and server have to be provided.
  * @author Ronny Bräunlich
  */
object FormicJsonProtocol extends DefaultJsonProtocol{

  implicit object FormicMessageFormat extends RootJsonFormat[FormicMessage]{
    override def read(json: JsValue): FormicMessage = {
      json match {
        case obj:JsObject if obj.fields.size == 1 => json.convertTo[CreateResponse]
      }
    }

    override def write(obj: FormicMessage): JsValue = {
      obj match {
        case createResponse:CreateResponse => createResponse.toJson
      }
    }

  }

  implicit val instanceIdFormatter = jsonFormat1(DataTypeInstanceId.apply)
  //Messages
  implicit val createFormatter = jsonFormat1(CreateResponse)
}
