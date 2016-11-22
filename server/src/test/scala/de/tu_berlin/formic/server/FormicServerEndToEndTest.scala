package de.tu_berlin.formic.server

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.stream.scaladsl.{Flow, Keep, Sink, SinkQueueWithCancel, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.testkit.TestKit
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.message.{CreateRequest, CreateResponse, FormicMessage}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import org.scalatest.{Matchers, WordSpecLike}
import upickle.default._
import de.tu_berlin.formic.common.json.FormicJsonProtocol._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

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

      val sinkAndSource = connectUser("foo")

      val incoming = sinkAndSource._1

      val outgoing = sinkAndSource._2
      val dataTypeInstanceId = DataTypeInstanceId()
      outgoing.offer(TextMessage(write(CreateRequest(ClientId(), dataTypeInstanceId, DataTypeName("linear")))))

      incoming.pull().onComplete {
        case Success(m) =>
          val text = m.get.asTextMessage.getStrictText
          read[FormicMessage](text) should equal(CreateResponse(dataTypeInstanceId))
          println("created")
        case Failure(ex) => fail(ex)
      }

      Thread.sleep(5000)

      server.stop()
    }
  }

  def connectUser(username: String)(implicit materializer: ActorMaterializer, executionContext: ExecutionContext): (SinkQueueWithCancel[Message], SourceQueueWithComplete[Message]) = {
    val sink: Sink[Message, SinkQueueWithCancel[Message]] = Sink.queue()
    val source = Source.queue[Message](10, OverflowStrategy.fail)
    val flow = Flow.fromSinkAndSourceMat(sink, source)(Keep.both)

    // upgradeResponse is a Future[WebSocketUpgradeResponse] that
    // completes or fails when the connection succeeds or fails
    val (upgradeResponse, sinkAndSource) =
    Http().singleWebSocketRequest(
      WebSocketRequest(
        Uri("ws://127.0.0.1:8080/formic"),
        List(Authorization(BasicHttpCredentials(username, "")))
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
    val result = Await.ready(connected, 3 seconds)
    result.value.get match {
      case Success(_) => sinkAndSource
      case Failure(ex) => fail(ex)
    }
  }
}
