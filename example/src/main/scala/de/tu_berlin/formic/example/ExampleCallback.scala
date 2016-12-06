package de.tu_berlin.formic.example

import de.tu_berlin.formic.client.NewInstanceCallback
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
import de.tu_berlin.formic.datatype.linear.client.FormicString
import org.scalajs.dom._
import org.scalajs.dom.raw.HTMLInputElement
import org.scalajs.jquery.{JQueryEventObject, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import de.tu_berlin.formic.example.ExampleCallback.BACKSPACE_CODE

/**
  * @author Ronny Bräunlich
  */
class ExampleCallback extends NewInstanceCallback {

  override def newCallbackFor(instance: FormicDataType, dataType: DataTypeName): () => Unit = Main.updateUIForFormicString(instance.asInstanceOf[FormicString])

  override def doNewInstanceCreated(instance: FormicDataType, dataType: DataTypeName): Unit = {
    val id = instance.dataTypeInstanceId.id
    val inputId = id
    //TODO
    jQuery("body").append("<p>String data type with id" + id + "</p>")
    jQuery("body").append("<input id=\"" + inputId + "\" name=\"string\" type=\"text\" value=\"\"></input>")
    jQuery("#" + inputId).keypress(Main.keyPressHandler(inputId))

    instance.asInstanceOf[FormicString].getAll.foreach {
      buff => jQuery("#" + inputId).value(buff.mkString)
    }
  }
}

object ExampleCallback {
  val BACKSPACE_CODE = 8
}