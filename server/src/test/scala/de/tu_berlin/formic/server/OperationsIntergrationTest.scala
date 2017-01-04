package de.tu_berlin.formic.server

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Flow, Keep, Sink, SinkQueueWithCancel, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.testkit.TestKit
import de.tu_berlin.formic.common.datatype.OperationContext
import de.tu_berlin.formic.common.json.FormicJsonProtocol._
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import de.tu_berlin.formic.datatype.linear.LinearInsertOperation
import de.tu_berlin.formic.datatype.linear.server.StringDataTypeFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, OneInstancePerTest, WordSpecLike}
import upickle.default._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
class OperationsIntergrationTest extends TestKit(ActorSystem("OperationsIntergrationTest"))
  with WordSpecLike
  with Matchers
  with OneInstancePerTest
  with BeforeAndAfterAll {

  val formicServer = new FormicServer

  val testRoute = path("formic") {
    extractCredentials {
      creds =>
        get {
          handleWebSocketMessages(formicServer.newUserProxy(creds.get.asInstanceOf[BasicHttpCredentials].username)
          (formicServer.system, formicServer.materializer))
        }
    }
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.terminate()
    system.terminate()
  }

  val server = new ServerThread(formicServer, testRoute)

  "Formic server" must {
    "allow two users to work on a linear structure together" in {
      server.setDaemon(true)
      server.start()
      Thread.sleep(6000)

      implicit val materializer = ActorMaterializer()
      import system.dispatcher

      val user1Id = ClientId("foo")
      val user2Id = ClientId("bar")
      val (user1Incoming, user1Outgoing) = connectUser(user1Id.id)
      val (user2Incoming, user2Outgoing) = connectUser(user2Id.id)


      val dataTypeInstanceId: DataTypeInstanceId = createStringDataTypeInstance(user1Id, user2Id, user1Incoming, user1Outgoing, user2Incoming, user2Outgoing)


      applyOperations(user1Id, user2Id, user1Incoming, user1Outgoing, user2Incoming, user2Outgoing, dataTypeInstanceId)

      user2Outgoing.offer(TextMessage(write(UpdateRequest(user2Id, dataTypeInstanceId))))
      val finalResponse = user2Incoming.pull()
      Await.ready(finalResponse, 3 seconds)
      finalResponse.value.get match {
        case Success(m) =>
          val text = m.get.asTextMessage.getStrictText
          val readMsg = read[FormicMessage](text)
          readMsg.asInstanceOf[UpdateResponse].dataTypeInstanceId should equal(dataTypeInstanceId)
          readMsg.asInstanceOf[UpdateResponse].dataType should equal(StringDataTypeFactory.name)
          readMsg.asInstanceOf[UpdateResponse].data should equal("[\"3\",\"2\",\"1\",\"c\",\"b\",\"a\"]")
        //the lastOperationId is unimportant here
        case Failure(ex) => fail(ex)
      }

      user1Outgoing.complete()
      user2Outgoing.complete()

      println("Shutting down OperationsIntergrationTest server")
      server.terminate()
    }
  }

  def verifyEqual(message: Future[Option[Message]], formicMessage: FormicMessage)(implicit ec: ExecutionContext) = {
    val ready = Await.ready(message, 3 seconds)
    ready.value.get match {
      case Success(m) =>
        val text = m.get.asTextMessage.getStrictText
        read[FormicMessage](text) should equal(formicMessage)
      case Failure(ex) => fail(ex)
    }
  }

  def applyOperations(user1Id: ClientId, user2Id: ClientId, user1Incoming: SinkQueueWithCancel[Message], user1Outgoing: SourceQueueWithComplete[Message], user2Incoming: SinkQueueWithCancel[Message], user2Outgoing: SourceQueueWithComplete[Message], dataTypeInstanceId: DataTypeInstanceId)(implicit ec: ExecutionContext) = {
    //let both users send operations in parallel
    //because the id of u1 is greater than u2 (f > b), it should have precedence
    //user 2
    val u2op1 = LinearInsertOperation(0, 'a', OperationId(), OperationContext(List.empty), user2Id)
    val u2Msg1 = OperationMessage(user2Id, dataTypeInstanceId, StringDataTypeFactory.name, List(u2op1))
    val u2op2 = LinearInsertOperation(0, 'b', OperationId(), OperationContext(List(u2op1.id)), user2Id)
    val u2Msg2 = OperationMessage(user2Id, dataTypeInstanceId, StringDataTypeFactory.name, List(u2op2))
    val u2op3 = LinearInsertOperation(0, 'c', OperationId(), OperationContext(List(u2op2.id)), user2Id)
    val u2Msg3 = OperationMessage(user2Id, dataTypeInstanceId, StringDataTypeFactory.name, List(u2op3))
    //user 1
    val u1op1 = LinearInsertOperation(0, '1', OperationId(), OperationContext(List.empty), user1Id)
    val u1Msg1 = OperationMessage(user1Id, dataTypeInstanceId, StringDataTypeFactory.name, List(u1op1))
    val u1op2 = LinearInsertOperation(0, '2', OperationId(), OperationContext(List(u1op1.id)), user1Id)
    val u1Msg2 = OperationMessage(user1Id, dataTypeInstanceId, StringDataTypeFactory.name, List(u1op2))
    val u1op3 = LinearInsertOperation(0, '3', OperationId(), OperationContext(List(u1op2.id)), user1Id)
    val u1Msg3 = OperationMessage(user1Id, dataTypeInstanceId, StringDataTypeFactory.name, List(u1op3))
    user2Outgoing.offer(TextMessage(write(u2Msg1)))
    user2Outgoing.offer(TextMessage(write(u2Msg2)))
    user2Outgoing.offer(TextMessage(write(u2Msg3)))
    // 3 acks for u2
    verifyEqual(user2Incoming.pull(), u2Msg1)
    verifyEqual(user2Incoming.pull(), u2Msg2)
    verifyEqual(user2Incoming.pull(), u2Msg3)
    //3 incoming for u1
    verifyEqual(user1Incoming.pull(), u2Msg1)
    verifyEqual(user1Incoming.pull(), u2Msg2)
    verifyEqual(user1Incoming.pull(), u2Msg3)


    user1Outgoing.offer(TextMessage(write(u1Msg1)))
    user1Outgoing.offer(TextMessage(write(u1Msg2)))
    user1Outgoing.offer(TextMessage(write(u1Msg3)))
    // 3 acks for u1

    val transformedu1op1 = LinearInsertOperation(u1op1.index, u1op1.o, u1op1.id, OperationContext(List(u2op3.id)), user1Id)
    val transformedu1Msg1 = OperationMessage(user1Id, u1Msg1.dataTypeInstanceId, u1Msg1.dataType, List(transformedu1op1))
    verifyEqual(user1Incoming.pull(), transformedu1Msg1)
    verifyEqual(user1Incoming.pull(), u1Msg2)
    verifyEqual(user1Incoming.pull(), u1Msg3)
    //3 incoming for u2
    verifyEqual(user2Incoming.pull(), transformedu1Msg1)
    verifyEqual(user2Incoming.pull(), u1Msg2)
    verifyEqual(user2Incoming.pull(), u1Msg3)
  }

  def createStringDataTypeInstance(user1Id: ClientId, user2Id: ClientId, user1Incoming: SinkQueueWithCancel[Message], user1Outgoing: SourceQueueWithComplete[Message], user2Incoming: SinkQueueWithCancel[Message], user2Outgoing: SourceQueueWithComplete[Message])(implicit executionContext: ExecutionContext): DataTypeInstanceId = {
    val dataTypeInstanceId = DataTypeInstanceId()
    user1Outgoing.offer(TextMessage(write(CreateRequest(user1Id, dataTypeInstanceId, StringDataTypeFactory.name))))

    val incomingCreateResponse = user1Incoming.pull()
    Await.ready(incomingCreateResponse, 3 seconds)
    incomingCreateResponse.value.get match {
      case Success(m) =>
        val text = m.get.asTextMessage.getStrictText
        read[FormicMessage](text) should equal(CreateResponse(dataTypeInstanceId))
      case Failure(ex) => fail(ex)
    }

    user2Outgoing.offer(TextMessage(write(UpdateRequest(user2Id, dataTypeInstanceId))))

    val incomingUpdateResponse = user2Incoming.pull()
    //can't start sending operation messages before the client is subscribed to the data type instance
    Await.ready(incomingUpdateResponse, 5 seconds)
    incomingUpdateResponse.value.get match {
      case Success(m) =>
        val text = m.get.asTextMessage.getStrictText
        read[FormicMessage](text) should equal(UpdateResponse(dataTypeInstanceId, StringDataTypeFactory.name, "[]", Option.empty))
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
    val serverPort = server.binding.localAddress.getPort
    val (upgradeResponse, sinkAndSource) =
      Http().singleWebSocketRequest(
        WebSocketRequest(
          Uri(s"ws://0.0.0.0:$serverPort/formic"),
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

    val result = Await.ready(connected, 6 seconds)

    result.value.get match {
      case Success(_) => sinkAndSource
      case Failure(ex) => throw ex
    }
  }
}
