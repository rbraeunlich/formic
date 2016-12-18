package de.tu_berlin.formic.example

import de.tu_berlin.formic.client.NewInstanceCallback
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
import de.tu_berlin.formic.datatype.linear.client.FormicString
import org.scalajs.jquery.jQuery

/**
  * @author Ronny Bräunlich
  */
class ExampleCallback extends NewInstanceCallback {

  override def newCallbackFor(instance: FormicDataType, dataType: DataTypeName): () => Unit = Main.updateUIForString(instance.dataTypeInstanceId)

  override def doNewInstanceCreated(instance: FormicDataType, dataType: DataTypeName): Unit = {
    Main.strings += instance.asInstanceOf[FormicString]
    val id = instance.dataTypeInstanceId.id
    val inputId = id
    jQuery("body").append("<p>String data type with id " + id + "</p>")
    jQuery("body").append("<textarea rows=\"30\" cols=\"50\" id=\"" + inputId + "\"></textarea>")
    jQuery("#" + inputId).keypress(Main.keyPressHandler(inputId))

    Main.updateUIForString(instance.dataTypeInstanceId)()
  }
}
