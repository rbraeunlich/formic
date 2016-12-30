package de.tu_berlin.formic.datatype.json

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.controlalgo.WaveOTServer
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.server.datatype.AbstractDataTypeFactory

/**
  * @author Ronny Bräunlich
  */
class JsonDataTypeFactory extends AbstractDataTypeFactory[JsonServerDataType] {

  override def create(dataTypeInstanceId: DataTypeInstanceId): JsonServerDataType = {
    JsonServerDataType(dataTypeInstanceId, new WaveOTServer(), name)
  }

  override val name: DataTypeName = JsonDataTypeFactory.name
}

object JsonDataTypeFactory {

  val name = DataTypeName("json")

}
