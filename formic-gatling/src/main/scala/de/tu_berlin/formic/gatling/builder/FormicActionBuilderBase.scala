package de.tu_berlin.formic.gatling.builder

import io.gatling.core.config.GatlingConfiguration

/**
  * @author Ronny Bräunlich
  */
case class FormicActionBuilderBase(requestName: String) {

  def create()(implicit configuration: GatlingConfiguration) = FormicCreationBuilder(requestName)

}
