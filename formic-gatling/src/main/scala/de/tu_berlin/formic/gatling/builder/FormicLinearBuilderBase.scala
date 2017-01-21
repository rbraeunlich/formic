package de.tu_berlin.formic.gatling.builder

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.gatling.action.linear._
import io.gatling.core.session.Expression

/**
  * @author Ronny Bräunlich
  */
case class FormicLinearBuilderBase() {

  def insert[T](toInsert: T) = FormicLinearBuilderInsertIndexStep(toInsert)

  def remove(index: Expression[Int]) = FormicLinearDeleteActionBuilder(index)

  def subscribe(dataTypeInstanceId: Expression[String]) = FormicLinearSubscriptionActionBuilder(dataTypeInstanceId)

}

case class FormicLinearBuilderInsertIndexStep(toInsert: Any) {

  def index(index: Expression[Int]) = FormicLinearInsertActionBuilder(toInsert, index)

}