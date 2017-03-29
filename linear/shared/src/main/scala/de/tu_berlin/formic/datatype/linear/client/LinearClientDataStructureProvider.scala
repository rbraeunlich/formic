package de.tu_berlin.formic.datatype.linear.client

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.datatype.{ClientDataStructureProvider, DataStructureName}
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datatype.linear.LinearFormicJsonDataTypeProtocol

/**
  * @author Ronny Bräunlich
  */
class LinearClientDataStructureProvider extends ClientDataStructureProvider {

  override def initFactories(actorSystem: ActorSystem): Map[DataStructureName, ActorRef] = {
    var factories: Map[DataStructureName, ActorRef] = Map.empty
    val formicBooleanListFactory = actorSystem.actorOf(Props(new FormicBooleanListDataTypeFactory), FormicBooleanListDataTypeFactory.name.name)
    val formicDoubleListFactory = actorSystem.actorOf(Props(new FormicDoubleListDataTypeFactory), FormicDoubleListDataTypeFactory.name.name)
    val formicIntegerListFactory = actorSystem.actorOf(Props(new FormicIntegerListDataTypeFactory), FormicIntegerListDataTypeFactory.name.name)
    val formicStringFactory = actorSystem.actorOf(Props(new FormicStringDataTypeFactory), FormicStringDataTypeFactory.name.name)
    factories += (FormicBooleanListDataTypeFactory.name -> formicBooleanListFactory)
    factories += (FormicDoubleListDataTypeFactory.name -> formicDoubleListFactory)
    factories += (FormicIntegerListDataTypeFactory.name -> formicIntegerListFactory)
    factories += (FormicStringDataTypeFactory.name -> formicStringFactory)

    factories
  }

  override def registerFormicJsonDataStructureProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Boolean](FormicBooleanListDataTypeFactory.name))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Double](FormicDoubleListDataTypeFactory.name))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Int](FormicIntegerListDataTypeFactory.name))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Char](FormicStringDataTypeFactory.name))
  }
}

object LinearClientDataStructureProvider {
  def apply(): LinearClientDataStructureProvider = new LinearClientDataStructureProvider()
}