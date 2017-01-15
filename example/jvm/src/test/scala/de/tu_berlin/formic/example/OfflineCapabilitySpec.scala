package de.tu_berlin.formic.example

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.testkit.TestKit
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.{FormicSystemFactory, NewInstanceCallback}
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
import de.tu_berlin.formic.datatype.linear.client.FormicString
import de.tu_berlin.formic.example.OfflineCapabilitySpec.CollectingCallback
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author Ronny Bräunlich
  */
class OfflineCapabilitySpec extends TestKit(ActorSystem("ParallelEditingSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  implicit val ec = system.dispatcher

  var serverThread: ServerThread = _

  override def afterAll(): Unit = {
    system.terminate()
    if(serverThread != null) serverThread.terminate()
  }

  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))

  "A FormicClient" must {
    "be able to handle disconnection from the server" in {
      val user = FormicSystemFactory.create(ConfigFactory.parseString("akka {\n  loglevel = debug\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}"))
      //the online possibility to check the exchange with the server is to use a second user,
      // else we cannot distinguish between online and offline, which is intentional
      val userForCheck = FormicSystemFactory.create(ConfigFactory.parseString("akka {\n  loglevel = debug\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}"))
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
      Thread.sleep(1000)
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
}