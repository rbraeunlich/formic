package de.tu_berlin.formic.datatype.json

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.controlalgo.WaveOTServer
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.server.datatype.AbstractServerDataTypeFactory

/**
  * @author Ronny Bräunlich
  */
class JsonServerDataTypeFactory extends AbstractServerDataTypeFactory[JsonServerDataType] {

  override def create(dataTypeInstanceId: DataTypeInstanceId): JsonServerDataType = {
    JsonServerDataType(dataTypeInstanceId, new WaveOTServer(), name)
  }

  override val name: DataTypeName = JsonServerDataTypeFactory.name
}

object JsonServerDataTypeFactory {

  val name = DataTypeName("json")

}
