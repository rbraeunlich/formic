package de.tu_berlin.formic.common

/**
  * @author Ronny Bräunlich
  */
case class DataTypeInstanceId private(id: String) extends Id

object DataTypeInstanceId {

  def apply(): DataTypeInstanceId = new DataTypeInstanceId(java.util.UUID.randomUUID.toString)

  def valueOf(s: String): DataTypeInstanceId = new DataTypeInstanceId(s)
}
