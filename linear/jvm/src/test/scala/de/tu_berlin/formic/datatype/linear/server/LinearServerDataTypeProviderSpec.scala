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
        BooleanListDataTypeFactory.name,
        IntegerListDataTypeFactory.name,
        DoubleListDataTypeFactory.name,
        StringDataTypeFactory.name)

      val actorPaths = factoryMap.values.map(ref => ref.path.name.toString)
      actorPaths should contain allOf(
        BooleanListDataTypeFactory.name.name,
        IntegerListDataTypeFactory.name.name,
        DoubleListDataTypeFactory.name.name,
        StringDataTypeFactory.name.name
        )
    }

    "register a FormicJsonDataTypeProtocols for each list type" in {
      val protocol = new FormicJsonProtocol
      val provider = LinearServerDataStructureProvider()

      provider.registerFormicJsonDataStructureProtocols(protocol)

      val registered = protocol.dataTypeOperationJsonProtocols

      registered should contain allOf(
        BooleanListDataTypeFactory.name -> new LinearFormicJsonDataTypeProtocol[Boolean](BooleanListDataTypeFactory.name),
        IntegerListDataTypeFactory.name -> new LinearFormicJsonDataTypeProtocol[Int](IntegerListDataTypeFactory.name),
        DoubleListDataTypeFactory.name -> new LinearFormicJsonDataTypeProtocol[Double](DoubleListDataTypeFactory.name),
        StringDataTypeFactory.name -> new LinearFormicJsonDataTypeProtocol[Char](StringDataTypeFactory.name)
        )

    }
  }
}
