package de.tu_berlin.formic.client

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.{Done, NotUsed}
import de.tu_berlin.formic.client.WebSocketConnection.{OnClose, OnConnect, OnMessage}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}
/**
  * @author Ronny Bräunlich
  */
class WrappedAkkaStreamWebSocket(val url: String, val receiver: ActorRef)(implicit val materializer: ActorMaterializer, val actorSystem: ActorSystem) extends WebSocketWrapper{

  var outgoing: SourceQueueWithComplete[Message] = _

  establishConnection()

  def establishConnection()(implicit materializer: ActorMaterializer, actorSystem: ActorSystem) = {
    implicit val ec = actorSystem.dispatcher
    val source = Source.queue[Message](10, OverflowStrategy.fail)
    val sink: Sink[Message, NotUsed] =
      Flow[Message].map {
        // transform websocket message to domain message
        case TextMessage.Strict(text) => text
        case TextMessage.Streamed(textStream) =>
          val bar = textStream
            .limit(100)
            .completionTimeout(5.seconds)
            .runFold("")(_ + _)
          val result = Await.result(bar, 5.seconds)
          result
        case _ => throw new IllegalArgumentException("Illegal message received")
      }.map(text => OnMessage(text)).to(Sink.actorRef[OnMessage](receiver, OnClose(1)))

    val flow = Flow.fromSinkAndSourceMat(sink, source)(Keep.both)

    val uri: Uri = Uri(url)
    // upgradeResponse is a Future[WebSocketUpgradeResponse] that
    // completes or fails when the connection succeeds or fails
    val (upgradeResponse, sinkAndSource) =
      Http().singleWebSocketRequest(
        WebSocketRequest(
          uri,
          List(Authorization(BasicHttpCredentials(uri.authority.userinfo, "")))
        ),
        flow
      )
    val connected = upgradeResponse.map { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Done
      } else {
        throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
      }
    }

    val result = Await.ready(connected, 6.seconds)

    result.value.get match {
      case Success(_) =>
        outgoing = sinkAndSource._2
        receiver ! OnConnect(this)
      case Failure(ex) => throw ex
    }
  }

  override def send(message: String): Unit = {
    outgoing.offer(TextMessage(message))
  }
}
