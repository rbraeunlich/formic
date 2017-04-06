package de.tu_berlin.formic.common.datastructure.client

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import de.tu_berlin.formic.common.datastructure.client.AbstractClientDataStructure.RemoteInstantiation
import de.tu_berlin.formic.common.datastructure.client.AbstractClientDataStructureFactory.{LocalCreateRequest, NewDataStructureCreated, WrappedCreateRequest}
import de.tu_berlin.formic.common.datastructure.{DataStructureName, FormicDataStructure}
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId, OperationId}

import scala.reflect.ClassTag

/**
  * @author Ronny Bräunlich
  */
//Why the ClassTag? See http://stackoverflow.com/questions/18692265/no-classtag-available-for-t-not-for-array
abstract class AbstractClientDataStructureFactory[T <: AbstractClientDataStructure : ClassTag, S <: FormicDataStructure : ClassTag] extends Actor with ActorLogging {

  override def receive: Receive = {
    case WrappedCreateRequest(outgoingConnection, data, lastOperationId, req, localClientId) =>
      log.debug(s"Factory for $name received CreateRequest: $req from sender: $sender")
      val id: DataStructureInstanceId = req.dataStructureInstanceId
      val initialData = if(data == null || data.isEmpty) Option.empty else Option(data)
      val actor = context.actorOf(Props(createDataStructure(id, outgoingConnection, initialData, lastOperationId)), id.id)
      val wrapper = createWrapper(id, actor, localClientId)
      actor ! RemoteInstantiation
      sender ! NewDataStructureCreated(id, actor, wrapper)

    case local: LocalCreateRequest =>
      log.debug(s"Factory for $name received LocalCreateRequest: $local from sender: $sender")
      val id: DataStructureInstanceId = local.dataStructureInstanceId
      val actor = context.actorOf(Props(createDataStructure(id, local.outgoingConnection, Option.empty, Option.empty)), id.id)
      sender ! NewDataStructureCreated(id, actor, null)
  }

  /**
    * Creates a new data type.
    *
    * @param dataStructureInstanceId the id of the data type
    * @param outgoingConnection the connection to send messages to the server
    * @param data the initial data as JSON, might be empty
    * @param lastOperationId the operation id the data is based on, might be empty
    * @return
    */
  def createDataStructure(dataStructureInstanceId: DataStructureInstanceId, outgoingConnection: ActorRef, data: Option[String], lastOperationId: Option[OperationId]): T

  def createWrapper(dataStructureInstanceId: DataStructureInstanceId, dataType: ActorRef, localClientId: ClientId): S

  val name: DataStructureName
}

object AbstractClientDataStructureFactory {

  /**
    * Local means that a client created the FormicDataType itself by calling new and using FormicSystem.init().
    * Therefore no wrapper data type needs to be created.
    */
  case class LocalCreateRequest(outgoingConnection: ActorRef, dataStructureInstanceId: DataStructureInstanceId)

  case class NewDataStructureCreated(dataStructureInstanceId: DataStructureInstanceId, dataStructureActor: ActorRef, wrapper: FormicDataStructure)

  /**
    * To be able to pass the outgoing connection and the initial data to the factory, the CreateRequest has to be wrapped.
    */
  case class WrappedCreateRequest(outgoingConnection: ActorRef, data: String, lastOperationId: Option[OperationId], createRequest: CreateRequest, localClientId: ClientId)

}


