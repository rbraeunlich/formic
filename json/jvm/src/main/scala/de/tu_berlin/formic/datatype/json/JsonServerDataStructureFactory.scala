package de.tu_berlin.formic.datatype.json

import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.common.controlalgo.WaveOTServer
import de.tu_berlin.formic.common.datatype.DataStructureName
import de.tu_berlin.formic.common.server.datatype.AbstractServerDataStructureFactory

/**
  * @author Ronny Bräunlich
  */
class JsonServerDataStructureFactory extends AbstractServerDataStructureFactory[JsonServerDataStructure] {

  override def create(dataTypeInstanceId: DataStructureInstanceId): JsonServerDataStructure = {
    JsonServerDataStructure(dataTypeInstanceId, new WaveOTServer(), name)
  }

  override val name: DataStructureName = JsonServerDataStructureFactory.name
}

object JsonServerDataStructureFactory {

  val name = DataStructureName("json")

}
