package de.tu_berlin.formic.datatype.json

import akka.actor.ActorSystem
import akka.testkit.TestKit
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataTypeProtocol._

/**
  * @author Ronny Bräunlich
  */
class JsonServerDataTypeProviderSpec extends TestKit(ActorSystem("JsonServerDataTypeProviderSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "The JsonServerDataTypeProviderSpec" must {
    "create a factory actor for every list type" in {
      val provider = JsonServerDataStructureProvider()
      val factoryMap = provider.initFactories(system)

      factoryMap.keySet should contain(JsonServerDataStructureFactory.name)

      val actorPaths = factoryMap.values.map(ref => ref.path.name.toString)
      actorPaths should contain(JsonServerDataStructureFactory.name.name)
    }

    "register a FormicJsonDataTypeProtocols for each list type" in {
      val protocol = new FormicJsonProtocol
      val provider = JsonServerDataStructureProvider()

      provider.registerFormicJsonDataStructureProtocols(protocol)

      val registered = protocol.dataTypeOperationJsonProtocols

      registered should contain(JsonServerDataStructureFactory.name -> new JsonFormicJsonDataTypeProtocol(JsonServerDataStructureFactory.name))
    }
  }
}
