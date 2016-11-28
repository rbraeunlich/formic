package de.tu_berlin.formic.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import de.tu_berlin.formic.client.WebSocketConnection.OnConnect
import de.tu_berlin.formic.common.ClientId
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class WebSocketConnectionSpec extends TestKit(ActorSystem("WebSocketConnectionSpec"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers{

  override def afterAll(): Unit = {
    system.terminate()
  }

  "WebSocketConnection" must {
    "create a dispatcher after connecting" in {
      val connection: TestActorRef[WebSocketConnection] = TestActorRef(Props(new WebSocketConnection(TestProbe().ref, TestProbe().ref, ClientId())))

      connection ! OnConnect

      connection.underlyingActor.dispatcher should not be null
    }
  }
}
