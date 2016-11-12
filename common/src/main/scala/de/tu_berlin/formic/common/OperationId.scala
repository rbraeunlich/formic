package de.tu_berlin.formic.common

case class OperationId private (id: String) extends Id

/**
  * @author Ronny Bräunlich
  */
object OperationId {

  def apply(): OperationId = {
    OperationId(java.util.UUID.randomUUID.toString)
  }

  def valueOf(s:String): OperationId = {
    OperationId(s)
  }
}
