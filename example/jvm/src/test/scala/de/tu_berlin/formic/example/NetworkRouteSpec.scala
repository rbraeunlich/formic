package de.tu_berlin.formic.example

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server.AuthenticationFailedRejection
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
/**
  * @author Ronny Bräunlich
  */
class NetworkRouteSpec extends WordSpecLike
  with ScalatestRouteTest
  with Matchers
  with BeforeAndAfterAll{

  override def afterAll(): Unit = {
    super.afterAll()
  }

  "Network route" must {
    "reject users without authentification" in {
      implicit val materializer = ActorMaterializer()
      val probe = WSProbe()
      val route = new NetworkRoute()(system).route((_) => Flow.fromSinkAndSource(Sink.ignore, Source.empty))

      WS("/formic", probe.flow) ~> route ~> check {
        rejection shouldBe a[AuthenticationFailedRejection]
      }
    }

    "accept users with unique username independent of password" in {
      implicit val materializer = ActorMaterializer()
      val probe = WSProbe()
      val route = new NetworkRoute()(system).route((_) => Flow.fromSinkAndSource(Sink.ignore, Source.empty))

      WS("/formic", probe.flow).addCredentials(BasicHttpCredentials("NetworkRoute", "")) ~> route ~> check {
        isWebSocketUpgrade should be(true)
        status should be(StatusCodes.SwitchingProtocols)
      }
    }

    "reject users with duplicate username" in {
      implicit val materializer = ActorMaterializer()
      val probe = WSProbe()
      val probe2 = WSProbe()
      val route = new NetworkRoute()(system).route((_) => Flow.fromSinkAndSource(Sink.ignore, Source.empty))

      WS("/formic", probe.flow).addCredentials(BasicHttpCredentials("NetworkRoute2", "")) ~> route ~> check {
        isWebSocketUpgrade should be(true)
        status should be(StatusCodes.SwitchingProtocols)
      }

      WS("/formic", probe2.flow).addCredentials(BasicHttpCredentials("NetworkRoute2", "")) ~> route ~> check {
        rejection shouldBe a[AuthenticationFailedRejection]
      }
    }
  }
}
