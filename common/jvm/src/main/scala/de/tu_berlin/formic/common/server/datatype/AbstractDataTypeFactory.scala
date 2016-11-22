package de.tu_berlin.formic.common.server.datatype

import akka.actor.{Actor, Props}
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.{AbstractDataType, DataTypeName}
import de.tu_berlin.formic.common.message.CreateRequest

import scala.reflect.ClassTag

/**
  * @author Ronny Bräunlich
  */
//Why the ClassTag? See http://stackoverflow.com/questions/18692265/no-classtag-available-for-t-not-for-array
abstract class AbstractDataTypeFactory[T <: AbstractDataType : ClassTag] extends Actor {

  override def receive: Receive = {
    case CreateRequest(_, dataTypeInstanceId, _) =>
      val newDataType = context.actorOf(Props(create(dataTypeInstanceId)), dataTypeInstanceId.id)
      sender ! NewDataTypeCreated(dataTypeInstanceId, newDataType)
  }

  def create(dataTypeInstanceId: DataTypeInstanceId): T

  val name: DataTypeName
}
