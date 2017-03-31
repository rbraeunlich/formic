package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.common.controlalgo.WaveOTServer
import de.tu_berlin.formic.common.datastructure.DataStructureName
import de.tu_berlin.formic.common.server.datastructure.AbstractServerDataStructureFactory
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
abstract class LinearServerDataStructureFactory[S](implicit writer: Writer[S]) extends AbstractServerDataStructureFactory[LinearServerDataStructure[S]] {

  override def create(dataTypeInstanceId: DataStructureInstanceId): LinearServerDataStructure[S] = {
    LinearServerDataStructure(dataTypeInstanceId, new WaveOTServer(), name)
  }

}