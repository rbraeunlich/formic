package de.tu_berlin.formic.common

case class OperationId private(id: String) extends Id

/**
  * @author Ronny Bräunlich
  */
object OperationId {

  def apply(): OperationId = new OperationId(java.util.UUID.randomUUID.toString)

  def valueOf(s: String): OperationId = new OperationId(s)
}
