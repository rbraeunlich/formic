package de.tu_berlin.formic.client

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.testkit.{TestKit, TestProbe}
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.common.message.UpdateRequest
import org.scalatest.{Matchers, WordSpecLike}
import upickle.default._
import de.tu_berlin.formic.common.json.FormicJsonProtocol._

/**
  * @author Ronny Bräunlich
  */
class WebSocketFactoryJVMSpec extends TestKit(ActorSystem("WebSocketFactoryJVMSpec"))
  with WordSpecLike
  with Matchers
  with StopSystemAfterAll {

  implicit val actorSystem = system
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))

  "WebSocketFactoryJVM" must {
    "open a WebSocket connection" in {
      val testActor = TestProbe()
      val connection = new WebSocketFactoryJVM().createConnection("ws://foo@echo.websocket.org", testActor.ref)
      val message = UpdateRequest(ClientId(), DataTypeInstanceId())

      connection.send(write(message))

      testActor.expectMsg(message)
    }
  }
}
