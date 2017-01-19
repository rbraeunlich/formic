package de.tu_berlin.formic.gatling

import akka.actor.ActorRef
import com.sun.xml.internal.bind.marshaller.DataWriter
import de.tu_berlin.formic.client.FormicSystem
import de.tu_berlin.formic.datatype.linear.client.FormicString
import io.gatling.commons.util.TimeHelper
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.Session

/**
  * @author Ronny Bräunlich
  */
class CreateDataType(formicSystem: FormicSystem, val next: Action) extends ChainableAction {

  override def name: String = "CreateDataType action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val string = new FormicString(() => {}, formicSystem)
    val end = TimeHelper.nowMillis
    session.set("formicString", string)
    next ! session
  }
}

class LinearInsertion(val next: ActorRef) {

}

class LinearDeletion(val next: ActorRef) {

}
