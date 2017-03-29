package de.tu_berlin.formic.client

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.FormicSystemIntegrationSpec.MockWebSocketFactory
import de.tu_berlin.formic.client.WebSocketConnection.{OnConnect, OnMessage}
import de.tu_berlin.formic.common.{ClientId, OperationId}
import de.tu_berlin.formic.common.datatype.client.ClientDataTypeEvent
import de.tu_berlin.formic.common.datatype.{ClientDataStructureProvider, DataStructureName, FormicDataType, OperationContext}
import de.tu_berlin.formic.common.message._
import de.tu_berlin.formic.datatype.linear.{LinearInsertOperation, LinearNoOperation}
import de.tu_berlin.formic.datatype.linear.client.{FormicString, LinearClientDataStructureProvider}
import org.scalatest.{Matchers, WordSpecLike}
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
class FormicSystemIntegrationSpec extends TestKit(ActorSystem("FormicSystemIntegrationSpec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "FormicSystem" must {
    "send NoOperation messages to the server if the transformations resulted in one for a linear structure" in {
      val mockSocketFactory = MockWebSocketFactory()
      val formicSystem = new FormicSystem(ConfigFactory.load(), mockSocketFactory) with ClientDataTypes{
        override val dataTypeProvider: Set[ClientDataStructureProvider] = Set(new LinearClientDataStructureProvider)
      }
      formicSystem.init(new NewInstanceCallback {

      override def doNewInstanceCreated(instance: FormicDataType, dataType: DataStructureName): Unit = {}

        override def newCallbackFor(instance: FormicDataType, dataType: DataStructureName): (ClientDataTypeEvent) => Unit = _ => {}
      }, ClientId("2"))
      implicit val writer = formicSystem.jsonProtocol.writer
      implicit val reader = formicSystem.jsonProtocol.reader
      val webSocketConnection = formicSystem.connection
      Thread.sleep(1000)
      webSocketConnection ! OnConnect(mockSocketFactory.wrapper)
      Thread.sleep(2000)
      val string = new FormicString(_ => {}, formicSystem)
      Thread.sleep(2000)
      val createRequest = read[FormicMessage](mockSocketFactory.wrapper.sent.last)
      createRequest shouldBe a[CreateRequest]

      webSocketConnection ! OnMessage(write(CreateResponse(createRequest.asInstanceOf[CreateRequest].dataStructureInstanceId)))

      string.add(0, 'a')
      string.add(1, 'a')
      string.add(2, 'a')
      string.add(0, 'a')
      string.add(1, 'a')

      Thread.sleep(1000)
      val opMsg1 = read[FormicMessage](mockSocketFactory.wrapper.sent.last)
      opMsg1 shouldBe a[OperationMessage]
      //send ack
      webSocketConnection ! OnMessage(write(opMsg1.asInstanceOf[OperationMessage]))
      Thread.sleep(1000)
      val opMsg2 = read[FormicMessage](mockSocketFactory.wrapper.sent.last)
      opMsg2 shouldBe a[OperationMessage]
      //send ack
      webSocketConnection ! OnMessage(write(opMsg2.asInstanceOf[OperationMessage]))

      val opId = opMsg2.asInstanceOf[OperationMessage].operations.head.id

      //make second operation a no-op
      webSocketConnection ! OnMessage(write(
        OperationMessage(ClientId("3"), opMsg1.asInstanceOf[OperationMessage].dataStructureInstanceId, opMsg1.asInstanceOf[OperationMessage].dataStructure,
          List(LinearInsertOperation(0, 'a', OperationId(), OperationContext(List(opId)), ClientId("3"))))))


      Thread.sleep(1000)
      val opMsg3 = read[FormicMessage](mockSocketFactory.wrapper.sent.last)
      opMsg3 shouldBe a[OperationMessage]
      //send ack
      webSocketConnection ! OnMessage(write(opMsg3.asInstanceOf[OperationMessage]))

      Thread.sleep(1000)
      val opMsg4 = read[FormicMessage](mockSocketFactory.wrapper.sent.last)
      opMsg4 shouldBe a[OperationMessage]
      opMsg4.asInstanceOf[OperationMessage].operations.head shouldBe a[LinearNoOperation]
    }
  }
}

object FormicSystemIntegrationSpec {

  case class MockWebSocketFactory() extends WebSocketFactory {

    val wrapper = new WebSocketWrapper {

      var sent: List[String] = List.empty

      override def send(message: String): Unit = {
        sent = sent :+ message
      }
    }

    override def createConnection(url: String, connection: ActorRef): WebSocketWrapper = {
      wrapper
    }
  }

}