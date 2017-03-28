package de.tu_berlin.formic.datatype.tree.client

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.datatype.{ClientDataTypeProvider, DataStructureName}
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datatype.tree.TreeFormicJsonDataTypeProtocol

/**
  * @author Ronny Bräunlich
  */
class TreeClientDataTypeProvider extends ClientDataTypeProvider {

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

  override def registerFormicJsonDataTypeProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[Boolean](FormicBooleanTreeFactory.name))
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[Double](FormicDoubleTreeFactory.name))
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[Int](FormicIntegerTreeFactory.name))
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[String](FormicStringTreeFactory.name))
  }
}

object TreeClientDataTypeProvider {
  def apply(): TreeClientDataTypeProvider = new TreeClientDataTypeProvider()
}