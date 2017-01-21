package de.tu_berlin.formic.gatling.builder

import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.gatling.action.FormicConnectActionBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression

/**
  * @author Ronny Bräunlich
  */
case class FormicActionBuilderBase(requestName: String) {

  def connect() = FormicConnectActionBuilder()

  def create()(implicit configuration: GatlingConfiguration) = FormicCreationBuilder(requestName)

  def linear(dataTypeInstanceId: Expression[String])(implicit configuration: GatlingConfiguration) = FormicLinearBuilderBase(dataTypeInstanceId)

}
