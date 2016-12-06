package de.tu_berlin.formic.client

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType.ReceiveCallback
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.{LocalCreateRequest, NewDataTypeCreated, WrappedCreateRequest}
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.common.message.{CreateRequest, UpdateRequest}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.datatype.linear.LinearFormicJsonDataTypeProtocol
import de.tu_berlin.formic.datatype.linear.client._

import scala.concurrent.duration._
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
@JSExport
class FormicSystem extends DataTypeInitiator {

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

  implicit val system = ActorSystem("FormicSystem")

  implicit val ec = system.dispatcher

  @JSExport
  def init(callback: NewInstanceCallback, username: ClientId = ClientId()) = {
    initFactories()
    val instantiator = system.actorOf(Props(new DataTypeInstantiator(factories)))
    val wrappedCallback = system.actorOf(Props(new NewInstanceCallbackActorWrapper(callback)))
    connection = system.actorOf(Props(new WebSocketConnection(wrappedCallback, instantiator, username, WebSocketFactory)))
  }

  @JSExport
  def requestDataType(dataTypeInstanceId: DataTypeInstanceId) = {
    connection ! UpdateRequest(id, dataTypeInstanceId)
  }

  override def initDataType(dataType: FormicDataType): Unit = {
    val name = dataType.dataTypeName
    factories.find(t => t._1 == name) match {
      case Some((k, v)) =>
        val request = CreateRequest(id, dataType.dataTypeInstanceId, name)
        ask(v, LocalCreateRequest(connection, dataType.dataTypeInstanceId))(2.seconds)
          .mapTo[NewDataTypeCreated]
          .map(msg => msg.dataTypeActor)
          .onComplete {
            case Success(actor) =>
              dataType.actor = actor
              actor ! ReceiveCallback(dataType.callback)
              connection ! (actor, request)
            case Failure(ex) => throw ex
          }
      case None => throw new IllegalArgumentException("Unknown data type: " + name)
    }
  }

  def initFactories()(implicit actorSystem: ActorSystem): Unit = {
    val formicBooleanListFactory = actorSystem.actorOf(Props(new FormicBooleanListDataTypeFactory), FormicBooleanListDataTypeFactory.dataTypeName.name)
    val formicDoubleListFactroy = actorSystem.actorOf(Props(new FormicDoubleListDataTypeFactory), FormicDoubleListDataTypeFactory.dataTypeName.name)
    val formicIntegerListFactory = actorSystem.actorOf(Props(new FormicIntegerListDataTypeFactory), FormicIntegerListDataTypeFactory.dataTypeName.name)
    val formicStringFactory = actorSystem.actorOf(Props(new FormicStringDataTypeFactory), FormicStringDataTypeFactory.dataTypeName.name)

    FormicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Boolean](FormicBooleanListDataTypeFactory.dataTypeName))
    FormicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Double](FormicDoubleListDataTypeFactory.dataTypeName))
    FormicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Int](FormicIntegerListDataTypeFactory.dataTypeName))
    FormicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Char](FormicStringDataTypeFactory.dataTypeName))

    factories += (FormicBooleanListDataTypeFactory.dataTypeName -> formicBooleanListFactory)
    factories += (FormicDoubleListDataTypeFactory.dataTypeName -> formicDoubleListFactroy)
    factories += (FormicIntegerListDataTypeFactory.dataTypeName -> formicIntegerListFactory)
    factories += (FormicStringDataTypeFactory.dataTypeName -> formicStringFactory)
  }
}
