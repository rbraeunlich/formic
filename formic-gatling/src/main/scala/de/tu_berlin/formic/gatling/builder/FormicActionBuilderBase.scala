package de.tu_berlin.formic.gatling.builder

import de.tu_berlin.formic.gatling.action.{FormicConnectActionBuilder, FormicSubscriptionActionBuilder}
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression

/**
  * @author Ronny Bräunlich
  */
case class FormicActionBuilderBase(requestName: String) {

  def connect() = FormicConnectActionBuilder()

  def create()(implicit configuration: GatlingConfiguration) = FormicCreationBuilder(requestName)

  def linear(dataTypeInstanceId: Expression[String])(implicit configuration: GatlingConfiguration) = FormicLinearBuilderBase(dataTypeInstanceId)

  def subscribe(dataTypeInstanceId: Expression[String]) = FormicSubscriptionActionBuilder(dataTypeInstanceId)

  def tree(dataTypeInstanceId: Expression[String])(implicit configuration: GatlingConfiguration) = FormicTreeBuilderBase(dataTypeInstanceId)

  def json(dataTypeInstanceId: Expression[String])(implicit configuration: GatlingConfiguration) = FormicJsonBuilderBase(dataTypeInstanceId)

}
