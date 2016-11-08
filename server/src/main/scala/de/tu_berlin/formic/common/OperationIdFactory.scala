package de.tu_berlin.formic.common

trait OperationId {
  val id:String
}

private case class OperationIdImpl (id: String) extends OperationId

/**
  * @author Ronny Bräunlich
  */
object OperationIdFactory {

  def apply(): OperationId = {
    return OperationIdImpl(java.util.UUID.randomUUID.toString)
  }
}
