package de.tu_berlin.formic.datastructure.json

import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.common.controlalgo.WaveOTServer
import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.server.datastructure.AbstractServerDataStructureFactory

/**
  * @author Ronny Bräunlich
  */
class JsonServerDataStructureFactory extends AbstractServerDataStructureFactory[JsonServerDataStructure] {

  override def create(dataStructureInstanceId: DataStructureInstanceId): JsonServerDataStructure = {
    JsonServerDataStructure(dataStructureInstanceId, new WaveOTServer(), name)
  }

  override val name: DataStructureName = JsonServerDataStructureFactory.name
}

object JsonServerDataStructureFactory {

  val name = DataStructureName("json")

}
