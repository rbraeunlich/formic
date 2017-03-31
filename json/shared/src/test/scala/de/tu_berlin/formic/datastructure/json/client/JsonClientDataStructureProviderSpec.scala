package de.tu_berlin.formic.datastructure.json.client

import akka.actor.ActorSystem
import akka.testkit.TestKit
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datastructure.json.JsonFormicJsonDataStructureProtocol
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import de.tu_berlin.formic.datastructure.json.JsonFormicJsonDataStructureProtocol._

/**
  * @author Ronny Bräunlich
  */
class JsonClientDataStructureProviderSpec extends TestKit(ActorSystem("JsonClientDataStructureProviderSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "The JsonClientDataStructureProviderSpec" must {
    "create a factory actor for every list type" in {
      val provider = JsonClientDataStructureProvider()
      val factoryMap = provider.initFactories(system)

      factoryMap.keySet should contain(FormicJsonObjectFactory.name)

      val actorPaths = factoryMap.values.map(ref => ref.path.name.toString)
      actorPaths should contain(FormicJsonObjectFactory.name.name)
    }

    "register a FormicJsonDataStructureProtocols for each list type" in {
      val protocol = new FormicJsonProtocol
      val provider = JsonClientDataStructureProvider()

      provider.registerFormicJsonDataStructureProtocols(protocol)

      val registered = protocol.dataTypeOperationJsonProtocols

      registered should contain(FormicJsonObjectFactory.name -> new JsonFormicJsonDataStructureProtocol(FormicJsonObjectFactory.name))
    }
  }
}
