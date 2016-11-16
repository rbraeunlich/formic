package de.tu_berlin.formic.common.json

import de.tu_berlin.formic.common.datatype.{DataTypeName, DataTypeOperation}

/**
  * @author Ronny Bräunlich
  */
trait FormicJsonDataTypeProtocol {

  def deserializeOperation(json: String): DataTypeOperation

  val name: DataTypeName

  def serializeOperation(op: DataTypeOperation): String
}
