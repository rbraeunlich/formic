package de.tu_berlin.formic.example

import de.tu_berlin.formic.client.NewInstanceCallback
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}

/**
  * @author Ronny Bräunlich
  */
class ExampleCallback extends NewInstanceCallback {

  override def newCallbackFor(instance: FormicDataType, dataType: DataTypeName): () => Unit = () => {}

  override def doNewInstanceCreated(instance: FormicDataType, dataType: DataTypeName): Unit = {
    println("New data type created")
  }
}