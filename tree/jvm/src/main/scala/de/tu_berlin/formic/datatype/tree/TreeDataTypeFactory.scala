package de.tu_berlin.formic.datatype.tree

import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.common.controlalgo.WaveOTServer
import de.tu_berlin.formic.common.server.datatype.AbstractServerDataTypeFactory
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
abstract class TreeDataTypeFactory[S](implicit writer: Writer[S]) extends AbstractServerDataTypeFactory[TreeServerDataStructure[S]] {

  override def create(dataTypeInstanceId: DataStructureInstanceId): TreeServerDataStructure[S] = {
    TreeServerDataStructure(dataTypeInstanceId, new WaveOTServer(), name)
  }
}
