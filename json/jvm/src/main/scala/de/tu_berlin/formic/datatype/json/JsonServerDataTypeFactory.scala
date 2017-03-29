package de.tu_berlin.formic.datatype.json

import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.common.controlalgo.WaveOTServer
import de.tu_berlin.formic.common.datatype.DataStructureName
import de.tu_berlin.formic.common.server.datatype.AbstractServerDataTypeFactory

/**
  * @author Ronny Bräunlich
  */
class JsonServerDataTypeFactory extends AbstractServerDataTypeFactory[JsonServerDataType] {

  override def create(dataTypeInstanceId: DataStructureInstanceId): JsonServerDataType = {
    JsonServerDataType(dataTypeInstanceId, new WaveOTServer(), name)
  }

  override val name: DataStructureName = JsonServerDataTypeFactory.name
}

object JsonServerDataTypeFactory {

  val name = DataStructureName("json")

}
