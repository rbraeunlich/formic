package de.tu_berlin.formic.common.json

import de.tu_berlin.formic.common.datastructure.{DataStructureName, DataStructureOperation}

/**
  * Only the concrete data structures can know about the structure of their operations. Therefore,
  * they are responsible to provide a FormicJsonDataStructureProtocol that implements those methods.

  * @author Ronny Bräunlich
  */
trait FormicJsonDataStructureProtocol {

  def deserializeOperation(json: String): DataStructureOperation

  def serializeOperation(op: DataStructureOperation): String

  val name: DataStructureName

}
