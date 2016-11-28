package de.tu_berlin.formic.client.datatype

import akka.actor.{Actor, ActorRef, Props}
import de.tu_berlin.formic.client.FormicDataType
import de.tu_berlin.formic.client.datatype.AbstractClientDataTypeFactory.NewDataTypeCreated
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.{AbstractDataType, DataTypeName}
import de.tu_berlin.formic.common.message.CreateRequest

import scala.reflect.ClassTag

/**
  * @author Ronny Bräunlich
  */
//Why the ClassTag? See http://stackoverflow.com/questions/18692265/no-classtag-available-for-t-not-for-array
abstract class AbstractClientDataTypeFactory[T <: AbstractDataType : ClassTag, S <: FormicDataType : ClassTag] extends Actor {

  override def receive: Receive = {
    case CreateRequest(_, dataTypeInstanceId, _) =>
      val actor = context.actorOf(Props(createDataType(dataTypeInstanceId)), dataTypeInstanceId.id)
      val wrapper = createWrapperType(dataTypeInstanceId, actor)
      sender ! NewDataTypeCreated(dataTypeInstanceId, actor, wrapper)
  }

  def createDataType(dataTypeInstanceId: DataTypeInstanceId): T

  def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef): S

  val name: DataTypeName
}

object AbstractClientDataTypeFactory {

  case class NewDataTypeCreated(dataTypeInstanceId: DataTypeInstanceId, dataTypeActor: ActorRef, wrapper: FormicDataType)

}


