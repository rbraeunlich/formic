package de.tu_berlin.formic.gatling.builder

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.gatling.action.FormicCreateActionBuilder

/**
  * @author Ronny Bräunlich
  */
case class FormicCreationBuilder(requestName: String) {

  def linear() = FormicCreateActionBuilder("linear")

}
