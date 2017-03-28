package de.tu_berlin.formic.client

import akka.actor.Actor
import de.tu_berlin.formic.common.datatype.client.ClientDataTypeEvent
import de.tu_berlin.formic.common.datatype.{DataStructureName, FormicDataType}

/**
  * @author Ronny Bräunlich
  */
trait NewInstanceCallback {

  /**
    * Set a new callback interface at a data type instance that was created remotely.
    */
  def newCallbackFor(instance: FormicDataType, dataType: DataStructureName): (ClientDataTypeEvent) => Unit

  /**
    * Perform any initializations necessary for a new, remote data type.
    */
  def doNewInstanceCreated(instance: FormicDataType, dataType: DataStructureName)

  final def newInstanceCreated(instance: FormicDataType, dataType: DataStructureName) = {
    instance.callback = newCallbackFor(instance, dataType)
    doNewInstanceCreated(instance, dataType)
  }

}
