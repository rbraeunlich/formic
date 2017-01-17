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
      val provider = TreeServerDataTypeProvider()
      val factoryMap = provider.initFactories(system)

      factoryMap.keySet should contain allOf(
        BooleanTreeDataTypeFactory.name,
        IntegerTreeDataTypeFactory.name,
        DoubleTreeDataTypeFactory.name,
        StringTreeDataTypeFactory.name)

      val actorPaths = factoryMap.values.map(ref => ref.path.name.toString)
      actorPaths should contain allOf(
        BooleanTreeDataTypeFactory.name.name,
        IntegerTreeDataTypeFactory.name.name,
        DoubleTreeDataTypeFactory.name.name,
        StringTreeDataTypeFactory.name.name
        )
    }

    "register a FormicJsonDataTypeProtocols for each list type" in {
      val protocol = new FormicJsonProtocol
      val provider = TreeServerDataTypeProvider()

      provider.registerFormicJsonDataTypeProtocols(protocol)

      val registered = protocol.dataTypeOperationJsonProtocols

      registered should contain allOf(
        BooleanTreeDataTypeFactory.name -> new TreeFormicJsonDataTypeProtocol[Boolean](BooleanTreeDataTypeFactory.name),
        IntegerTreeDataTypeFactory.name -> new TreeFormicJsonDataTypeProtocol[Int](IntegerTreeDataTypeFactory.name),
        DoubleTreeDataTypeFactory.name -> new TreeFormicJsonDataTypeProtocol[Double](DoubleTreeDataTypeFactory.name),
        StringTreeDataTypeFactory.name -> new TreeFormicJsonDataTypeProtocol[Char](StringTreeDataTypeFactory.name)
        )

    }
  }
}
