package de.tu_berlin.formic.gatling.builder

import de.tu_berlin.formic.gatling.action.tree._
import io.gatling.core.session.Expression

/**
  * @author Ronny Bräunlich
  */
case class FormicTreeBuilderBase(dataTypeInstanceId: Expression[String]) {

  def insert(toInsert: Int) = FormicTreeBuilderInsertPathStep(dataTypeInstanceId ,toInsert)

  def remove(pathElements: Seq[Expression[Int]]) = FormicTreeDeleteActionBuilder(dataTypeInstanceId, pathElements)

}

case class FormicTreeBuilderInsertPathStep(dataTypeInstanceId: Expression[String], toInsert: Int) {

  def path(pathElements: Seq[Expression[Int]]) = FormicTreeInsertActionBuilder(dataTypeInstanceId, toInsert, pathElements)

}