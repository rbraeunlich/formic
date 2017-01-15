package de.tu_berlin.formic.datatype.tree

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.datatype.{DataTypeName, ServerDataTypeProvider}
import de.tu_berlin.formic.common.json.FormicJsonProtocol

/**
  * @author Ronny Bräunlich
  */
class TreeServerDataTypeProvider extends ServerDataTypeProvider {

  override def initFactories(actorSystem: ActorSystem): Map[DataTypeName, ActorRef] = {
    var factories: Map[DataTypeName, ActorRef] = Map.empty
    val booleanTreeFactory = actorSystem.actorOf(Props[BooleanTreeDataTypeFactory], BooleanTreeDataTypeFactory.name.name)
    val doubleTreeFactory = actorSystem.actorOf(Props[DoubleTreeDataTypeFactory], DoubleTreeDataTypeFactory.name.name)
    val integerTreeFactory = actorSystem.actorOf(Props[IntegerTreeDataTypeFactory], IntegerTreeDataTypeFactory.name.name)
    val stringTreeFactory = actorSystem.actorOf(Props[StringTreeDataTypeFactory], StringTreeDataTypeFactory.name.name)
    factories += (BooleanTreeDataTypeFactory.name -> booleanTreeFactory)
    factories += (DoubleTreeDataTypeFactory.name -> doubleTreeFactory)
    factories += (IntegerTreeDataTypeFactory.name -> integerTreeFactory)
    factories += (StringTreeDataTypeFactory.name -> stringTreeFactory)

    factories
  }

  override def registerFormicJsonDataTypeProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[Boolean](BooleanTreeDataTypeFactory.name))
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[Double](DoubleTreeDataTypeFactory.name))
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[Int](IntegerTreeDataTypeFactory.name))
    formicJsonProtocol.registerProtocol(new TreeFormicJsonDataTypeProtocol[String](StringTreeDataTypeFactory.name))
  }
}

object TreeServerDataTypeProvider {
  def apply(): TreeServerDataTypeProvider = new TreeServerDataTypeProvider()
}