package de.tu_berlin.formic.gatling.builder

import de.tu_berlin.formic.common.DataTypeInstanceId
import io.gatling.core.config.GatlingConfiguration

/**
  * @author Ronny Bräunlich
  */
case class FormicActionBuilderBase(requestName: String) {

  def create()(implicit configuration: GatlingConfiguration) = FormicCreationBuilder(requestName)

  def linear()(implicit configuration: GatlingConfiguration) = FormicLinearBuilderBase()

}
