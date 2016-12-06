package de.tu_berlin.formic.example

import de.tu_berlin.formic.client.FormicSystem
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId}
import de.tu_berlin.formic.datatype.linear.client.FormicString
import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLInputElement
import org.scalajs.jquery.{JQueryEventObject, jQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.JSApp
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
object Main extends JSApp {

  val system = new FormicSystem()

  var string: FormicString = _

  val BACKSPACE_CODE = 8

  def main(): Unit = {
    system.init(new ExampleCallback(), ClientId())
    jQuery(setupUI _)
  }

  def setupUI() = {
    jQuery("#new-string-button").click(createNewString _)
    jQuery("#subscribe-button").click(subscribe _)
  }

  def createNewString() = {
    string = new FormicString(() => updateUI(string.dataTypeInstanceId.id), system)
    val id = string.dataTypeInstanceId
    val inputId = id.id
    jQuery("body").append("<p>String data type with id " + id + "</p>")
    jQuery("body").append("<input id=\"" + inputId + "\" name=\"string\" type=\"text\"</input>")
    jQuery("#" + inputId).keypress(keyPressHandler(inputId))
  }

  def updateUI(id: String): Unit = {
    string.getAll.foreach {
      buff => jQuery("#" + id).value(buff.mkString)
    }
  }

  def subscribe(): Unit = {
    val id = jQuery("#subscribe-id").value()
    system.requestDataType(DataTypeInstanceId(id.toString))
  }

  //common functions needed by Main and the callback
  def keyPressHandler(elementId: String): (JQueryEventObject) => Unit = {
    (event: JQueryEventObject) => {
      //this is quite some hack
      val index = document.getElementById(elementId).asInstanceOf[HTMLInputElement].selectionStart
      if (event.which != BACKSPACE_CODE) {
        val character = event.which.toChar
        println("Inserting new Character: " + character)
        string.add(index, character)
      } else {
        //since a delete with backspace starts behind the character to delete
        string.remove(index - 1)
      }
    }
  }

  def updateUIForFormicString(string: FormicString): () => Unit = () => {
    string.getAll.onComplete {
      case Success(buff) => jQuery("#" + string.dataTypeInstanceId.id).value(buff.mkString)
      case Failure(ex) => throw ex
    }
  }
}