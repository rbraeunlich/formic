package de.tu_berlin.formic.client

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import com.typesafe.config.{Config, ConfigFactory}
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType.ReceiveCallback
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.{LocalCreateRequest, NewDataTypeCreated}
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.common.message.{CreateRequest, UpdateRequest}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.datatype.linear.LinearFormicJsonDataTypeProtocol
import de.tu_berlin.formic.datatype.linear.client._
import de.tu_berlin.formic.datatype.tree._

import scala.concurrent.duration._
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
@JSExport
class FormicSystem(config: Config = ConfigFactory.load()) extends DataTypeInitiator {

  implicit val system = ActorSystem("FormicSystem", config)

  implicit val ec = system.dispatcher

  @JSExport
  var serverAddress: String = system.settings.config.getString("formic.server.address")

  @JSExport
  var serverPort: String = system.settings.config.getString("formic.server.port")

  @JSExport
  var bufferSize: Int = system.settings.config.getInt("formic.client.buffersize")

  var connection: ActorRef = _

  @JSExport
  var id: ClientId = _

  var factories: Map[DataTypeName, ActorRef] = Map.empty

  @JSExport
  def init(callback: NewInstanceCallback, username: ClientId = ClientId()) = {
    initFactories()
    val instantiator = system.actorOf(Props(new DataTypeInstantiator(factories)))
    val wrappedCallback = system.actorOf(Props(new NewInstanceCallbackActorWrapper(callback)))
    val url = s"ws://${username.id}@$serverAddress:$serverPort/formic"
    connection = system.actorOf(Props(new WebSocketConnection(wrappedCallback, instantiator, username, WebSocketFactory, url)))
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
    initLinearFactories()
    initTreeFactories()
  }

  def initLinearFactories()(implicit actorSystem: ActorSystem): Unit = {
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

  def initTreeFactories()(implicit actorSystem: ActorSystem): Unit = {
    val booleanTreeFactory = actorSystem.actorOf(Props(new FormicBooleanTreeFactory), FormicBooleanTreeFactory.name.name)
    val doubleTreeFactory = actorSystem.actorOf(Props(new FormicDoubleTreeFactory), FormicDoubleTreeFactory.name.name)
    val integerTreeFactory = actorSystem.actorOf(Props(new FormicIntegerTreeFactory), FormicIntegerTreeFactory.name.name)
    val stringTreeFactory = actorSystem.actorOf(Props(new FormicStringTreeFactory), FormicStringTreeFactory.name.name)

    FormicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[Boolean](FormicBooleanTreeFactory.name))
    FormicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[Double](FormicDoubleTreeFactory.name))
    FormicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[Int](FormicIntegerTreeFactory.name))
    FormicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[String](FormicStringTreeFactory.name))

    factories += (FormicBooleanTreeFactory.name -> booleanTreeFactory)
    factories += (FormicDoubleTreeFactory.name -> doubleTreeFactory)
    factories += (FormicIntegerTreeFactory.name -> integerTreeFactory)
    factories += (FormicStringTreeFactory.name -> stringTreeFactory)
  }
}
