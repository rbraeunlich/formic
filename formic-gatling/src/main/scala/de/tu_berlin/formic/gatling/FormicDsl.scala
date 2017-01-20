package de.tu_berlin.formic.gatling

import de.tu_berlin.formic.gatling.builder.FormicActionBuilderBase
import de.tu_berlin.formic.gatling.protocol.{FormicGatlingProtocol, FormicProtocolBuilder, FormicProtocolBuilderBase}

import scala.language.implicitConversions

/**
  * @author Ronny Bräunlich
  */
trait FormicDsl {

  val formic = FormicProtocolBuilderBase

  def formic(requestName: String) = FormicActionBuilderBase(requestName)


  implicit def formicProtocolBuilder2formicProtocol(builder: FormicProtocolBuilder): FormicGatlingProtocol = builder.build
}
