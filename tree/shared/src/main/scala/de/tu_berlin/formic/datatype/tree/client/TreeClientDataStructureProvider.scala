package de.tu_berlin.formic.datatype.tree.client

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.datastructure.{ClientDataStructureProvider, DataStructureName}
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datatype.tree.TreeFormicJsonDataStructureProtocol

/**
  * @author Ronny Bräunlich
  */
class TreeClientDataStructureProvider extends ClientDataStructureProvider {

  override def initFactories(actorSystem: ActorSystem): Map[DataStructureName, ActorRef] = {
    var factories: Map[DataStructureName, ActorRef] = Map.empty
    val booleanTreeFactory = actorSystem.actorOf(Props(new FormicBooleanTreeFactory), FormicBooleanTreeFactory.name.name)
    val doubleTreeFactory = actorSystem.actorOf(Props(new FormicDoubleTreeFactory), FormicDoubleTreeFactory.name.name)
    val integerTreeFactory = actorSystem.actorOf(Props(new FormicIntegerTreeFactory), FormicIntegerTreeFactory.name.name)
    val stringTreeFactory = actorSystem.actorOf(Props(new FormicStringTreeFactory), FormicStringTreeFactory.name.name)
    factories += (FormicBooleanTreeFactory.name -> booleanTreeFactory)
    factories += (FormicDoubleTreeFactory.name -> doubleTreeFactory)
    factories += (FormicIntegerTreeFactory.name -> integerTreeFactory)
    factories += (FormicStringTreeFactory.name -> stringTreeFactory)

    factories
  }

  override def registerFormicJsonDataStructureProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataStructureProtocol[Boolean](FormicBooleanTreeFactory.name))
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataStructureProtocol[Double](FormicDoubleTreeFactory.name))
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataStructureProtocol[Int](FormicIntegerTreeFactory.name))
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataStructureProtocol[String](FormicStringTreeFactory.name))
  }
}

object TreeClientDataStructureProvider {
  def apply(): TreeClientDataStructureProvider = new TreeClientDataStructureProvider()
}