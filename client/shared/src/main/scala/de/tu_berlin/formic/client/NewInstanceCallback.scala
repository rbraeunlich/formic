package de.tu_berlin.formic.client

import akka.actor.Actor
import de.tu_berlin.formic.common.datatype.client.ClientDataTypeEvent
import de.tu_berlin.formic.common.datatype.{DataStructureName, FormicDataStructure}

/**
  * @author Ronny Bräunlich
  */
trait NewInstanceCallback {

  /**
    * Set a new callback interface at a data type instance that was created remotely.
    */
  def newCallbackFor(instance: FormicDataStructure, dataType: DataStructureName): (ClientDataTypeEvent) => Unit

  /**
    * Perform any initializations necessary for a new, remote data type.
    */
  def doNewInstanceCreated(instance: FormicDataStructure, dataType: DataStructureName)

  final def newInstanceCreated(instance: FormicDataStructure, dataType: DataStructureName) = {
    instance.callback = newCallbackFor(instance, dataType)
    doNewInstanceCreated(instance, dataType)
  }

}
