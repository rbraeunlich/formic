package de.tu_berlin.formic.client

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import com.typesafe.config.Config
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataType.ReceiveCallback
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.{LocalCreateRequest, NewDataTypeCreated}
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.common.message.{CreateRequest, UpdateRequest}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.datatype.json.client.FormicJsonObjectFactory
import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataTypeProtocol
import de.tu_berlin.formic.datatype.linear.LinearFormicJsonDataTypeProtocol
import de.tu_berlin.formic.datatype.linear.client._
import de.tu_berlin.formic.datatype.tree._
import de.tu_berlin.formic.datatype.tree.client.{FormicBooleanTreeFactory, FormicDoubleTreeFactory, FormicIntegerTreeFactory, FormicStringTreeFactory}

import scala.concurrent.duration._
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
@JSExport
class FormicSystem(config: Config, val webSocketFactory: WebSocketFactory) extends DataTypeInitiator {

  def this(webSocketFactory: WebSocketFactory) = this(null, webSocketFactory)

  implicit val system = ActorSystem("FormicSystem", config)

  implicit val ec = system.dispatcher

  val serverAddressKey: String = "formic.server.address"
  @JSExport
  var serverAddress: String = if(system.settings.config.hasPath(serverAddressKey)) system.settings.config.getString(serverAddressKey) else null

  val serverPortKey: String = "formic.server.port"
  @JSExport
  var serverPort: String = if(system.settings.config.hasPath(serverPortKey)) system.settings.config.getString(serverPortKey) else null

  val clientBuffersizeKey: String = "formic.client.buffersize"
  @JSExport
  var bufferSize: Int = if(system.settings.config.hasPath(clientBuffersizeKey)) system.settings.config.getInt(clientBuffersizeKey) else 0

  var connection: ActorRef = _

  @JSExport
  var id: ClientId = _

  var factories: Map[DataTypeName, ActorRef] = Map.empty

  @JSExport
  def init(callback: NewInstanceCallback, username: ClientId = ClientId()) = {
    id = username
    initFactories()
    val instantiator = system.actorOf(Props(new DataTypeInstantiator(factories, id)), "Instantiator")
    val wrappedCallback = system.actorOf(Props(new NewInstanceCallbackActorWrapper(callback)), "CallbackActor")
    val url = s"ws://${username.id}@$serverAddress:$serverPort/formic"
    connection = system.actorOf(Props(new WebSocketConnection(wrappedCallback, instantiator, username, webSocketFactory, url, bufferSize)), "WebSocketConnection")
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
              dataType.clientId = id
              actor ! ReceiveCallback(dataType.callback)
              connection ! (actor, request)
            case Failure(ex) => throw ex
          }
      case None => throw new IllegalArgumentException("Unknown data type: " + name)
    }
  }

  def initFactories(): Unit = {
    initLinearFactories()
    initTreeFactories()
    initJsonFactory()
  }

  def initLinearFactories(): Unit = {
    val formicBooleanListFactory = system.actorOf(Props(new FormicBooleanListDataTypeFactory), FormicBooleanListDataTypeFactory.dataTypeName.name)
    val formicDoubleListFactroy = system.actorOf(Props(new FormicDoubleListDataTypeFactory), FormicDoubleListDataTypeFactory.dataTypeName.name)
    val formicIntegerListFactory = system.actorOf(Props(new FormicIntegerListDataTypeFactory), FormicIntegerListDataTypeFactory.dataTypeName.name)
    val formicStringFactory = system.actorOf(Props(new FormicStringDataTypeFactory), FormicStringDataTypeFactory.dataTypeName.name)

    FormicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Boolean](FormicBooleanListDataTypeFactory.dataTypeName))
    FormicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Double](FormicDoubleListDataTypeFactory.dataTypeName))
    FormicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Int](FormicIntegerListDataTypeFactory.dataTypeName))
    FormicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Char](FormicStringDataTypeFactory.dataTypeName))

    factories += (FormicBooleanListDataTypeFactory.dataTypeName -> formicBooleanListFactory)
    factories += (FormicDoubleListDataTypeFactory.dataTypeName -> formicDoubleListFactroy)
    factories += (FormicIntegerListDataTypeFactory.dataTypeName -> formicIntegerListFactory)
    factories += (FormicStringDataTypeFactory.dataTypeName -> formicStringFactory)
  }

  def initTreeFactories(): Unit = {
    val booleanTreeFactory = system.actorOf(Props(new FormicBooleanTreeFactory), FormicBooleanTreeFactory.name.name)
    val doubleTreeFactory = system.actorOf(Props(new FormicDoubleTreeFactory), FormicDoubleTreeFactory.name.name)
    val integerTreeFactory = system.actorOf(Props(new FormicIntegerTreeFactory), FormicIntegerTreeFactory.name.name)
    val stringTreeFactory = system.actorOf(Props(new FormicStringTreeFactory), FormicStringTreeFactory.name.name)

    FormicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[Boolean](FormicBooleanTreeFactory.name))
    FormicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[Double](FormicDoubleTreeFactory.name))
    FormicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[Int](FormicIntegerTreeFactory.name))
    FormicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[String](FormicStringTreeFactory.name))

    factories += (FormicBooleanTreeFactory.name -> booleanTreeFactory)
    factories += (FormicDoubleTreeFactory.name -> doubleTreeFactory)
    factories += (FormicIntegerTreeFactory.name -> integerTreeFactory)
    factories += (FormicStringTreeFactory.name -> stringTreeFactory)
  }

  def initJsonFactory() = {
    val factory = system.actorOf(Props(new FormicJsonObjectFactory), FormicJsonObjectFactory.name.name)
    FormicJsonProtocol.registerProtocol(new JsonFormicJsonDataTypeProtocol(FormicJsonObjectFactory.name)(JsonFormicJsonDataTypeProtocol.reader, JsonFormicJsonDataTypeProtocol.writer))
    factories += (FormicJsonObjectFactory.name -> factory)
  }
}
