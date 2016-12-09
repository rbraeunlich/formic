package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.controlalgo.WaveOTServer
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.server.datatype.AbstractDataTypeFactory
import de.tu_berlin.formic.datatype.linear.LinearServerDataType
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
abstract class LinearDataTypeFactory[S](implicit writer: Writer[S]) extends AbstractDataTypeFactory[LinearServerDataType[S]] {

  override def create(dataTypeInstanceId: DataTypeInstanceId): LinearServerDataType[S] = {
    LinearServerDataType(dataTypeInstanceId, new WaveOTServer(), name)
  }

}