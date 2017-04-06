package de.tu_berlin.formic.datastructure.linear.client

import akka.actor.ActorSystem
import akka.testkit.TestKit
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datastructure.linear.LinearFormicJsonDataStructureProtocol
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class LinearClientDataStructureProviderSpec extends TestKit(ActorSystem("LinearClientDataStructureProviderSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "The LinearClientDataStructureProvider" must {
    "create a factory actor for every list type" in {
      val provider = LinearClientDataStructureProvider()
      val factoryMap = provider.initFactories(system)

      factoryMap.keySet should contain allOf(
        FormicBooleanListDataStructureFactory.name,
        FormicIntegerListDataStructureFactory.name,
        FormicDoubleListDataStructureFactory.name,
        FormicStringDataStructureFactory.name)

      val actorPaths = factoryMap.values.map(ref => ref.path.name.toString)
      actorPaths should contain allOf(
        FormicBooleanListDataStructureFactory.name.name,
        FormicIntegerListDataStructureFactory.name.name,
        FormicDoubleListDataStructureFactory.name.name,
        FormicStringDataStructureFactory.name.name
        )
    }

    "register a FormicJsonDataTypeProtocols for each list type" in {
      val protocol = new FormicJsonProtocol
      val provider = LinearClientDataStructureProvider()

      provider.registerFormicJsonDataStructureProtocols(protocol)

      val registered = protocol.dataStructureOperationJsonProtocols

      registered should contain allOf(
        FormicBooleanListDataStructureFactory.name -> new LinearFormicJsonDataStructureProtocol[Boolean](FormicBooleanListDataStructureFactory.name),
        FormicIntegerListDataStructureFactory.name -> new LinearFormicJsonDataStructureProtocol[Int](FormicIntegerListDataStructureFactory.name),
        FormicDoubleListDataStructureFactory.name -> new LinearFormicJsonDataStructureProtocol[Double](FormicDoubleListDataStructureFactory.name),
        FormicStringDataStructureFactory.name -> new LinearFormicJsonDataStructureProtocol[Char](FormicStringDataStructureFactory.name)
        )

    }
  }
}
