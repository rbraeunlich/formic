package de.tu_berlin.formic.client

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import de.tu_berlin.formic.client.datatype.AbstractClientDataTypeFactory.NewDataTypeCreated
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
import de.tu_berlin.formic.common.message.{CreateRequest, UpdateRequest}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}

import scala.concurrent.duration._
import scala.scalajs.js.annotation.JSExport

/**
  * @author Ronny Bräunlich
  */
@JSExport
class FormicSystem {

  //TODO Read from Properties as default
  @JSExport
  var serverAddress: String = _

  @JSExport
  var serverPort: String = _

  @JSExport
  var bufferSize: Int = _

  var connection: ActorRef = _

  @JSExport
  var id: ClientId = _

  var factories: Map[DataTypeName, ActorRef] = Map.empty

  @JSExport
  def init(callback: NewInstanceCallback, username: ClientId = ClientId()) = {
    implicit val system = ActorSystem("FormicSystem")
    val instantiator = system.actorOf(Props(new DataTypeInstantiator(factories)))
    val wrappedCallback = system.actorOf(Props(new NewInstanceCallbackActorWrapper(callback)))
    connection = system.actorOf(Props(new WebSocketConnection(wrappedCallback, instantiator, id)))
  }

  @JSExport
  def requestDataType(dataTypeInstanceId: DataTypeInstanceId) = {
    connection ! UpdateRequest(id, dataTypeInstanceId)
  }

  def initDataType(dataType: FormicDataType): Unit = {
    val name = dataType.dataTypeName
    factories.find(t => t._1 == name) match {
      case Some((k, v)) =>
        import scala.concurrent.ExecutionContext.Implicits.global
        val response = ask(v, CreateRequest(id, DataTypeInstanceId(), name))(2.seconds)
          .mapTo[NewDataTypeCreated]
          .map(msg => msg.dataTypeActor)
          .onSuccess {
            case actor: ActorRef =>
              dataType.actor = actor
              dataType.connection = connection
          }
      case None => throw new IllegalArgumentException("Unknown data type: " + name)
    }
  }

  def initFactories()(implicit actorSystem: ActorSystem): Unit = {
  }
}
