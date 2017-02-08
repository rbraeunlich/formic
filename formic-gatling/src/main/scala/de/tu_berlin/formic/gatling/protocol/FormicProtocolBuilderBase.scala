package de.tu_berlin.formic.gatling.protocol

import de.tu_berlin.formic.common.ClientId

/**
  * @author Ronny Bräunlich
  */
case object FormicProtocolBuilderBase {

  def url(url: String) = FormicProtocolBuilderBufferSizeStep(url)

}

case class FormicProtocolBuilderBufferSizeStep(url: String) {

  def bufferSize(size: Int) = FormicProtocolBuilderLogLevelStep(url, size)

}

case class FormicProtocolBuilderLogLevelStep(url: String, bufferSize: Int) {

  def logLevel(level: String) = FormicProtocolBuilder(url, bufferSize, level)
}

case class FormicProtocolBuilder(url: String, bufferSize: Int, logLevel: String) {

  def build = new FormicGatlingProtocol(url, bufferSize, logLevel)
}
