package de.tu_berlin.formic.server

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, WebSocketRequest}
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.{Http, server}
import akka.stream.scaladsl.{Flow, Keep, Sink, SinkQueueWithCancel, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.testkit.TestKit
import akka.util.ByteString
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.message.{CreateRequest, CreateResponse, FormicMessage}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.datatype.linear.server.StringDataTypeFactory
import org.scalatest.{path => _, _}
import de.tu_berlin.formic.common.json.FormicJsonProtocol._
import upickle.default._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
class ExceptionHandlingIntegrationTest extends TestKit(ActorSystem("ExceptionHandlingIntegrationTest"))
  with WordSpecLike
  with Matchers
  with OneInstancePerTest
  with BeforeAndAfterAll
  with BeforeAndAfter {

  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  var serverThread: ServerThread = _

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }


  before {
    val server = new FormicServer()
    val testRoute = path("formic") {
      extractCredentials {
        creds =>
          get {
            handleWebSocketMessages(server.newUserProxy(creds.get.asInstanceOf[BasicHttpCredentials].username)
            (server.system, server.materializer))
          }
      }
    }
    serverThread = new ServerThread(server, testRoute)
    serverThread.setDaemon(true)
    serverThread.start()
    Thread.sleep(3000)

  }

  after {
    serverThread.terminate()
    Thread.sleep(3000)
  }

  "FormicServer" must {
    "reject binary WebSocket messages but resume" in {
      val userId = ClientId("foo")
      val (userIncoming, userOutgoing) = connectUser(userId.id)

      //this should cause a problem
      userOutgoing.offer(BinaryMessage(ByteString("Test Message")))

      //if the stream did not crash, this should work
      val dataTypeInstanceId = DataTypeInstanceId()
      userOutgoing.offer(TextMessage(write(CreateRequest(userId, dataTypeInstanceId, StringDataTypeFactory.name))))

      val incomingCreateResponse = userIncoming.pull()
      Await.ready(incomingCreateResponse, 3.seconds)
      incomingCreateResponse.value.get match {
        case Success(m) =>
          val text = m.get.asTextMessage.getStrictText
          read[FormicMessage](text) should equal(CreateResponse(dataTypeInstanceId))
        case Failure(ex) => fail(ex)
      }
    }
  }

  def connectUser(username: String)(implicit materializer: ActorMaterializer, executionContext: ExecutionContext): (SinkQueueWithCancel[Message], SourceQueueWithComplete[Message]) = {
    val sink: Sink[Message, SinkQueueWithCancel[Message]] = Sink.queue()
    val source = Source.queue[Message](10, OverflowStrategy.fail)
    val flow = Flow.fromSinkAndSourceMat(sink, source)(Keep.both)

    // upgradeResponse is a Future[WebSocketUpgradeResponse] that
    // completes or fails when the connection succeeds or fails
    val serverAddress = system.settings.config.getString("formic.server.address")
    val serverPort = system.settings.config.getInt("formic.server.port")
    val (upgradeResponse, sinkAndSource) =
      Http().singleWebSocketRequest(
        WebSocketRequest(
          Uri(s"ws://$serverAddress:$serverPort/formic"),
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

    val result = Await.ready(connected, 6.seconds)

    result.value.get match {
      case Success(_) => sinkAndSource
      case Failure(ex) => throw ex
    }
  }
}