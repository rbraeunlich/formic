package de.tu_berlin.formic.gatling.action

import de.tu_berlin.formic.client.NewInstanceCallback
import de.tu_berlin.formic.common.datatype.client.ClientDataTypeEvent
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}

import scala.concurrent.Promise

/**
  * @author Ronny Bräunlich
  */
class CollectingCallbackWithListener(timeMeasureCallback: TimeMeasureCallback) extends NewInstanceCallback {

  var dataTypes: List[FormicDataType] = List.empty

  var listener: List[((FormicDataType) => Boolean,(FormicDataType) => {})] = List.empty

  /**
    * Set a new callback interface at a data type instance that was created remotely.
    */
  override def newCallbackFor(instance: FormicDataType, dataType: DataTypeName): (ClientDataTypeEvent) => Unit = timeMeasureCallback.callbackMethod

  /**
    * Perform any initializations necessary for a new, remote data type.
    */
  override def doNewInstanceCreated(instance: FormicDataType, dataType: DataTypeName): Unit = {
    dataTypes = instance :: dataTypes
    //remove all listener that already matched to keep the list small
    val (matched, nonMatched) = listener.partition(t => t._1(instance))
    matched.foreach(t => t._2(instance))
    listener = nonMatched
  }

  def addListener[T](condition: (FormicDataType) => Boolean,listener: (FormicDataType) => Promise[T]): Unit = {
    this.listener = (condition, listener) :: this.listener
  }
}
