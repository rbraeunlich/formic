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
import de.tu_berlin.formic.common.datatype.{DataTypeName, OperationContext}
import de.tu_berlin.formic.common.json.FormicJsonProtocol._
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.datatype.linear.{LinearDataType, LinearInsertOperation}
import org.scalatest.{Matchers, WordSpecLike}
import upickle.default._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}
import scala.languageFeature.postfixOps

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

      val user1Id = ClientId("foo")
      val user2Id = ClientId("bar")
      val (user1Incoming, user1Outgoing) = connectUser(user1Id.id)
      val (user2Incoming, user2Outgoing) = connectUser(user2Id.id)


      val dataTypeInstanceId: DataTypeInstanceId = createLinearDataTypeInstance(user1Id, user2Id, user1Incoming, user1Outgoing, user2Incoming, user2Outgoing)


      val op1 = LinearInsertOperation(0, "a", OperationId(), OperationContext(List.empty), user2Id)
      user2Outgoing.offer(TextMessage(write(OperationMessage(user2Id, dataTypeInstanceId, LinearDataType.dataTypeName, List(op1)))))


      Thread.sleep(5000)

      server.stop()
    }
  }

  def createLinearDataTypeInstance(user1Id: ClientId, user2Id: ClientId, user1Incoming: SinkQueueWithCancel[Message], user1Outgoing: SourceQueueWithComplete[Message], user2Incoming: SinkQueueWithCancel[Message], user2Outgoing: SourceQueueWithComplete[Message])(implicit executionContext: ExecutionContext): DataTypeInstanceId = {
    val dataTypeInstanceId = DataTypeInstanceId()
    user1Outgoing.offer(TextMessage(write(CreateRequest(user1Id, dataTypeInstanceId, DataTypeName("linear")))))

    val incomingCreateResponse = user1Incoming.pull()
    incomingCreateResponse.onComplete {
      case Success(m) =>
        val text = m.get.asTextMessage.getStrictText
        read[FormicMessage](text) should equal(CreateResponse(dataTypeInstanceId))
        println("created")
      case Failure(ex) => fail(ex)
    }
    Await.result(incomingCreateResponse, 3 seconds)

    user2Outgoing.offer(TextMessage(write(UpdateRequest(user2Id, dataTypeInstanceId))))

    val incomingUpdateResponse = user2Incoming.pull()
    incomingUpdateResponse.onComplete {
      case Success(m) =>
        val text = m.get.asTextMessage.getStrictText
        read[FormicMessage](text) should equal(UpdateResponse(dataTypeInstanceId, LinearDataType.dataTypeName, "[]"))
      case Failure(ex) => fail(ex)
    }

    dataTypeInstanceId
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
