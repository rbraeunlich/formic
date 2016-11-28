package de.tu_berlin.formic.client

import akka.actor.Actor
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}

/**
  * @author Ronny Bräunlich
  */
trait NewInstanceCallback {

  def newCallbackFor(instance: FormicDataType, dataType: DataTypeName): () => Unit

  def doNewInstanceCreated(instance: FormicDataType, dataType: DataTypeName)

  final def newInstanceCreated(instance: FormicDataType, dataType: DataTypeName) = {
    instance.callback = newCallbackFor(instance, dataType)
    doNewInstanceCreated(instance, dataType)
  }

}
