package de.tu_berlin.formic.common.json

import de.tu_berlin.formic.common.datatype.{DataStructureName, DataTypeOperation}

/**
  * Only the concrete data types can know about the structure of their operations. Therefore,
  * they are responsible to provide a FormicJsonDataTypeProtocol that implements those methods.

  * @author Ronny Bräunlich
  */
trait FormicJsonDataTypeProtocol {

  def deserializeOperation(json: String): DataTypeOperation

  def serializeOperation(op: DataTypeOperation): String

  val name: DataStructureName

}
