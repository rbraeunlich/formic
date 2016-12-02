package de.tu_berlin.formic.datatype.linear.server

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.controlalgo.GoogleWaveOTServer
import de.tu_berlin.formic.common.datatype.DataTypeName
import de.tu_berlin.formic.common.server.datatype.AbstractDataTypeFactory
import de.tu_berlin.formic.datatype.linear.LinearServerDataType
import upickle.default._

/**
  * @author Ronny Bräunlich
  */
//FIXME for some reason ScalaJS won't compile a LinearDataTypeFactory[S], therefore its String for now
class LinearDataTypeFactory extends AbstractDataTypeFactory[LinearServerDataType[String]] {

  override def create(dataTypeInstanceId: DataTypeInstanceId): LinearServerDataType[String] = {
    LinearServerDataType(dataTypeInstanceId, new GoogleWaveOTServer())
  }

  override val name: DataTypeName = LinearDataTypeFactory.dataTypeName
}

object LinearDataTypeFactory {
  val dataTypeName = LinearServerDataType.dataTypeName
}