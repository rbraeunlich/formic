package de.tu_berlin.formic.server

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.testkit.TestKit
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.message.CreateRequest
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import org.scalatest.{Matchers, WordSpecLike}
import upickle.default._

import scala.concurrent.Future

/**
  * @author Ronny Bräunlich
  */
class FormicServerEndToEndTest extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike with Matchers {

  "Formic server" must {
    "allow two users to work on a linear structure together" in {
      val server = new Thread {
        override def run() {
          FormicServer.main(Array.empty)
        }
      }
      server.start()

      implicit val materializer = ActorMaterializer()
      import system.dispatcher

      // print each incoming strict text message
      val printSink: Sink[Message, Future[Done]] =
      Sink.foreach {
        case message: TextMessage.Strict =>
          println(message.text)
      }

      val source = Source.queue[Message](10, OverflowStrategy.fail)


      // the Future[Done] is the materialized value of Sink.foreach
      // and it is completed when the stream completes
      val flow = Flow.fromSinkAndSourceMat(printSink, source)(Keep.both)

      // upgradeResponse is a Future[WebSocketUpgradeResponse] that
      // completes or fails when the connection succeeds or fails
      // and closed is a Future[Done] representing the stream completion from above
      val (upgradeResponse, closed) =
      Http().singleWebSocketRequest(WebSocketRequest("ws://test@127.0.0.1:8080/formic"), flow)

      val connected = upgradeResponse.map { upgrade =>
        // just like a regular http request we can access response status which is available via upgrade.response.status
        // status code 101 (Switching Protocols) indicates that server support WebSockets
        if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
          Done
        } else {
          throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
        }
      }

      // in a real application you would not side effect here
      // and handle errors more carefully
      connected.onComplete(println)
      closed._1.foreach(_ => println("closed"))

      //TODO got to register the factory on the server
      val queue = closed._2
      queue.offer(TextMessage(write(CreateRequest(ClientId(), DataTypeInstanceId(), DataTypeName("linear")))))
      Thread.sleep(5000)

      server.stop()
    }
  }
}
