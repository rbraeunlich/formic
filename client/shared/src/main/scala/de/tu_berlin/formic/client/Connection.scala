package de.tu_berlin.formic.client

import de.tu_berlin.formic.common.message.FormicMessage

/**
  * @author Ronny Bräunlich
  */
trait Connection {

  def onConnect()

  def onError(errorMessage: String)

  def onMessage(formicMessage: FormicMessage)
}
