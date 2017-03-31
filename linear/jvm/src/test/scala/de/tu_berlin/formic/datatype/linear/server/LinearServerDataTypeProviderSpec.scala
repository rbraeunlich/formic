package de.tu_berlin.formic.datatype.linear.server

import akka.actor.ActorSystem
import akka.testkit.TestKit
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datatype.linear.LinearFormicJsonDataTypeProtocol
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class LinearServerDataTypeProviderSpec extends TestKit(ActorSystem("LinearServerDataTypeProviderSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "The LinearServerDataTypeProvider" must {
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
        BooleanListDataStructureFactory.name -> new LinearFormicJsonDataTypeProtocol[Boolean](BooleanListDataStructureFactory.name),
        IntegerListDataStructureFactory.name -> new LinearFormicJsonDataTypeProtocol[Int](IntegerListDataStructureFactory.name),
        DoubleListDataStructureFactory.name -> new LinearFormicJsonDataTypeProtocol[Double](DoubleListDataStructureFactory.name),
        StringDataStructureFactory.name -> new LinearFormicJsonDataTypeProtocol[Char](StringDataStructureFactory.name)
        )

    }
  }
}
