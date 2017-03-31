package de.tu_berlin.formic.datatype.linear.client

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.datatype.{ClientDataStructureProvider, DataStructureName}
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datatype.linear.LinearFormicJsonDataStructureProtocol

/**
  * @author Ronny Bräunlich
  */
class LinearClientDataStructureProvider extends ClientDataStructureProvider {

  override def initFactories(actorSystem: ActorSystem): Map[DataStructureName, ActorRef] = {
    var factories: Map[DataStructureName, ActorRef] = Map.empty
    val formicBooleanListFactory = actorSystem.actorOf(Props(new FormicBooleanListDataStructureFactory), FormicBooleanListDataStructureFactory.name.name)
    val formicDoubleListFactory = actorSystem.actorOf(Props(new FormicDoubleListDataStructureFactory), FormicDoubleListDataStructureFactory.name.name)
    val formicIntegerListFactory = actorSystem.actorOf(Props(new FormicIntegerListDataStructureFactory), FormicIntegerListDataStructureFactory.name.name)
    val formicStringFactory = actorSystem.actorOf(Props(new FormicStringDataStructureFactory), FormicStringDataStructureFactory.name.name)
    factories += (FormicBooleanListDataStructureFactory.name -> formicBooleanListFactory)
    factories += (FormicDoubleListDataStructureFactory.name -> formicDoubleListFactory)
    factories += (FormicIntegerListDataStructureFactory.name -> formicIntegerListFactory)
    factories += (FormicStringDataStructureFactory.name -> formicStringFactory)

    factories
  }

  override def registerFormicJsonDataStructureProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataStructureProtocol[Boolean](FormicBooleanListDataStructureFactory.name))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataStructureProtocol[Double](FormicDoubleListDataStructureFactory.name))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataStructureProtocol[Int](FormicIntegerListDataStructureFactory.name))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataStructureProtocol[Char](FormicStringDataStructureFactory.name))
  }
}

object LinearClientDataStructureProvider {
  def apply(): LinearClientDataStructureProvider = new LinearClientDataStructureProvider()
}