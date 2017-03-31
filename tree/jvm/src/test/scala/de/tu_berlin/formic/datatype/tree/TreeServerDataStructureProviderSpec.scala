package de.tu_berlin.formic.datatype.tree

import akka.actor.ActorSystem
import akka.testkit.TestKit
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class TreeServerDataStructureProviderSpec extends TestKit(ActorSystem("TreeServerDataStructureProviderSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "The TreeServerDataStructureProvider" must {
    "create a factory actor for every list type" in {
      val provider = TreeServerDataStructureProvider()
      val factoryMap = provider.initFactories(system)

      factoryMap.keySet should contain allOf(
        BooleanTreeDataStructureFactory.name,
        IntegerTreeDataStructureFactory.name,
        DoubleTreeDataStructureFactory.name,
        StringTreeDataStructureFactory.name)

      val actorPaths = factoryMap.values.map(ref => ref.path.name.toString)
      actorPaths should contain allOf(
        BooleanTreeDataStructureFactory.name.name,
        IntegerTreeDataStructureFactory.name.name,
        DoubleTreeDataStructureFactory.name.name,
        StringTreeDataStructureFactory.name.name
        )
    }

    "register a FormicJsonDataStructureProtocols for each list type" in {
      val protocol = new FormicJsonProtocol
      val provider = TreeServerDataStructureProvider()

      provider.registerFormicJsonDataStructureProtocols(protocol)

      val registered = protocol.dataTypeOperationJsonProtocols

      registered should contain allOf(
        BooleanTreeDataStructureFactory.name -> new TreeFormicJsonDataStructureProtocol[Boolean](BooleanTreeDataStructureFactory.name),
        IntegerTreeDataStructureFactory.name -> new TreeFormicJsonDataStructureProtocol[Int](IntegerTreeDataStructureFactory.name),
        DoubleTreeDataStructureFactory.name -> new TreeFormicJsonDataStructureProtocol[Double](DoubleTreeDataStructureFactory.name),
        StringTreeDataStructureFactory.name -> new TreeFormicJsonDataStructureProtocol[Char](StringTreeDataStructureFactory.name)
        )

    }
  }
}
