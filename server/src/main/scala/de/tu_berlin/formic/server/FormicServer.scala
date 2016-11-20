package de.tu_berlin.formic.server

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.ExceptionHandler
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, OverflowStrategy, Supervision}
import de.tu_berlin.formic.common.datatype.DataTypeName

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import de.tu_berlin.formic.common.ClientId

/**
  * @author Ronny Bräunlich
  */
object FormicServer {

  val factories: Map[DataTypeName, ActorRef] = Map.empty

  def main(args: Array[String]): Unit = {
    val decider: Supervision.Decider = {
      case _: IllegalArgumentException => Supervision.Resume
      case _ => Supervision.Stop
    }
    implicit val system = ActorSystem("formic-server")
    implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))

    implicit def myExceptionHandler: ExceptionHandler = ExceptionHandler {
      case iae:IllegalArgumentException => complete(HttpResponse(NotFound, entity = iae.getMessage))
    }

    val serverAddress = system.settings.config.getString("formic.server.address")
    val serverPort = system.settings.config.getInt("formic.server.port")
    val binding = Await.result(Http().bindAndHandle(NetworkRoute.route((username) => newUserProxy(username)), serverAddress, serverPort), 3.seconds)

    // the rest of the sample code will go here
    println("Started server at 127.0.0.1:8080, press enter to kill server")
    StdIn.readLine()
    system.terminate()
  }

  def newUserProxy(username: String)(implicit actorSystem: ActorSystem, materializer: ActorMaterializer): Flow[Message, Message, NotUsed] = {
    // new connection - new UserProxy actor
    val UserProxy = actorSystem.actorOf(Props(new UserProxy(factories, ClientId(username))))

    val incomingMessages: Sink[Message, NotUsed] =
      Flow[Message].map {
        // transform websocket message to domain message
        case TextMessage.Strict(text) => IncomingMessage(text)
        case TextMessage.Streamed(textStream) =>
          val bar = textStream
            .limit(100)
            .completionTimeout(5 seconds)
            .runFold("")(_ + _)
          val result = Await.result(bar, 5 seconds)
          IncomingMessage(result)
        case _ => throw new IllegalArgumentException("Illegal message received")
      }.to(Sink.actorRef[IncomingMessage](UserProxy, PoisonPill))

    val outgoingMessages: Source[Message, NotUsed] =
      Source.actorRef[OutgoingMessage](10, OverflowStrategy.fail)
        .mapMaterializedValue { outActor =>
          // give the UserProxy actor a way to send messages out
          UserProxy ! Connected(outActor)
          NotUsed
        }.map(
        // transform domain message to web socket message
        (outMsg: OutgoingMessage) => TextMessage(outMsg.text))

    // then combine both to a flow
    Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
  }

}
