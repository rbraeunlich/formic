package de.tu_berlin.formic.common.datatype.client

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.{NewDataTypeCreated, WrappedCreateRequest}
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
import de.tu_berlin.formic.common.message.CreateRequest

import scala.reflect.ClassTag

/**
  * @author Ronny Bräunlich
  */
//Why the ClassTag? See http://stackoverflow.com/questions/18692265/no-classtag-available-for-t-not-for-array
abstract class AbstractClientDataTypeFactory[T <: AbstractClientDataType: ClassTag, S <: FormicDataType : ClassTag] extends Actor with ActorLogging {

  override def receive: Receive = {
    case WrappedCreateRequest(outgoingConnection, req) =>
      log.debug(s"Factory for $name received CreateRequest: $req")
      val actor = context.actorOf(Props(createDataType(req.dataTypeInstanceId, outgoingConnection)), req.dataTypeInstanceId.id)
      val wrapper = createWrapperType(req.dataTypeInstanceId, actor)
      sender ! NewDataTypeCreated(req.dataTypeInstanceId, actor, wrapper)
  }

  def createDataType(dataTypeInstanceId: DataTypeInstanceId, outgoingConnection: ActorRef): T

  def createWrapperType(dataTypeInstanceId: DataTypeInstanceId, dataType: ActorRef): S

  val name: DataTypeName
}

object AbstractClientDataTypeFactory {

  case class NewDataTypeCreated(dataTypeInstanceId: DataTypeInstanceId, dataTypeActor: ActorRef, wrapper: FormicDataType)

  case class WrappedCreateRequest(outgoingConnection: ActorRef, createRequest: CreateRequest)
}


