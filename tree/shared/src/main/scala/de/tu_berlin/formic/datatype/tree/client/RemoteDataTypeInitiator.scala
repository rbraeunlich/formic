package de.tu_berlin.formic.datatype.tree.client

import de.tu_berlin.formic.common.datatype.FormicDataType
import de.tu_berlin.formic.common.datatype.client.DataTypeInitiator

/**
  * Data types that are instantiated because they were created remote (i.e. not by calling new whatever
  * locally), do not need the usual instantiator, since the Actor they wrap has already been created.
  * Therefore, this empty initiator can be used by the factories.
  *
  * @author Ronny Bräunlich
  */
object RemoteDataTypeInitiator extends DataTypeInitiator {
  override def initDataType(dataType: FormicDataType): Unit = {}
}
