package de.tu_berlin.formic.client

import de.tu_berlin.formic.common.datatype.DataTypeName

/**
  * @author Ronny Bräunlich
  */
trait FormicDataType {

  val dataTypeName: DataTypeName

  var callback: () => Unit = _

}
