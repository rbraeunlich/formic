package de.tu_berlin.formic.example

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.testkit.TestKit
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client._
import de.tu_berlin.formic.common.datatype.{ClientDataTypeProvider, DataTypeName, FormicDataType}
import de.tu_berlin.formic.datatype.linear.client.{FormicString, LinearClientDataTypeProvider}
import de.tu_berlin.formic.example.OfflineCapabilitySpec.{CollectingCallback, DropNextNMessages, TestWebSocketFactoryJVM}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author Ronny Bräunlich
  */
class OfflineCapabilitySpec extends TestKit(ActorSystem("ParallelEditingSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with PersistenceCleanup {

  implicit val ec = system.dispatcher

  var serverThread: ServerThread = _

  override def afterAll(): Unit = {
    system.terminate()
    if(serverThread != null) serverThread.terminate()
    deleteStorageLocations(serverThread.exampleServer.server.system)
  }

  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))

  val config = ConfigFactory.parseString("akka {\n  loglevel = debug\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}")

  "A FormicClient" must {
    "be able to handle disconnection from the server" in {
      val user = FormicSystemFactory.create(config, Set(LinearClientDataTypeProvider()))
      //the online possibility to check the exchange with the server is to use a second user,
      // else we cannot distinguish between online and offline, which is intentional
      val userForCheck = FormicSystemFactory.create(config, Set(LinearClientDataTypeProvider()))
      val userCallback = new CollectingCallback
      user.init(userCallback)
      val checkUserCallback = new CollectingCallback
      userForCheck.init(checkUserCallback)
      Thread.sleep(2000)
      val string = new FormicString(() => {}, user)
      Thread.sleep(1000)
      string.add(0, 'a')
      string.add(1, 'b')
      string.add(2, 'c')
      Thread.sleep(1000)

      val textFuture = string.getAll()
      Await.result(textFuture, 2.seconds).mkString should equal("abc")

      serverThread = new ServerThread
      serverThread.setDaemon(true)
      serverThread.run()
      Thread.sleep(10000) //retry for websocket is 5 seconds
      //Client should now exchange operations
      userForCheck.requestDataType(string.dataTypeInstanceId)
      Thread.sleep(3000)
      checkUserCallback.dataTypes shouldNot be(empty)
      val checkUserString = checkUserCallback.dataTypes.head.asInstanceOf[FormicString]
      val checkTextFuture = checkUserString.getAll()
      Await.result(checkTextFuture, 2.seconds).mkString should equal("abc")

      serverThread.terminate()
      Thread.sleep(2000)
      string.remove(1)
      string.remove(1)
      checkUserString.add(3, 'd')

      serverThread = new ServerThread
      serverThread.setDaemon(true)
      serverThread.run()
      Thread.sleep(12000)

      Await.result(string.getAll(), 2.seconds).mkString should equal("ad")
      Await.result(checkUserString.getAll(), 2.seconds).mkString should equal("ad")
    }

    "request historic operations if it missed some because of being offline" in {
      if(serverThread == null){
        serverThread = new ServerThread
        serverThread.setDaemon(true)
        serverThread.run()
        Thread.sleep(5000)
      }
      val user1 = FormicSystemFactory.create(config, Set(LinearClientDataTypeProvider()))
      //we need a special wrapper for user2 so we can intentionally drop messages
      val user2WebSocketFactory = new TestWebSocketFactoryJVM()
      val user2 = new FormicSystem(config, user2WebSocketFactory) with ClientDataTypes {
        override val dataTypeProvider: Set[ClientDataTypeProvider] = Set(LinearClientDataTypeProvider())
      }
      val user1Callback = new CollectingCallback
      user1.init(user1Callback)
      val user2Callback = new CollectingCallback
      user2.init(user2Callback)
      Thread.sleep(2000)
      val string = new FormicString(() => {}, user1)
      Thread.sleep(1000)
      user2.requestDataType(string.dataTypeInstanceId)
      Thread.sleep(1000)
      user2Callback.dataTypes shouldNot be(empty)
      val user2String = user2Callback.dataTypes.head.asInstanceOf[FormicString]

      //tell the wrapper to drop the next three messages
      user2WebSocketFactory.dropMessageActor ! DropNextNMessages(3)
      //apply some changes
      string.add(0, 'a')
      string.add(1, 'b')
      string.add(2, 'c')
      //perform a change so user2 receives an operation whose predecessor it does not know
      string.add(3, 'd')
      //wait for user 2 to apply historic operations
      Thread.sleep(3000)
      Await.result(string.getAll(), 2.seconds).mkString should equal("abcd")
      Await.result(user2String.getAll(), 2.seconds).mkString should equal("abcd")
    }
  }
}

object OfflineCapabilitySpec {
  class CollectingCallback extends NewInstanceCallback {

    var dataTypes: List[FormicDataType] = List.empty

    /**
      * Set a new callback interface at a data type instance that was created remotely.
      */
    override def newCallbackFor(instance: FormicDataType, dataType: DataTypeName): () => Unit = () => Unit

    /**
      * Perform any initializations necessary for a new, remote data type.
      */
    override def doNewInstanceCreated(instance: FormicDataType, dataType: DataTypeName): Unit = {
      dataTypes = instance :: dataTypes
    }
  }

  class TestWebSocketFactoryJVM()(implicit val materializer: ActorMaterializer, val actorSystem: ActorSystem) extends WebSocketFactory {
    var dropMessageActor:ActorRef = _

    override def createConnection(url: String, connection: ActorRef): WebSocketWrapper = {
      dropMessageActor = actorSystem.actorOf(Props(new MessageDroppingConnectionWrapper(connection)))
      new WrappedAkkaStreamWebSocket(url, dropMessageActor)(materializer, actorSystem)
    }
  }
  class MessageDroppingConnectionWrapper(val wrapped: ActorRef) extends Actor with ActorLogging {

    var dropMsgCounter = 0

    def receive = {
      case DropNextNMessages(n) => dropMsgCounter = n
      case rest =>
        if(dropMsgCounter > 0) {
          dropMsgCounter -= 1
          log.debug(s"Dropping message $rest")
        }
        else wrapped forward rest
    }
  }

  case class DropNextNMessages(n: Int)
}
