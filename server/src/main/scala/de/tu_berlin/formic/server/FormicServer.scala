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
import de.tu_berlin.formic.common.json.FormicJsonProtocol._
import de.tu_berlin.formic.common.message.FormicMessage
import de.tu_berlin.formic.datatype.linear.LinearFormicJsonDataTypeProtocol
import de.tu_berlin.formic.datatype.linear.server._
import upickle.default._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Ronny Bräunlich
  */
object FormicServer {

  var factories: Map[DataTypeName, ActorRef] = Map.empty

  implicit val system = ActorSystem("formic-server")

  def initFactories()(implicit actorSystem: ActorSystem) = {
    val booleanListFactory = actorSystem.actorOf(Props[BooleanListDataTypeFactory], BooleanListDataTypeFactory.name.name)
    val doubleListFactory = actorSystem.actorOf(Props[DoubleListDataTypeFactory], DoubleListDataTypeFactory.name.name)
    val integerListFactory = actorSystem.actorOf(Props[IntegerListDataTypeFactory], IntegerListDataTypeFactory.name.name)
    val stringFactory = actorSystem.actorOf(Props[StringDataTypeFactory], StringDataTypeFactory.name.name)

    FormicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Boolean](BooleanListDataTypeFactory.name))
    FormicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Double](DoubleListDataTypeFactory.name))
    FormicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Int](IntegerListDataTypeFactory.name))
    FormicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Char](StringDataTypeFactory.name))

    factories += (BooleanListDataTypeFactory.name -> booleanListFactory)
    factories += (DoubleListDataTypeFactory.name -> doubleListFactory)
    factories += (IntegerListDataTypeFactory.name -> integerListFactory)
    factories += (StringDataTypeFactory.name -> stringFactory)
  }

  def start(route: server.Route): Unit = {
    val decider: Supervision.Decider = {
      case _: IllegalArgumentException => Supervision.Resume
      case _ => Supervision.Stop
    }
    implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))
    implicit val ec = system.dispatcher

    val myExceptionHandler: ExceptionHandler = ExceptionHandler {
      case iae: IllegalArgumentException => complete(HttpResponse(NotFound, entity = iae.getMessage))
    }
    initFactories()
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
  }

  def newUserProxy(username: String)(implicit actorSystem: ActorSystem, materializer: ActorMaterializer): Flow[Message, Message, NotUsed] = {
    // new connection - new UserProxy actor
    val UserProxy = actorSystem.actorOf(Props(new UserProxy(factories, ClientId(username))), username)

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
      }.map(text => read[FormicMessage](text)).to(Sink.actorRef[FormicMessage](UserProxy, PoisonPill))

    val outgoingMessages: Source[Message, NotUsed] =
      Source.actorRef[FormicMessage](10, OverflowStrategy.fail)
        .mapMaterializedValue { outActor =>
          // give the UserProxy actor a way to send messages out
          UserProxy ! Connected(outActor)
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
