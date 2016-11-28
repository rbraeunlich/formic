package de.tu_berlin.formic.client

import akka.actor.{Actor, ActorRef}
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.message.{CreateRequest, UpdateResponse}

/**
  * @author Ronny Bräunlich
  */
class DataTypeInstantiator(val factories: Map[DataTypeName, ActorRef]) extends Actor {

  def receive = {
    case rep: UpdateResponse =>
      factories.find(t => t._1 == rep.dataType) match {
        case Some((k,v)) =>
          //that ways the factory directly answers to the dispatcher
          v forward CreateRequest(null, rep.dataTypeInstanceId, k)
        case None => throw new IllegalArgumentException(s"Unknown data type name: ${rep.dataType}")
      }
  }
}
