package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorSystem
import akka.testkit.TestKit
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datatype.tree.TreeFormicJsonDataStructureProtocol
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class TreeClientDataStructureProviderSpec extends TestKit(ActorSystem("TreeClientDataStructureProviderSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "The TreeClientDataStructureProvider" must {
    "create a factory actor for every list type" in {
      val provider = TreeClientDataStructureProvider()
      val factoryMap = provider.initFactories(system)

      factoryMap.keySet should contain allOf(
        FormicBooleanTreeFactory.name,
        FormicIntegerTreeFactory.name,
        FormicDoubleTreeFactory.name,
        FormicStringTreeFactory.name)

      val actorPaths = factoryMap.values.map(ref => ref.path.name.toString)
      actorPaths should contain allOf(
        FormicBooleanTreeFactory.name.name,
        FormicIntegerTreeFactory.name.name,
        FormicDoubleTreeFactory.name.name,
        FormicStringTreeFactory.name.name
        )
    }

    "register a FormicJsonDataTypeProtocols for each list type" in {
      val protocol = new FormicJsonProtocol
      val provider = TreeClientDataStructureProvider()

      provider.registerFormicJsonDataStructureProtocols(protocol)

      val registered = protocol.dataTypeOperationJsonProtocols

      registered should contain allOf(
        FormicBooleanTreeFactory.name -> new TreeFormicJsonDataStructureProtocol[Boolean](FormicBooleanTreeFactory.name),
        FormicIntegerTreeFactory.name -> new TreeFormicJsonDataStructureProtocol[Int](FormicIntegerTreeFactory.name),
        FormicDoubleTreeFactory.name -> new TreeFormicJsonDataStructureProtocol[Double](FormicDoubleTreeFactory.name),
        FormicStringTreeFactory.name -> new TreeFormicJsonDataStructureProtocol[Char](FormicStringTreeFactory.name)
        )

    }
  }
}
