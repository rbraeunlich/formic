package de.tu_berlin.formic.datatype.linear.client

import akka.actor.ActorSystem
import akka.testkit.TestKit
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datatype.linear.LinearFormicJsonDataTypeProtocol
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class LinearClientDataTypeProviderSpec extends TestKit(ActorSystem("LinearClientDataTypeProviderSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "The LinearClientDataTypeProvider" must {
    "create a factory actor for every list type" in {
      val provider = LinearClientDataTypeProvider()
      val factoryMap = provider.initFactories(system)

      factoryMap.keySet should contain allOf(
        FormicBooleanListDataTypeFactory.name,
        FormicIntegerListDataTypeFactory.name,
        FormicDoubleListDataTypeFactory.name,
        FormicStringDataTypeFactory.name)

      val actorPaths = factoryMap.values.map(ref => ref.path.name.toString)
      actorPaths should contain allOf(
        FormicBooleanListDataTypeFactory.name.name,
        FormicIntegerListDataTypeFactory.name.name,
        FormicDoubleListDataTypeFactory.name.name,
        FormicStringDataTypeFactory.name.name
        )
    }

    "register a FormicJsonDataTypeProtocols for each list type" in {
      val protocol = new FormicJsonProtocol
      val provider = LinearClientDataTypeProvider()

      provider.registerFormicJsonDataTypeProtocols(protocol)

      val registered = protocol.dataTypeOperationJsonProtocols

      registered should contain allOf(
        FormicBooleanListDataTypeFactory.name -> new LinearFormicJsonDataTypeProtocol[Boolean](FormicBooleanListDataTypeFactory.name),
        FormicIntegerListDataTypeFactory.name -> new LinearFormicJsonDataTypeProtocol[Int](FormicIntegerListDataTypeFactory.name),
        FormicDoubleListDataTypeFactory.name -> new LinearFormicJsonDataTypeProtocol[Double](FormicDoubleListDataTypeFactory.name),
        FormicStringDataTypeFactory.name -> new LinearFormicJsonDataTypeProtocol[Char](FormicStringDataTypeFactory.name)
        )

    }
  }
}
