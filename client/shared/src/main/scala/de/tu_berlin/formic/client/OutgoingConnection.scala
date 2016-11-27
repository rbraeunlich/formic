package de.tu_berlin.formic.client

import de.tu_berlin.formic.common.datatype.DataTypeOperation
import de.tu_berlin.formic.common.message.FormicMessage
/**
  * @author Ronny Bräunlich
  */
trait OutgoingConnection {

  def send(formicMessage: FormicMessage)

}
