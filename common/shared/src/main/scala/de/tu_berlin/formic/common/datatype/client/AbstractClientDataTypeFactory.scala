package de.tu_berlin.formic.common.datatype.client

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.{LocalCreateRequest, NewDataTypeCreated, WrappedCreateRequest}
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
import de.tu_berlin.formic.common.message.CreateRequest

import scala.reflect.ClassTag

/**
  * @author Ronny Bräunlich
  */
//Why the ClassTag? See http://stackoverflow.com/questions/18692265/no-classtag-available-for-t-not-for-array
abstract class AbstractClientDataTypeFactory[T <: AbstractClientDataType : ClassTag, S <: FormicDataType : ClassTag] extends Actor with ActorLogging {

  override def receive: Receive = {
    case WrappedCreateRequest(outgoingConnection, req) =>
      log.debug(s"Factory for $name received CreateRequest: $req from sender: $sender")
      val id: DataTypeInstanceId = req.dataTypeInstanceId
      val actor = context.actorOf(Props(createDataType(id, outgoingConnection)), id.id)
      val wrapper = createWrapperType(id, actor)
      sender ! NewDataTypeCreated(id, actor, wrapper)

    case local: LocalCreateRequest =>
      log.debug(s"Factory for $name received LocalCreateRequest: $local from sender: $sender")
      val id: DataTypeInstanceId = local.dataTypeInstanceId
      val actor = context.actorOf(Props(createDataType(id, local.outgoingConnection)), id.id)
      sender ! NewDataTypeCreated(id, actor, null)
  }

  def createDataType(dataTypeInstanceId: DataTypeInstanceId, outgoingConnection: ActorRef): T

  def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef): S

  val name: DataTypeName
}

object AbstractClientDataTypeFactory {

  /**
    * Local means that a client created the FormicDataType itself by calling new and using FormicSystem.init().
    * Therefore no wrapper data type needs to be created.
    */
  case class LocalCreateRequest(outgoingConnection: ActorRef, dataTypeInstanceId: DataTypeInstanceId)

  case class NewDataTypeCreated(dataTypeInstanceId: DataTypeInstanceId, dataTypeActor: ActorRef, wrapper: FormicDataType)

  /**
    * To be able to pass the outgoing connection to the next actor, the CreateRequest has to be wrapped.
    */
  case class WrappedCreateRequest(outgoingConnection: ActorRef, createRequest: CreateRequest)

}


