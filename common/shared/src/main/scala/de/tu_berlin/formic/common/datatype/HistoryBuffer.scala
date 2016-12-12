package de.tu_berlin.formic.common.datatype

import de.tu_berlin.formic.common.OperationId

/**
  * A simple buffer that contains the history of a data type.
  *
  * @author Ronny Bräunlich
  */
class HistoryBuffer(private var privateHistory: List[DataTypeOperation] = List.empty) {

  def history = privateHistory

  /**
    * Returns the operation that has the given id. If no operation with that
    * id could be found, the optional is empty.
    *
    * @param id the operation id
    * @return
    */
  def findOperation(id: OperationId): Option[DataTypeOperation] = {
    history.find(op => op.id == id)
  }

  /**
    * Returns all operations that happened after the given operation, excluding it.
    *
    * @param id The id of the last known operation
    * @return A list containing all the operations that took place after the given one in descending
    *         order, i.e. the first operation in the list is the newest one. The list might be empty.
    */
  def findAllOperationsAfter(id: OperationId): List[DataTypeOperation] = {
    if (id == null) history
    else {
      val index = history.map(op => op.id).indexOf(id)
      history.slice(0, index)
    }
  }

  /**
    * Adds the operation to the history.
    *
    * @param op the lastest operation
    */
  def addOperation(op: DataTypeOperation) = privateHistory = op :: privateHistory
}
