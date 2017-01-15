package de.tu_berlin.formic.datatype.linear.client

import akka.actor.{ActorRef, ActorSystem, Props}
import de.tu_berlin.formic.common.datatype.{ClientDataTypeProvider, DataTypeName}
import de.tu_berlin.formic.common.json.FormicJsonProtocol
import de.tu_berlin.formic.datatype.linear.LinearFormicJsonDataTypeProtocol

/**
  * @author Ronny Bräunlich
  */
class LinearClientDataTypeProvider extends ClientDataTypeProvider {

  override def initFactories(actorSystem: ActorSystem): Map[DataTypeName, ActorRef] = {
    var factories: Map[DataTypeName, ActorRef] = Map.empty
    val formicBooleanListFactory = actorSystem.actorOf(Props(new FormicBooleanListDataTypeFactory), FormicBooleanListDataTypeFactory.dataTypeName.name)
    val formicDoubleListFactory = actorSystem.actorOf(Props(new FormicDoubleListDataTypeFactory), FormicDoubleListDataTypeFactory.dataTypeName.name)
    val formicIntegerListFactory = actorSystem.actorOf(Props(new FormicIntegerListDataTypeFactory), FormicIntegerListDataTypeFactory.dataTypeName.name)
    val formicStringFactory = actorSystem.actorOf(Props(new FormicStringDataTypeFactory), FormicStringDataTypeFactory.dataTypeName.name)
    factories += (FormicBooleanListDataTypeFactory.dataTypeName -> formicBooleanListFactory)
    factories += (FormicDoubleListDataTypeFactory.dataTypeName -> formicDoubleListFactory)
    factories += (FormicIntegerListDataTypeFactory.dataTypeName -> formicIntegerListFactory)
    factories += (FormicStringDataTypeFactory.dataTypeName -> formicStringFactory)

    factories
  }

  override def registerFormicJsonDataTypeProtocols(formicJsonProtocol: FormicJsonProtocol): Unit = {
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Boolean](FormicBooleanListDataTypeFactory.dataTypeName))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Double](FormicDoubleListDataTypeFactory.dataTypeName))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Int](FormicIntegerListDataTypeFactory.dataTypeName))
    formicJsonProtocol.registerProtocol(new LinearFormicJsonDataTypeProtocol[Char](FormicStringDataTypeFactory.dataTypeName))
  }
}

object LinearClientDataTypeProvider {
  def apply(): LinearClientDataTypeProvider = new LinearClientDataTypeProvider()
}