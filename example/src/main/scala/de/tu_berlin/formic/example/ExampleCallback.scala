package de.tu_berlin.formic.example

import de.tu_berlin.formic.client.NewInstanceCallback
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
import de.tu_berlin.formic.datatype.linear.client.FormicString
import org.scalajs.jquery._

/**
  * @author Ronny Bräunlich
  */
class ExampleCallback extends NewInstanceCallback {

  override def newCallbackFor(instance: FormicDataType, dataType: DataTypeName): () => Unit = Main.updateUIForFormicString(instance.asInstanceOf[FormicString])

  override def doNewInstanceCreated(instance: FormicDataType, dataType: DataTypeName): Unit = {
    Main.strings += instance.asInstanceOf[FormicString]
    val id = instance.dataTypeInstanceId.id
    val inputId = id
    jQuery("body").append("<p>String data type with id " + id + "</p>")
    jQuery("body").append("<input id=\"" + inputId + "\" name=\"string\" type=\"text\" value=\"\"></input>")
    jQuery("#" + inputId).keypress(Main.keyPressHandler(inputId))

    Main.updateUIForFormicString(instance.asInstanceOf[FormicString])()
  }
}
