package de.tu_berlin.formic.common

/**
  * @author Ronny Bräunlich
  */
case class ClientId private (id: String) extends Id

object ClientId {

  def apply(): ClientId = {
    ClientId(java.util.UUID.randomUUID.toString)
  }

  def valueOf(s : String): ClientId = {
    ClientId(s)
  }
}
