package de.tu_berlin.formic.server.datatype

import akka.actor.{Actor, Props}
import de.tu_berlin.formic.common.datatype.{AbstractDataType, DataTypeName}
import de.tu_berlin.formic.common.message.{CreateRequest, CreateResponse}

import scala.reflect.ClassTag

/**
  * @author Ronny Bräunlich
  */
//Why the ClassTag? See http://stackoverflow.com/questions/18692265/no-classtag-available-for-t-not-for-array
abstract class AbstractDataTypeFactory[T <: AbstractDataType : ClassTag] extends Actor {

  override def receive: Receive = {
    case req: CreateRequest =>
      val newDataType = context.actorOf(Props(create()))
      sender ! NewDataTypeCreated(req.dataTypeInstanceId, newDataType)
  }

  def create(): T

  val name: DataTypeName
}
