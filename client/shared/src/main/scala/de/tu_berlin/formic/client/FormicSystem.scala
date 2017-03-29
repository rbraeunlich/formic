package de.tu_berlin.formic.client

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import com.typesafe.config.Config
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataStructure.ReceiveCallback
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataStructureFactory.{LocalCreateRequest, NewDataTypeCreated}
import de.tu_berlin.formic.common.datatype.client.DataStructureInitiator
import de.tu_berlin.formic.common.datatype.{DataStructureName, FormicDataStructure}
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.common.message.{CreateRequest, UpdateRequest}
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId}

import scala.concurrent.duration._
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
@JSExport
class FormicSystem(config: Config, val webSocketFactory: WebSocketFactory) extends DataStructureInitiator {

  this: ClientDataTypes =>

  def this(webSocketFactory: WebSocketFactory) = this(null, webSocketFactory)

  implicit val system = ActorSystem("FormicSystem", config)

  implicit val ec = system.dispatcher

  val jsonProtocol = FormicJsonProtocol()

  val serverAddressKey: String = "formic.server.address"
  @JSExport
  var serverAddress: String = if (system.settings.config.hasPath(serverAddressKey)) system.settings.config.getString(serverAddressKey) else null

  val serverPortKey: String = "formic.server.port"
  @JSExport
  var serverPort: String = if (system.settings.config.hasPath(serverPortKey)) system.settings.config.getString(serverPortKey) else null

  val clientBuffersizeKey: String = "formic.client.buffersize"
  @JSExport
  var bufferSize: Int = if (system.settings.config.hasPath(clientBuffersizeKey)) system.settings.config.getInt(clientBuffersizeKey) else 0

  var connection: ActorRef = _

  @JSExport
  var id: ClientId = _

  var factories: Map[DataStructureName, ActorRef] = Map.empty

  @JSExport
  def init(callback: NewInstanceCallback, username: ClientId = ClientId()) = {
    id = username
    initDataTypes()
    val instantiator = system.actorOf(Props(new DataStructureInstantiator(factories, id)), "Instantiator")
    val wrappedCallback = system.actorOf(Props(new NewInstanceCallbackActorWrapper(callback)), "CallbackActor")
    val url = s"ws://${username.id}@$serverAddress:$serverPort/formic"
    connection = system.actorOf(Props(new WebSocketConnection(wrappedCallback, instantiator, username, webSocketFactory, url, bufferSize, jsonProtocol)), "WebSocketConnection")
  }

  @JSExport
  def requestDataType(dataStructureInstanceId: DataStructureInstanceId) = {
    connection ! UpdateRequest(id, dataStructureInstanceId)
  }

  override def initDataStructure(dataType: FormicDataStructure): Unit = {
    val name = dataType.dataStructureName
    factories.find(t => t._1 == name) match {
      case Some((k, v)) =>
        val request = CreateRequest(id, dataType.dataStructureInstanceId, name)
        ask(v, LocalCreateRequest(connection, dataType.dataStructureInstanceId))(2.seconds)
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

  def initDataTypes(): Unit = {
    dataTypeProvider.foreach { provider =>
      factories ++= provider.initFactories(system)
      provider.registerFormicJsonDataStructureProtocols(jsonProtocol)
    }
  }
}
