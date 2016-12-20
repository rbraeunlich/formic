package de.tu_berlin.formic.example

import de.tu_berlin.formic.client.NewInstanceCallback
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
import de.tu_berlin.formic.datatype.linear.client.FormicString
import de.tu_berlin.formic.datatype.tree.FormicIntegerTree
import org.scalajs.jquery.jQuery

/**
  * @author Ronny Bräunlich
  */
class ExampleCallback extends NewInstanceCallback {

  override def newCallbackFor(instance: FormicDataType, dataType: DataTypeName): () => Unit = {
    instance match {
      case str: FormicString => Main.updateUIForString(instance.dataTypeInstanceId)
      case tree: FormicIntegerTree => Main.updateUIForTree(instance.dataTypeInstanceId)
    }
  }

  override def doNewInstanceCreated(instance: FormicDataType, dataType: DataTypeName): Unit = {
    instance match {
      case str: FormicString =>
        Main.strings += str
        val id = str.dataTypeInstanceId.id
        val inputId = id
        jQuery("body").append("<p>String data type with id " + id + "</p>")
        jQuery("body").append("<textarea rows=\"30\" cols=\"50\" id=\"" + inputId + "\"></textarea>")
        jQuery("#" + inputId).keypress(Main.keyPressHandler(inputId))
        Main.updateUIForString(str.dataTypeInstanceId)()

      case tree: FormicIntegerTree =>
        Main.trees += tree
        if(jQuery("body").has("#"+tree.dataTypeInstanceId.id).length == 0){
          jQuery("body").append(s"""<div id=\"${tree.dataTypeInstanceId.id}\">""")
          jQuery("body").append("</div>")
        }
        Main.updateUIForTree(tree.dataTypeInstanceId)()
    }
  }
}
