package de.tu_berlin.formic.common.datatype

import de.tu_berlin.formic.common.{ClientId, OperationId}

/**
  * @author Ronny Bräunlich
  */
trait DataStructureOperation {
  val id: OperationId
  val operationContext: OperationContext
  var clientId: ClientId
}
