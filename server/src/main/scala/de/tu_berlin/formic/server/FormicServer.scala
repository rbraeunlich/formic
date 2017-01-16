package de.tu_berlin.formic.server

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.event.Logging
import akka.http.scaladsl.{Http, server}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, OverflowStrategy, Supervision}
import de.tu_berlin.formic.common.ClientId
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.common.message.FormicMessage
import upickle.default._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Ronny Bräunlich
  */
class FormicServer {

  this: ServerDataTypes =>

  var factories: Map[DataTypeName, ActorRef] = Map.empty

  val decider: Supervision.Decider = {
    case iae: IllegalArgumentException =>
      system.log.error(iae, "Exception in stream")
      Supervision.Resume
    case _ => Supervision.Stop
  }

  implicit val system = ActorSystem("formic-server")

  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  implicit val ec = system.dispatcher

  val jsonProtocol = FormicJsonProtocol()
  implicit val writer = jsonProtocol.writer
  implicit val reader = jsonProtocol.reader

  def initDataTypes() = {
    dataTypeProvider.foreach{ provider =>
      factories ++= provider.initFactories(system)
      provider.registerFormicJsonDataTypeProtocols(jsonProtocol)
    }
  }

  def start(route: server.Route): Http.ServerBinding = {
    val myExceptionHandler: ExceptionHandler = ExceptionHandler {
      case iae: IllegalArgumentException => complete(HttpResponse(NotFound, entity = iae.getMessage))
    }
    initDataTypes()
    val serverAddress = system.settings.config.getString("formic.server.address")
    val serverPort = system.settings.config.getInt("formic.server.port")
    val binding = Http().bindAndHandle(
      handleExceptions(myExceptionHandler) {route},
      serverAddress,
      serverPort)

    // the rest of the sample code will go here
    val log = Logging(system.eventStream, "formic-server")
    binding.map { serverBinding =>
      log.info(s"FormicServer bound to ${serverBinding.localAddress} ")
    }.onFailure {
      case ex: Exception =>
        log.error(ex, "Failed to bind to {}:{}!", serverAddress, serverPort)
        system.terminate()
    }
    Await.result(binding, 5.seconds)
  }

  def newUserProxy(username: String)(implicit actorSystem: ActorSystem, materializer: ActorMaterializer): Flow[Message, Message, NotUsed] = {
    // new connection - new UserProxy actor
    val userProxy = actorSystem.actorOf(Props(new UserProxy(factories, ClientId(username))), username)

    val incomingMessages: Sink[Message, NotUsed] =
      Flow[Message].map {
        // transform websocket message to domain message
        case TextMessage.Strict(text) => text
        case TextMessage.Streamed(textStream) =>
          val bar = textStream
            .limit(100)
            .completionTimeout(5 seconds)
            .runFold("")(_ + _)
          val result = Await.result(bar, 5 seconds)
          result
        case _ => throw new IllegalArgumentException("Illegal message received")
      }.map(text => read[FormicMessage](text)).to(Sink.actorRef[FormicMessage](userProxy, PoisonPill))

    val outgoingMessages: Source[Message, NotUsed] =
      Source.actorRef[FormicMessage](10, OverflowStrategy.fail)
        .mapMaterializedValue { outActor =>
          // give the UserProxy actor a way to send messages out
          userProxy ! Connected(outActor)
          NotUsed
        }.map(
        // transform domain message to web socket message
        (outMsg: FormicMessage) => TextMessage(write(outMsg)))

    // then combine both to a flow
    Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
  }

  def terminate(): Unit = {
    system.terminate()
  }
}
