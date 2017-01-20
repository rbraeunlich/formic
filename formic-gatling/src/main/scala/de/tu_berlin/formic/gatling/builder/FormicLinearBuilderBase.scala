package de.tu_berlin.formic.gatling.builder

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.gatling.action.linear.{FormicLinearDeleteActionBuilder, FormicLinearInsertActionBuilder}
import io.gatling.core.session.Expression

/**
  * @author Ronny Bräunlich
  */
case class FormicLinearBuilderBase(dataTypeInstanceId: DataTypeInstanceId) {

  def insert[T](toInsert: T) = FormicLinearBuilderInsertIndexStep(dataTypeInstanceId, toInsert)

  def remove(index: Expression[Int]) = FormicLinearDeleteActionBuilder(dataTypeInstanceId, index)

}

case class FormicLinearBuilderInsertIndexStep(dataTypeInstanceId: DataTypeInstanceId, toInsert: Any) {

  def index(index: Expression[Int]) = FormicLinearInsertActionBuilder(dataTypeInstanceId, toInsert, index)

}