package de.tu_berlin.formic.gatling.builder

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.gatling.action.FormicCreateActionBuilder
import io.gatling.core.session.Expression

/**
  * @author Ronny Bräunlich
  */
case class FormicCreationBuilder(requestName: String) {

  def linear(dataTypeInstanceId: Expression[String]) = FormicCreateActionBuilder("linear", dataTypeInstanceId)

}
