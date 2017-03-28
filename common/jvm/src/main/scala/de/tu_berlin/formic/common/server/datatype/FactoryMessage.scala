package de.tu_berlin.formic.common.server.datatype

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataStructureInstanceId$

/**
  * @author Ronny Bräunlich
  */
sealed trait FactoryMessage

case class NewDataTypeCreated(dataTypeInstanceId: DataStructureInstanceId, ref: ActorRef) extends  FactoryMessage
