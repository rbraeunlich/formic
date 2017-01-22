package de.tu_berlin.formic.gatling.builder

import de.tu_berlin.formic.gatling.action.json._
import io.gatling.core.session.Expression
import upickle.default._
/**
  * @author Ronny Bräunlich
  */
case class FormicJsonBuilderBase(dataTypeInstanceId: Expression[String]) {

  def insert[T](toInsert: T)(implicit writer: Writer[T]) = FormicJsonBuilderInsertPathStep(dataTypeInstanceId ,toInsert)

  def remove(pathElements: Seq[Expression[String]]) = FormicJsonDeleteActionBuilder(dataTypeInstanceId, pathElements)

  def replace[T](replacement: T)(implicit writer: Writer[T]) = FormicJsonBuilderReplacePathStep(dataTypeInstanceId, replacement)

}

case class FormicJsonBuilderInsertPathStep[T](dataTypeInstanceId: Expression[String], toInsert: T)(implicit writer: Writer[T]) {

  def path(pathElements: Seq[Expression[String]]) = FormicJsonInsertActionBuilder(dataTypeInstanceId, toInsert, pathElements)

}

case class FormicJsonBuilderReplacePathStep[T](dataTypeInstanceId: Expression[String], replacement: T)(implicit writer: Writer[T]) {

  def path(pathElements: Seq[Expression[String]]) = FormicJsonReplacementActionBuilder(dataTypeInstanceId, replacement, pathElements)

}