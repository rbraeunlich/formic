package de.tu_berlin.formic.datastructure.linear.server

import akka.actor.ActorSystem
import akka.testkit.TestKit
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datastructure.linear.LinearFormicJsonDataStructureProtocol
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class LinearServerDataStructureProviderSpec extends TestKit(ActorSystem("LinearServerDataStructureProviderSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "The LinearServerDataStructureProvider" must {
    "create a factory actor for every list type" in {
      val provider = LinearServerDataStructureProvider()
      val factoryMap = provider.initFactories(system)

      factoryMap.keySet should contain allOf(
        BooleanListDataStructureFactory.name,
        IntegerListDataStructureFactory.name,
        DoubleListDataStructureFactory.name,
        StringDataStructureFactory.name)

      val actorPaths = factoryMap.values.map(ref => ref.path.name.toString)
      actorPaths should contain allOf(
        BooleanListDataStructureFactory.name.name,
        IntegerListDataStructureFactory.name.name,
        DoubleListDataStructureFactory.name.name,
        StringDataStructureFactory.name.name
        )
    }

    "register a FormicJsonDataTypeProtocols for each list type" in {
      val protocol = new FormicJsonProtocol
      val provider = LinearServerDataStructureProvider()

      provider.registerFormicJsonDataStructureProtocols(protocol)

      val registered = protocol.dataTypeOperationJsonProtocols

      registered should contain allOf(
        BooleanListDataStructureFactory.name -> new LinearFormicJsonDataStructureProtocol[Boolean](BooleanListDataStructureFactory.name),
        IntegerListDataStructureFactory.name -> new LinearFormicJsonDataStructureProtocol[Int](IntegerListDataStructureFactory.name),
        DoubleListDataStructureFactory.name -> new LinearFormicJsonDataStructureProtocol[Double](DoubleListDataStructureFactory.name),
        StringDataStructureFactory.name -> new LinearFormicJsonDataStructureProtocol[Char](StringDataStructureFactory.name)
        )

    }
  }
}
