package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.DataStructureInstanceId$
import de.tu_berlin.formic.common.controlalgo.WaveOTServer
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.server.datatype.AbstractServerDataTypeFactory
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
abstract class LinearServerDataTypeFactory[S](implicit writer: Writer[S]) extends AbstractServerDataTypeFactory[LinearServerDataType[S]] {

  override def create(dataTypeInstanceId: DataStructureInstanceId): LinearServerDataType[S] = {
    LinearServerDataType(dataTypeInstanceId, new WaveOTServer(), name)
  }

}