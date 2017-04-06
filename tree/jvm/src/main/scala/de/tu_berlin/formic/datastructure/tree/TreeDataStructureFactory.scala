package de.tu_berlin.formic.datastructure.tree

import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.common.controlalgo.WaveOTServer
import de.tu_berlin.formic.common.server.datastructure.AbstractServerDataStructureFactory
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
abstract class TreeDataStructureFactory[S](implicit writer: Writer[S]) extends AbstractServerDataStructureFactory[TreeServerDataStructure[S]] {

  override def create(dataStructureInstanceId: DataStructureInstanceId): TreeServerDataStructure[S] = {
    TreeServerDataStructure(dataStructureInstanceId, new WaveOTServer(), name)
  }
}
