package de.tu_berlin.formic.client

import akka.actor.{Actor, ActorRef}
import de.tu_berlin.formic.client.Dispatcher.WrappedUpdateResponse
import de.tu_berlin.formic.common.ClientId
import de.tu_berlin.formic.common.datatype.DataStructureName
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.WrappedCreateRequest
import de.tu_berlin.formic.common.message.CreateRequest

/**
  * @author Ronny Bräunlich
  */
class DataTypeInstantiator(val factories: Map[DataStructureName, ActorRef], val localClientId: ClientId) extends Actor {

  def receive = {
    case WrappedUpdateResponse(outgoing, rep) =>
      factories.find(t => t._1 == rep.dataType) match {
        case Some((k,v)) =>
          //that ways the factory directly answers to the dispatcher
          v forward WrappedCreateRequest(outgoing, rep.data, rep.lastOperationId, CreateRequest(null, rep.dataTypeInstanceId, k), localClientId)
        case None => throw new IllegalArgumentException(s"Unknown data type name: ${rep.dataType}")
      }
  }
}
