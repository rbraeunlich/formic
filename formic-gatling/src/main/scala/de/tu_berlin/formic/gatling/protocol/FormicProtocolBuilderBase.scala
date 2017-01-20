package de.tu_berlin.formic.gatling.protocol

import de.tu_berlin.formic.common.ClientId

/**
  * @author Ronny Bräunlich
  */
case object FormicProtocolBuilderBase {

  def url(url: String) = FormicProtocolBuilderUsernameStep(url)

}

case class FormicProtocolBuilderUsernameStep(url: String) {

  def username(name : String) = FormicProtocolBuilderBufferSizeStep(url, ClientId(name))

  def username(clientId: ClientId) = FormicProtocolBuilderBufferSizeStep(url, clientId)

}

case class FormicProtocolBuilderBufferSizeStep(url: String, username: ClientId) {

  def bufferSize(size: Int) = FormicProtocolBuilderLogLevelStep(url, username, size)

}

case class FormicProtocolBuilderLogLevelStep(url: String, username: ClientId, bufferSize: Int) {

  def logLevel(level: String) = FormicProtocolBuilder(url, username, bufferSize, level)
}

case class FormicProtocolBuilder(url: String, username: ClientId, bufferSize: Int, logLevel: String) {

  def build = new FormicGatlingProtocol(url, username, bufferSize, logLevel)
}
