package de.tu_berlin.formic.common.server.datastructure

import akka.actor.ActorRef
import de.tu_berlin.formic.common.DataStructureInstanceId

/**
  * @author Ronny Bräunlich
  */
sealed trait FactoryMessage

case class NewDataStructureCreated(dataStructureInstanceId: DataStructureInstanceId, ref: ActorRef) extends  FactoryMessage
