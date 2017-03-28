package de.tu_berlin.formic.common

/**
  * @author Ronny Bräunlich
  */
case class DataStructureInstanceId private(id: String) extends Id

object DataStructureInstanceId {

  def apply(): DataStructureInstanceId = new DataStructureInstanceId(java.util.UUID.randomUUID.toString)

  def valueOf(s: String): DataStructureInstanceId = new DataStructureInstanceId(s)
}
