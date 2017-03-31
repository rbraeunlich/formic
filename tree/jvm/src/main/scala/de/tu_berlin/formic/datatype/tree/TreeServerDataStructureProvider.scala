package de.tu_berlin.formic.datatype.tree

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.datatype.{DataStructureName, ServerDataStructureProvider}
import de.tu_berlin.formic.common.json.FormicJsonProtocol

/**
  * @author Ronny Bräunlich
  */
class TreeServerDataStructureProvider extends ServerDataStructureProvider {

  override def initFactories(actorSystem: ActorSystem): Map[DataStructureName, ActorRef] = {
    var factories: Map[DataStructureName, ActorRef] = Map.empty
    val booleanTreeFactory = actorSystem.actorOf(Props[BooleanTreeDataStructureFactory], BooleanTreeDataStructureFactory.name.name)
    val doubleTreeFactory = actorSystem.actorOf(Props[DoubleTreeDataStructureFactory], DoubleTreeDataStructureFactory.name.name)
    val integerTreeFactory = actorSystem.actorOf(Props[IntegerTreeDataStructureFactory], IntegerTreeDataStructureFactory.name.name)
    val stringTreeFactory = actorSystem.actorOf(Props[StringTreeDataStructureFactory], StringTreeDataStructureFactory.name.name)
    factories += (BooleanTreeDataStructureFactory.name -> booleanTreeFactory)
    factories += (DoubleTreeDataStructureFactory.name -> doubleTreeFactory)
    factories += (IntegerTreeDataStructureFactory.name -> integerTreeFactory)
    factories += (StringTreeDataStructureFactory.name -> stringTreeFactory)

    factories
  }

  override def registerFormicJsonDataStructureProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[Boolean](BooleanTreeDataStructureFactory.name))
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[Double](DoubleTreeDataStructureFactory.name))
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[Int](IntegerTreeDataStructureFactory.name))
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[String](StringTreeDataStructureFactory.name))
  }
}

object TreeServerDataStructureProvider {
  def apply(): TreeServerDataStructureProvider = new TreeServerDataStructureProvider()
}