package de.tu_berlin.formic.client

import akka.actor.Actor
import de.tu_berlin.formic.common.datatype.client.ClientDataTypeEvent
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}

/**
  * @author Ronny Bräunlich
  */
trait NewInstanceCallback {

  /**
    * Set a new callback interface at a data type instance that was created remotely.
    */
  def newCallbackFor(instance: FormicDataType, dataType: DataTypeName): (ClientDataTypeEvent) => Unit

  /**
    * Perform any initializations necessary for a new, remote data type.
    */
  def doNewInstanceCreated(instance: FormicDataType, dataType: DataTypeName)

  final def newInstanceCreated(instance: FormicDataType, dataType: DataTypeName) = {
    instance.callback = newCallbackFor(instance, dataType)
    doNewInstanceCreated(instance, dataType)
  }

}
