package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.datatype.linear.LinearDataType
import de.tu_berlin.formic.server.datatype.AbstractDataTypeFactory
import upickle.default._
import de.tu_berlin.formic.common.controlalgo.GoogleWaveOTServer
/**
  * @author Ronny Bräunlich
  */
class LinearDataTypeFactory[T] extends AbstractDataTypeFactory[LinearDataType[T]] {

  override def create(dataTypeInstanceId: DataTypeInstanceId): LinearDataType[T] = {
    LinearDataType(dataTypeInstanceId, new GoogleWaveOTServer())
  }

  override val name: DataTypeName = LinearDataType.dataTypeName
}