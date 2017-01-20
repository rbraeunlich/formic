package de.tu_berlin.formic.gatling.builder

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.gatling.action.FormicLinearInsertActionBuilder
import io.gatling.core.session.Expression

/**
  * @author Ronny Bräunlich
  */
case class FormicLinearBuilderBase(dataTypeInstanceId: DataTypeInstanceId) {

  def insert[T](toInsert: T) = FormicLinearBuilderIndexStep(dataTypeInstanceId, toInsert)

}

case class FormicLinearBuilderIndexStep(dataTypeInstanceId: DataTypeInstanceId, toInsert: Any) {

  def index(index: Int) = FormicLinearInsertActionBuilder(dataTypeInstanceId, toInsert, index)

  //TODO
  //def index(index: Expression[Int]) = FormicLinearInsertActionBuilder(dataTypeInstanceId, toInsert, index.))

}