package de.tu_berlin.formic.datatype.linear.server

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.datatype.{DataStructureName, ServerDataStructureProvider}
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datatype.linear.LinearFormicJsonDataTypeProtocol

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
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Boolean](BooleanListDataStructureFactory.name))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Double](DoubleListDataStructureFactory.name))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Int](IntegerListDataStructureFactory.name))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Char](StringDataStructureFactory.name))
  }
}

object LinearServerDataStructureProvider {
  def apply(): LinearServerDataStructureProvider = new LinearServerDataStructureProvider()
}
