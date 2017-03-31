package de.tu_berlin.formic.datatype.tree

import akka.actor.ActorSystem
import akka.testkit.TestKit
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class TreeServerDataTypeProviderSpec extends TestKit(ActorSystem("TreeServerDataTypeProviderSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "The TreeServerDataTypeProvider" must {
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

    "register a FormicJsonDataTypeProtocols for each list type" in {
      val protocol = new FormicJsonProtocol
      val provider = TreeServerDataStructureProvider()

      provider.registerFormicJsonDataStructureProtocols(protocol)

      val registered = protocol.dataTypeOperationJsonProtocols

      registered should contain allOf(
        BooleanTreeDataStructureFactory.name -> new TreeFormicJsonDataTypeProtocol[Boolean](BooleanTreeDataStructureFactory.name),
        IntegerTreeDataStructureFactory.name -> new TreeFormicJsonDataTypeProtocol[Int](IntegerTreeDataStructureFactory.name),
        DoubleTreeDataStructureFactory.name -> new TreeFormicJsonDataTypeProtocol[Double](DoubleTreeDataStructureFactory.name),
        StringTreeDataStructureFactory.name -> new TreeFormicJsonDataTypeProtocol[Char](StringTreeDataStructureFactory.name)
        )

    }
  }
}
