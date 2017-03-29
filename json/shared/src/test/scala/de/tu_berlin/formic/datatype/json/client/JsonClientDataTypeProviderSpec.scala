package de.tu_berlin.formic.datatype.json.client

import akka.actor.ActorSystem
import akka.testkit.TestKit
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataTypeProtocol
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import de.tu_berlin.formic.datatype.json.JsonFormicJsonDataTypeProtocol._

/**
  * @author Ronny Bräunlich
  */
class JsonClientDataTypeProviderSpec extends TestKit(ActorSystem("JsonClientDataTypeProviderSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "The JsonClientDataTypeProviderSpec" must {
    "create a factory actor for every list type" in {
      val provider = JsonClientDataStructureProvider()
      val factoryMap = provider.initFactories(system)

      factoryMap.keySet should contain(FormicJsonObjectFactory.name)

      val actorPaths = factoryMap.values.map(ref => ref.path.name.toString)
      actorPaths should contain(FormicJsonObjectFactory.name.name)
    }

    "register a FormicJsonDataTypeProtocols for each list type" in {
      val protocol = new FormicJsonProtocol
      val provider = JsonClientDataStructureProvider()

      provider.registerFormicJsonDataStructureProtocols(protocol)

      val registered = protocol.dataTypeOperationJsonProtocols

      registered should contain(FormicJsonObjectFactory.name -> new JsonFormicJsonDataTypeProtocol(FormicJsonObjectFactory.name))
    }
  }
}
