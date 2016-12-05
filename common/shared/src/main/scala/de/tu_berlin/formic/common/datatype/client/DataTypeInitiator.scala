package de.tu_berlin.formic.common.datatype.client

import de.tu_berlin.formic.common.datatype.FormicDataType

/**
  * @author Ronny Bräunlich
  */
trait DataTypeInitiator {

  def initDataType(dataType: FormicDataType)

}
