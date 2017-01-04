package de.tu_berlin.formic.common.server.datatype

import akka.actor.{Actor, ActorLogging, Props}
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.message.CreateRequest

import scala.reflect.ClassTag

/**
  * @author Ronny Bräunlich
  */
//Why the ClassTag? See http://stackoverflow.com/questions/18692265/no-classtag-available-for-t-not-for-array
abstract class AbstractDataTypeFactory[T <: AbstractServerDataType : ClassTag] extends Actor with ActorLogging{

  override def receive: Receive = {
    case req:CreateRequest =>
      log.debug(s"Factory for $name received CreateRequest: $req")
      val newDataType = context.actorOf(Props(create(req.dataTypeInstanceId)), req.dataTypeInstanceId.id)
      sender ! NewDataTypeCreated(req.dataTypeInstanceId, newDataType)
  }

  def create(dataTypeInstanceId: DataTypeInstanceId): T

  val name: DataTypeName
}
