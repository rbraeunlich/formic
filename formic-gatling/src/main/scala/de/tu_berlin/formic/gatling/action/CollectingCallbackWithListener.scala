package de.tu_berlin.formic.gatling.action

import de.tu_berlin.formic.client.NewInstanceCallback
import de.tu_berlin.formic.common.datastructure.client.ClientDataStructureEvent
import de.tu_berlin.formic.common.datastructure.{DataStructureName, FormicDataStructure}

import scala.concurrent.Promise

/**
  * @author Ronny Bräunlich
  */
class CollectingCallbackWithListener(timeMeasureCallback: TimeMeasureCallback) extends NewInstanceCallback {

  var dataTypes: List[FormicDataStructure] = List.empty

  var listener: List[((FormicDataStructure) => Boolean,(FormicDataStructure) => {})] = List.empty

  /**
    * Set a new callback interface at a data type instance that was created remotely.
    */
  override def newCallbackFor(instance: FormicDataStructure, dataType: DataStructureName): (ClientDataStructureEvent) => Unit = timeMeasureCallback.callbackMethod

  /**
    * Perform any initializations necessary for a new, remote data type.
    */
  override def doNewInstanceCreated(instance: FormicDataStructure, dataType: DataStructureName): Unit = {
    dataTypes = instance :: dataTypes
    //remove all listener that already matched to keep the list small
    val (matched, nonMatched) = listener.partition(t => t._1(instance))
    matched.foreach(t => t._2(instance))
    listener = nonMatched
  }

  def addListener[T](condition: (FormicDataStructure) => Boolean, listener: (FormicDataStructure) => Promise[T]): Unit = {
    this.listener = (condition, listener) :: this.listener
  }
}
