package de.tu_berlin.formic.server.datatype

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataTypeInstanceId

/**
  * @author Ronny Bräunlich
  */
sealed trait FactoryMessage

case class NewDataTypeCreated(dataTypeInstanceId: DataTypeInstanceId, ref: ActorRef) extends  FactoryMessage
