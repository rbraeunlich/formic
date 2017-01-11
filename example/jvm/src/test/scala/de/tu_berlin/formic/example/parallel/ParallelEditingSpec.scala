package de.tu_berlin.formic.example.parallel

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.testkit.TestKit
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.{FormicSystemFactory, NewInstanceCallback}
import de.tu_berlin.formic.common.ClientId
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
import de.tu_berlin.formic.datatype.linear.client.FormicString
import de.tu_berlin.formic.datatype.tree.{AccessPath, TreeNode, ValueTreeNode}
import de.tu_berlin.formic.datatype.tree.client.FormicIntegerTree
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
      Thread.sleep(3000)
      stringUser1.add(2, 'c')
      stringUser2.add(2, 'd')
      Thread.sleep(3000)

      checkTextOfBothStrings(stringUser1, stringUser2, "abcd")

      //deletion
      stringUser1.add(4, 'e')
      stringUser1.add(5, 'e')
      Thread.sleep(3000)
      stringUser2.remove(5)
      stringUser2.remove(4)
      stringUser1.remove(4)
      Thread.sleep(3000)

      checkTextOfBothStrings(stringUser1, stringUser2, "abcd")

      //deletion and insertion
      stringUser1.add(4, 'e')
      stringUser2.remove(3)
      Thread.sleep(3000)
      stringUser1.add(3, 'd')
      stringUser2.remove(3)
      Thread.sleep(3000)

      checkTextOfBothStrings(stringUser1, stringUser2, "abcd")

      //one operation with several ones parallel
      stringUser1.add(4, 'e')
      stringUser1.add(5, 'f')
      stringUser1.add(6, 'g')
      stringUser1.add(7, 'h')
      stringUser2.add(1, 'ä')
      Thread.sleep(3000)

      checkTextOfBothStrings(stringUser1, stringUser2, "aäbcdefgh")

      //an operation parallel to a no-op
      stringUser1.remove(1)
      stringUser2.remove(1)
      stringUser1.add(8, 'i')

      checkTextOfBothStrings(stringUser1, stringUser2, "abcdefghi")
    }
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
    treeUser1.insert(110, AccessPath(2,0))
    Thread.sleep(1000)
    treeUser2.remove(AccessPath(2))
    treeUser1.remove(AccessPath(2,0))
    Thread.sleep(1000)
    checkBothTrees(treeUser1, treeUser2, ValueTreeNode(1, List(ValueTreeNode(10), ValueTreeNode(11))))

    //effect independence
    treeUser1.insert(110, AccessPath(0,0))
    treeUser2.insert(220, AccessPath(1,0))
    Thread.sleep(1000)
    checkBothTrees(treeUser1, treeUser2, ValueTreeNode(1, List(ValueTreeNode(10, List(ValueTreeNode(110))), ValueTreeNode(11, List(ValueTreeNode(220))))))

    treeUser2.remove(AccessPath(0,0))
    treeUser1.insert(13, AccessPath(1))
    Thread.sleep(1000)
    checkBothTrees(treeUser1, treeUser2, ValueTreeNode(1, List(ValueTreeNode(10),ValueTreeNode(13), ValueTreeNode(11, List(ValueTreeNode(220))))))

    treeUser1.remove(AccessPath(2,0))
    treeUser2.insert(23, AccessPath(0))
    Thread.sleep(1000)
    checkBothTrees(treeUser1, treeUser2, ValueTreeNode(1, List(ValueTreeNode(23), ValueTreeNode(10),ValueTreeNode(13), ValueTreeNode(11))))
  }

  def checkBothTrees(treeUser1: FormicIntegerTree, treeUser2: FormicIntegerTree, expected: TreeNode): Any = {
    val user1Tree = treeUser1.getTree()
    val user2Tree = treeUser2.getTree()
    val user1Result = Await.result(user1Tree, 3.seconds)
    val user2Result = Await.result(user2Tree, 3.seconds)
    user1Result should equal(expected)
    user2Result should equal(expected)
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
