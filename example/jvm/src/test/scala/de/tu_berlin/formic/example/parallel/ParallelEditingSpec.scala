package de.tu_berlin.formic.example.parallel

import java.util.concurrent.{CountDownLatch, TimeUnit}

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.testkit.TestKit
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.{FormicSystemFactory, NewInstanceCallback}
import de.tu_berlin.formic.common.ClientId
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
import de.tu_berlin.formic.datatype.json._
import de.tu_berlin.formic.datatype.json.client.FormicJsonObject
import de.tu_berlin.formic.datatype.linear.client.FormicString
import de.tu_berlin.formic.datatype.tree.client.{FormicIntegerTree, FormicStringTree, FormicTree}
import de.tu_berlin.formic.datatype.tree.{AccessPath, TreeNode, ValueTreeNode}
import de.tu_berlin.formic.example.ServerThread
import de.tu_berlin.formic.example.parallel.ParallelEditingSpec.CollectingCallback
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author Ronny Bräunlich
  */
class ParallelEditingSpec extends TestKit(ActorSystem("ParallelEditingSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  val host = "http://localhost:8080"

  var serverThread: ServerThread = _

  implicit val ec = system.dispatcher

  override def beforeAll(): Unit = {
    serverThread = new ServerThread
    serverThread.setDaemon(true)
    serverThread.run()
    Thread.sleep(3000)
  }

  override def afterAll(): Unit = {
    serverThread.terminate()
    system.terminate()
  }

  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))

  "The formic system" must {
    "result in a consistent linear structure when parallel edits happen" in {
      val user1Id = ClientId("2")
      val user2Id = ClientId("1") //important user1 > user2
      val user1 = FormicSystemFactory.create(ConfigFactory.parseString("akka {\n  loglevel = debug\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}"))
      val user2 = FormicSystemFactory.create(ConfigFactory.parseString("akka {\n  loglevel = debug\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}"))
      val user1Callback = new CollectingCallback
      val user2Callback = new CollectingCallback
      user1.init(user1Callback, user1Id)
      user2.init(user2Callback, user2Id)
      Thread.sleep(3000)
      val stringUser1 = new FormicString(() => {}, user1)
      Thread.sleep(1000) //send the CreateRequest to the server
      user2.requestDataType(stringUser1.dataTypeInstanceId)
      awaitCond(user2Callback.dataTypes.nonEmpty, 5.seconds)
      val stringUser2 = user2Callback.dataTypes.head.asInstanceOf[FormicString]

      //parallel insertion
      stringUser2.add(0, 'b')
      stringUser1.add(0, 'a')
      Thread.sleep(1000)
      stringUser1.add(2, 'c')
      stringUser2.add(2, 'd')
      Thread.sleep(1000)

      checkTextOfBothStrings(stringUser1, stringUser2, "abcd")

      //deletion
      stringUser1.add(4, 'e')
      stringUser1.add(5, 'e')
      Thread.sleep(1000)
      stringUser2.remove(5)
      stringUser2.remove(4)
      stringUser1.remove(4)
      Thread.sleep(1000)

      checkTextOfBothStrings(stringUser1, stringUser2, "abcd")

      //deletion and insertion
      stringUser1.add(4, 'e')
      stringUser2.remove(3)
      Thread.sleep(1000)
      stringUser1.add(3, 'd')
      stringUser2.remove(3)
      Thread.sleep(1000)

      checkTextOfBothStrings(stringUser1, stringUser2, "abcd")

      //one operation with several ones parallel
      stringUser1.add(4, 'e')
      stringUser1.add(5, 'f')
      stringUser1.add(6, 'g')
      stringUser1.add(7, 'h')
      stringUser2.add(1, 'ä')
      Thread.sleep(1000)

      checkTextOfBothStrings(stringUser1, stringUser2, "aäbcdefgh")

      //an operation parallel to a no-op
      stringUser1.remove(1)
      stringUser2.remove(1)
      stringUser1.add(8, 'i')
      Thread.sleep(1000)
      checkTextOfBothStrings(stringUser1, stringUser2, "abcdefghi")

      user1.system.terminate()
      user2.system.terminate()
    }

    def checkTextOfBothStrings(stringUser1: FormicString, stringUser2: FormicString, expected: String) = {
      val user1Text = stringUser1.getAll()
      val user2Text = stringUser2.getAll()
      val user1Result = Await.result(user1Text, 3.seconds)
      val user2Result = Await.result(user2Text, 3.seconds)
      user1Result.mkString should equal(expected)
      user2Result.mkString should equal(expected)
    }

    "result in a consistent tree when parallel edits happen" in {
      val user1Id = ClientId("4")
      val user2Id = ClientId("3") //important user1 > user2
      val user1 = FormicSystemFactory.create(ConfigFactory.parseString("akka {\n  loglevel = debug\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}"))
      val user2 = FormicSystemFactory.create(ConfigFactory.parseString("akka {\n  loglevel = debug\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}"))
      val user1Callback = new CollectingCallback
      val user2Callback = new CollectingCallback
      user1.init(user1Callback, user1Id)
      user2.init(user2Callback, user2Id)
      Thread.sleep(3000)
      val treeUser1 = new FormicIntegerTree(() => {}, user1)
      Thread.sleep(1000)
      //insert root node
      treeUser1.insert(1, AccessPath())
      Thread.sleep(1000) //send the CreateRequest to the server
      user2.requestDataType(treeUser1.dataTypeInstanceId)
      awaitCond(user2Callback.dataTypes.nonEmpty, 5.seconds)
      val treeUser2 = user2Callback.dataTypes.head.asInstanceOf[FormicIntegerTree]

      //parallel insertion
      treeUser2.insert(20, AccessPath(0))
      treeUser1.insert(10, AccessPath(0))
      Thread.sleep(1000)
      treeUser1.insert(11, AccessPath(1))
      treeUser2.insert(21, AccessPath(2))
      Thread.sleep(1000)
      checkBothTrees(treeUser1, treeUser2, ValueTreeNode(1, List(ValueTreeNode(10), ValueTreeNode(11), ValueTreeNode(20), ValueTreeNode(21))))

      //deletion
      treeUser1.remove(AccessPath(2))
      treeUser1.remove(AccessPath(3))
      treeUser2.remove(AccessPath(3))
      Thread.sleep(1000)
      checkBothTrees(treeUser1, treeUser2, ValueTreeNode(1, List(ValueTreeNode(10), ValueTreeNode(11))))

      //deletion on different level
      treeUser1.insert(12, AccessPath(2))
      treeUser1.insert(110, AccessPath(2, 0))
      Thread.sleep(1000)
      treeUser2.remove(AccessPath(2))
      treeUser1.remove(AccessPath(2, 0))
      Thread.sleep(1000)
      checkBothTrees(treeUser1, treeUser2, ValueTreeNode(1, List(ValueTreeNode(10), ValueTreeNode(11))))

      //effect independence
      treeUser1.insert(110, AccessPath(0, 0))
      treeUser2.insert(220, AccessPath(1, 0))
      Thread.sleep(1000)
      checkBothTrees(treeUser1, treeUser2, ValueTreeNode(1, List(ValueTreeNode(10, List(ValueTreeNode(110))), ValueTreeNode(11, List(ValueTreeNode(220))))))

      treeUser2.remove(AccessPath(0, 0))
      treeUser1.insert(13, AccessPath(1))
      Thread.sleep(1000)
      checkBothTrees(treeUser1, treeUser2, ValueTreeNode(1, List(ValueTreeNode(10), ValueTreeNode(13), ValueTreeNode(11, List(ValueTreeNode(220))))))

      treeUser1.remove(AccessPath(2, 0))
      treeUser2.insert(23, AccessPath(0))
      Thread.sleep(1000)
      checkBothTrees(treeUser1, treeUser2, ValueTreeNode(1, List(ValueTreeNode(23), ValueTreeNode(10), ValueTreeNode(13), ValueTreeNode(11))))

      //an operation parallel to a no-op
      treeUser1.remove(AccessPath(0))
      treeUser2.remove(AccessPath(0))
      treeUser2.insert(200, AccessPath(0))
      Thread.sleep(1000)
      checkBothTrees(treeUser1, treeUser2, ValueTreeNode(1, List(ValueTreeNode(200), ValueTreeNode(10), ValueTreeNode(13), ValueTreeNode(11))))

      //one operation with several ones parallel
      treeUser1.insert(5000, AccessPath(3, 0))
      treeUser1.insert(6000, AccessPath(3, 0))
      treeUser1.insert(7000, AccessPath(3, 0))
      treeUser1.insert(8000, AccessPath(3, 0))
      treeUser2.remove(AccessPath(0))
      Thread.sleep(1000)

      checkBothTrees(treeUser1, treeUser2, ValueTreeNode(1, List(ValueTreeNode(10), ValueTreeNode(13), ValueTreeNode(11, List(ValueTreeNode(8000), ValueTreeNode(7000), ValueTreeNode(6000), ValueTreeNode(5000))))))

      user1.system.terminate()
      user2.system.terminate()
    }

    def checkBothTrees[T](treeUser1: FormicTree[T], treeUser2: FormicTree[T], expected: TreeNode): Any = {
      val user1Tree = treeUser1.getTree()
      val user2Tree = treeUser2.getTree()
      val user1Result = Await.result(user1Tree, 3.seconds)
      val user2Result = Await.result(user2Tree, 3.seconds)
      user1Result should equal(expected)
      user2Result should equal(expected)
    }

    "result in a consistent JSON when parallel edits happen" in {
      val user1Id = ClientId("6")
      val user2Id = ClientId("5") //important user1 > user2
      val user1 = FormicSystemFactory.create(ConfigFactory.parseString("akka {\n  loglevel = debug\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}"))
      val user2 = FormicSystemFactory.create(ConfigFactory.parseString("akka {\n  loglevel = debug\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}"))
      val user1Callback = new CollectingCallback
      val user2Callback = new CollectingCallback
      user1.init(user1Callback, user1Id)
      user2.init(user2Callback, user2Id)
      Thread.sleep(3000)
      val jsonUser1 = new FormicJsonObject(() => {}, user1)
      Thread.sleep(1000) //send the CreateRequest to the server
      user2.requestDataType(jsonUser1.dataTypeInstanceId)
      awaitCond(user2Callback.dataTypes.nonEmpty, 7.seconds)
      val jsonUser2 = user2Callback.dataTypes.head.asInstanceOf[FormicJsonObject]

      //parallel insertion
      jsonUser1.insert(1.23, JsonPath("num1"))
      jsonUser2.insert(2.34, JsonPath("num2"))
      Thread.sleep(1000)
      jsonUser2.insert(b = true, JsonPath("bool1"))
      jsonUser1.insert(b = false, JsonPath("bool2"))
      Thread.sleep(1000)
      checkBothJsons(jsonUser1, jsonUser2,
        ObjectNode(null, List(
          NumberNode("num1", 1.23),
          NumberNode("num2", 2.34),
          BooleanNode("bool1", value = true),
          BooleanNode("bool2", value = false))))

      //parallel replace
      jsonUser1.replace(3.45, JsonPath("num1"))
      jsonUser2.replace(4.56, JsonPath("num1"))
      Thread.sleep(2000)
      checkBothJsons(jsonUser1, jsonUser2,
        ObjectNode(null, List(
          NumberNode("num1", 3.45),
          NumberNode("num2", 2.34),
          BooleanNode("bool1", value = true),
          BooleanNode("bool2", value = false))))

      //deletion
      jsonUser1.remove(JsonPath("bool2"))
      jsonUser1.remove(JsonPath("bool1"))
      jsonUser2.remove(JsonPath("bool1"))
      Thread.sleep(2000)
      checkBothJsons(jsonUser1, jsonUser2,
        ObjectNode(null, List(
          NumberNode("num1", 3.45),
          NumberNode("num2", 2.34))))

      //deletion on different level
      jsonUser2.insert(Array("text1", "text2"), JsonPath("string"))
      Thread.sleep(2000)
      jsonUser1.remove(JsonPath("string", "1"))
      jsonUser2.remove(JsonPath("num1"))
      Thread.sleep(2000)
      checkBothJsons(jsonUser1, jsonUser2,
        ObjectNode(null, List(
          NumberNode("num2", 2.34),
          ArrayNode("string", List(
            StringNode(null, List(
              CharacterNode(null, 't'),
              CharacterNode(null, 'e'),
              CharacterNode(null, 'x'),
              CharacterNode(null, 't'),
              CharacterNode(null, '1')))
          )))))

      //an operation parallel to a no-op
      jsonUser1.remove(JsonPath("string"))
      jsonUser2.remove(JsonPath("string"))
      jsonUser2.insert(b = false, JsonPath("bool"))
      Thread.sleep(2000)
      checkBothJsons(jsonUser1, jsonUser2,
        ObjectNode(null, List(
          BooleanNode("bool", value = false),
          NumberNode("num2", 2.34))))

      //one operation with several ones parallel
      jsonUser1.insert(Array(1.0), JsonPath("arr"))
      jsonUser1.insert(2.0, JsonPath("arr", "0"))
      jsonUser1.insert(3.0, JsonPath("arr", "0"))
      jsonUser1.insert(4.0, JsonPath("arr", "0"))
      jsonUser2.remove(JsonPath("bool"))
      Thread.sleep(3000)
      checkBothJsons(jsonUser1, jsonUser2,
        ObjectNode(null, List(
          ArrayNode("arr", List(
            NumberNode(null, 4.0),
            NumberNode(null, 3.0),
            NumberNode(null, 2.0),
            NumberNode(null, 1.0))),
          NumberNode("num2", 2.34)
        )))

      //effect independence
      jsonUser1.remove(JsonPath("arr"))
      jsonUser1.insert(Array(1), JsonPath("a"))
      jsonUser1.insert(Array(10), JsonPath("z"))
      Thread.sleep(3000)
      jsonUser1.insert(2, JsonPath("a", "1"))
      jsonUser2.insert(20, JsonPath("z", "1"))
      Thread.sleep(1000)
      checkBothJsons(jsonUser1, jsonUser2,
        ObjectNode(null, List(
          ArrayNode("a", List(
            NumberNode(null, 1),
            NumberNode(null, 2)
          )),
          NumberNode("num2", 2.34),
          ArrayNode("z", List(
            NumberNode(null, 10),
            NumberNode(null, 20)
          )))))

      jsonUser2.replace(3.45, JsonPath("num2"))
      jsonUser1.remove(JsonPath("z", "1"))
      Thread.sleep(1000)
      checkBothJsons(jsonUser1, jsonUser2,
        ObjectNode(null, List(
          ArrayNode("a", List(
            NumberNode(null, 1),
            NumberNode(null, 2)
          )),
          NumberNode("num2", 3.45),
          ArrayNode("z", List(
            NumberNode(null, 10)
          )))))

      jsonUser1.remove(JsonPath("a", "0"))
      jsonUser2.insert(30, JsonPath("z", "0"))
      Thread.sleep(1000)
      checkBothJsons(jsonUser1, jsonUser2,
        ObjectNode(null, List(
          ArrayNode("a", List(
            NumberNode(null, 2)
          )),
          NumberNode("num2", 3.45),
          ArrayNode("z", List(
            NumberNode(null, 30),
            NumberNode(null, 10)
          )))))

      user1.system.terminate()
      user2.system.terminate()
    }

    def checkBothJsons(jsonUser1: FormicJsonObject, jsonUser2: FormicJsonObject, expected: ObjectNode): Any = {
      val user1Json = jsonUser1.getNodeAt(JsonPath())
      val user2Json = jsonUser2.getNodeAt(JsonPath())
      val user1Result = Await.result(user1Json, 3.seconds)
      val user2Result = Await.result(user2Json, 3.seconds)
      user1Result should equal(expected)
      user2Result should equal(expected)
    }

    "handle many parallel edits on linear structure" in {
      val iterations = 1000
      val user1Id = ClientId("8")
      val user2Id = ClientId("7") //important user1 > user2
      val user1 = FormicSystemFactory.create(ConfigFactory.parseString("akka {\n  loglevel = info\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}"))
      val user2 = FormicSystemFactory.create(ConfigFactory.parseString("akka {\n  loglevel = info\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}"))
      val user1Callback = new CollectingCallback
      val user2Callback = new CollectingCallback
      val latch = new CountDownLatch((iterations * 2 + iterations) * 2) //every local operation results in two callback invocations, every remote one in one and that for two users
      user1.init(user1Callback, user1Id)
      user2.init(user2Callback, user2Id)
      Thread.sleep(3000)
      val stringUser1 = new FormicString(() => {
        latch.countDown()
      }, user1)
      Thread.sleep(1000) //send the CreateRequest to the server
      user2.requestDataType(stringUser1.dataTypeInstanceId)
      awaitCond(user2Callback.dataTypes.nonEmpty, 5.seconds)
      val stringUser2 = user2Callback.dataTypes.head.asInstanceOf[FormicString]
      stringUser2.callback = () => latch.countDown()

      for (x <- 0.until(iterations)) {
        stringUser1.add(0, 'a')
        stringUser2.add(x, 'z')
      }
      val firstHalf = for (x <- 0.until(iterations)) yield 'a'
      val secondHalf = for (x <- 0.until(iterations)) yield 'z'
      val expected = firstHalf.mkString + secondHalf.mkString

      latch.await(60, TimeUnit.SECONDS)

      checkTextOfBothStrings(stringUser1, stringUser2, expected)

      user1.system.terminate()
      user2.system.terminate()
    }

    "handle many parallel edits on tree structure" in {
      val iterations = 1000
      val user1Id = ClientId("11")
      val user2Id = ClientId("10") //important user1 > user2
      val user1 = FormicSystemFactory.create(ConfigFactory.parseString("akka {\n  loglevel = info\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}"))
      val user2 = FormicSystemFactory.create(ConfigFactory.parseString("akka {\n  loglevel = info\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}"))
      val user1Callback = new CollectingCallback
      val user2Callback = new CollectingCallback
      val latch = new CountDownLatch((iterations * 2 + iterations) * 2) //every local operation results in two callback invocations, every remote one in one and that for two users
      user1.init(user1Callback, user1Id)
      user2.init(user2Callback, user2Id)
      Thread.sleep(3000)
      val treeUser1 = new FormicStringTree(() => {}, user1)
      Thread.sleep(1000)
      //insert root node
      treeUser1.insert("1", AccessPath())
      Thread.sleep(1000) //send the CreateRequest to the server
      user2.requestDataType(treeUser1.dataTypeInstanceId)
      awaitCond(user2Callback.dataTypes.nonEmpty, 5.seconds)
      val treeUser2 = user2Callback.dataTypes.head.asInstanceOf[FormicStringTree]
      //because we add the root node, we set the correct callbacks here
      treeUser1.callback = () => latch.countDown()
      treeUser2.callback = () => latch.countDown()

      for(x <- 0.until(iterations)) {
        treeUser1.insert("a", AccessPath(0))
        treeUser2.insert("z", AccessPath(x))
      }

      val firstHalf = (for(x <- 0.until(iterations)) yield ValueTreeNode("a")).toList
      val secondHalf = (for(x <- 0.until(iterations)) yield ValueTreeNode("z")).toList
      val expected = ValueTreeNode("1", firstHalf ++ secondHalf)

      latch.await(60, TimeUnit.SECONDS)

      checkBothTrees(treeUser1, treeUser2, expected)

      user1.system.terminate()
      user2.system.terminate()
    }

    "handle many parallel edits on json structure" in {
      val iterations = 1000
      val user1Id = ClientId("13")
      val user2Id = ClientId("12") //important user1 > user2
      val user1 = FormicSystemFactory.create(ConfigFactory.parseString("akka {\n  loglevel = info\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}"))
      val user2 = FormicSystemFactory.create(ConfigFactory.parseString("akka {\n  loglevel = info\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}"))
      val user1Callback = new CollectingCallback
      val user2Callback = new CollectingCallback
      val latch = new CountDownLatch((iterations * 2 + iterations) * 2) //every local operation results in two callback invocations, every remote one in one and that for two users
      user1.init(user1Callback, user1Id)
      user2.init(user2Callback, user2Id)
      Thread.sleep(3000)
      val jsonUser1 = new FormicJsonObject(() => {}, user1)
      Thread.sleep(1000) //send the CreateRequest to the server
      user2.requestDataType(jsonUser1.dataTypeInstanceId)
      awaitCond(user2Callback.dataTypes.nonEmpty, 5.seconds)
      val jsonUser2 = user2Callback.dataTypes.head.asInstanceOf[FormicJsonObject]
      //because we add the root node, we set the correct callbacks here
      jsonUser1.callback = () => latch.countDown()
      jsonUser2.callback = () => latch.countDown()

      for(x <- 0.until(iterations)) {
        jsonUser1.insert(1, JsonPath(s"a$x"))
        jsonUser2.insert(2, JsonPath(s"z$x"))
      }

      val firstHalf = (for(x <- 0.until(iterations)) yield NumberNode(s"a$x", 1)).toList
      val secondHalf = (for(x <- 0.until(iterations)) yield NumberNode(s"z$x", 2)).toList
      val expected = ObjectNode(null, firstHalf ++ secondHalf)

      latch.await(60, TimeUnit.SECONDS)

      checkBothJsons(jsonUser1, jsonUser2, expected)

      user1.system.terminate()
      user2.system.terminate()
    }
  }
}

object ParallelEditingSpec {

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
