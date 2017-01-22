package de.tu_berlin.formic.gatling.builder

import de.tu_berlin.formic.gatling.action.FormicCreateActionBuilder
import io.gatling.core.session.Expression

/**
  * @author Ronny Bräunlich
  */
case class FormicCreationBuilder(requestName: String) {

  def linear(dataTypeInstanceId: Expression[String]) = FormicCreateActionBuilder("linear", dataTypeInstanceId)

  def tree(dataTypeInstanceId: Expression[String]) = FormicCreateActionBuilder("tree", dataTypeInstanceId)

  def json(dataTypeInstanceId: Expression[String]) = FormicCreateActionBuilder("json", dataTypeInstanceId)

}
