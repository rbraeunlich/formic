package de.tu_berlin.formic.datatype.tree.client

import akka.actor.ActorSystem
import akka.testkit.TestKit
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datatype.tree.TreeFormicJsonDataTypeProtocol
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class TreeClientDataTypeProviderSpec extends TestKit(ActorSystem("TreeClientDataTypeProviderSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "The TreeClientDataTypeProvider" must {
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
        FormicBooleanTreeFactory.name -> new TreeFormicJsonDataTypeProtocol[Boolean](FormicBooleanTreeFactory.name),
        FormicIntegerTreeFactory.name -> new TreeFormicJsonDataTypeProtocol[Int](FormicIntegerTreeFactory.name),
        FormicDoubleTreeFactory.name -> new TreeFormicJsonDataTypeProtocol[Double](FormicDoubleTreeFactory.name),
        FormicStringTreeFactory.name -> new TreeFormicJsonDataTypeProtocol[Char](FormicStringTreeFactory.name)
        )

    }
  }
}
