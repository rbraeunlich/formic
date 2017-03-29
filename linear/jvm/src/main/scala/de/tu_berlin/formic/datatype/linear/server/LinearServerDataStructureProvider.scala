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
    val booleanListFactory = actorSystem.actorOf(Props[BooleanListDataTypeFactory], BooleanListDataTypeFactory.name.name)
    val doubleListFactory = actorSystem.actorOf(Props[DoubleListDataTypeFactory], DoubleListDataTypeFactory.name.name)
    val integerListFactory = actorSystem.actorOf(Props[IntegerListDataTypeFactory], IntegerListDataTypeFactory.name.name)
    val stringFactory = actorSystem.actorOf(Props[StringDataTypeFactory], StringDataTypeFactory.name.name)

    factories += (BooleanListDataTypeFactory.name -> booleanListFactory)
    factories += (DoubleListDataTypeFactory.name -> doubleListFactory)
    factories += (IntegerListDataTypeFactory.name -> integerListFactory)
    factories += (StringDataTypeFactory.name -> stringFactory)

    factories
  }

  override def registerFormicJsonDataStructureProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Boolean](BooleanListDataTypeFactory.name))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Double](DoubleListDataTypeFactory.name))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Int](IntegerListDataTypeFactory.name))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Char](StringDataTypeFactory.name))
  }
}

object LinearServerDataStructureProvider {
  def apply(): LinearServerDataStructureProvider = new LinearServerDataStructureProvider()
}