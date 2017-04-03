package de.tu_berlin.formic.datastructure.linear.server

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.datastructure.{DataStructureName, ServerDataStructureProvider}
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datastructure.linear.LinearFormicJsonDataStructureProtocol

/**
  * @author Ronny Bräunlich
  */
class LinearServerDataStructureProvider extends ServerDataStructureProvider {
  
  override def initFactories(actorSystem: ActorSystem): Map[DataStructureName, ActorRef] = {
    var factories: Map[DataStructureName, ActorRef] = Map.empty
    val booleanListFactory = actorSystem.actorOf(Props[BooleanListDataStructureFactory], BooleanListDataStructureFactory.name.name)
    val doubleListFactory = actorSystem.actorOf(Props[DoubleListDataStructureFactory], DoubleListDataStructureFactory.name.name)
    val integerListFactory = actorSystem.actorOf(Props[IntegerListDataStructureFactory], IntegerListDataStructureFactory.name.name)
    val stringFactory = actorSystem.actorOf(Props[StringDataStructureFactory], StringDataStructureFactory.name.name)

    factories += (BooleanListDataStructureFactory.name -> booleanListFactory)
    factories += (DoubleListDataStructureFactory.name -> doubleListFactory)
    factories += (IntegerListDataStructureFactory.name -> integerListFactory)
    factories += (StringDataStructureFactory.name -> stringFactory)

    factories
  }

  override def registerFormicJsonDataStructureProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataStructureProtocol[Boolean](BooleanListDataStructureFactory.name))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataStructureProtocol[Double](DoubleListDataStructureFactory.name))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataStructureProtocol[Int](IntegerListDataStructureFactory.name))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataStructureProtocol[Char](StringDataStructureFactory.name))
  }
}

object LinearServerDataStructureProvider {
  def apply(): LinearServerDataStructureProvider = new LinearServerDataStructureProvider()
}
