package de.tu_berlin.formic.common

/**
  * @author Ronny Bräunlich
  */
case class ClientId private(id: String) extends Id

object ClientId {

  def apply(): ClientId = new ClientId(java.util.UUID.randomUUID.toString)

  def valueOf(s: String): ClientId = new ClientId(s)

}
