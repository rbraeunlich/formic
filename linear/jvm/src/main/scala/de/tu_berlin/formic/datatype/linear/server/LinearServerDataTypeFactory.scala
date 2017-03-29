package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.common.controlalgo.WaveOTServer
import de.tu_berlin.formic.common.datatype.DataStructureName
import de.tu_berlin.formic.common.server.datatype.AbstractServerDataTypeFactory
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
abstract class LinearServerDataTypeFactory[S](implicit writer: Writer[S]) extends AbstractServerDataTypeFactory[LinearServerDataStructure[S]] {

  override def create(dataTypeInstanceId: DataStructureInstanceId): LinearServerDataStructure[S] = {
    LinearServerDataStructure(dataTypeInstanceId, new WaveOTServer(), name)
  }

}