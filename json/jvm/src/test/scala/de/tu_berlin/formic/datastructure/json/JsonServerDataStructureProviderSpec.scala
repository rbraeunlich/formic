package de.tu_berlin.formic.datastructure.json

import akka.actor.ActorSystem
import akka.testkit.TestKit
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import de.tu_berlin.formic.datastructure.json.JsonFormicJsonDataStructureProtocol._

/**
  * @author Ronny Bräunlich
  */
class JsonServerDataStructureProviderSpec extends TestKit(ActorSystem("JsonServerDataStructureProviderSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "The JsonServerDataStructureProvider" must {
    "create a factory actor for every list type" in {
      val provider = JsonServerDataStructureProvider()
      val factoryMap = provider.initFactories(system)

      factoryMap.keySet should contain(JsonServerDataStructureFactory.name)

      val actorPaths = factoryMap.values.map(ref => ref.path.name.toString)
      actorPaths should contain(JsonServerDataStructureFactory.name.name)
    }

    "register a FormicJsonDataStructureProtocols for each list type" in {
      val protocol = new FormicJsonProtocol
      val provider = JsonServerDataStructureProvider()

      provider.registerFormicJsonDataStructureProtocols(protocol)

      val registered = protocol.dataTypeOperationJsonProtocols

      registered should contain(JsonServerDataStructureFactory.name -> new JsonFormicJsonDataStructureProtocol(JsonServerDataStructureFactory.name))
    }
  }
}
