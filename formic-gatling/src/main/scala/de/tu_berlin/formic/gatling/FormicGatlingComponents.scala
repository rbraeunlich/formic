package de.tu_berlin.formic.gatling

import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session.Session

/**
  * @author Ronny Bräunlich
  */
case class FormicGatlingComponents(formicGatlingProtocol: FormicGatlingProtocol) extends ProtocolComponents{

  override def onStart: Option[(Session) => Session] = None

  override def onExit: Option[(Session) => Unit] = None
}
