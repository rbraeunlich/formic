package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.datatype.linear.LinearDataType
import upickle.default._
import de.tu_berlin.formic.common.controlalgo.GoogleWaveOTServer
import de.tu_berlin.formic.common.server.datatype.AbstractDataTypeFactory

/**
  * @author Ronny Bräunlich
  */
//FIXME for some reason ScalaJS won't compile a LinearDataTypeFactory[S], therefore its String for now
class LinearDataTypeFactory extends AbstractDataTypeFactory[LinearDataType[String]] {

  override def create(dataTypeInstanceId: DataTypeInstanceId): LinearDataType[String] = {
    LinearDataType(dataTypeInstanceId, new GoogleWaveOTServer())
  }

  override val name: DataTypeName = LinearDataType.dataTypeName
}