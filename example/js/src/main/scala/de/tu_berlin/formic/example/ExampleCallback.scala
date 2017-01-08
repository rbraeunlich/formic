package de.tu_berlin.formic.example

import de.tu_berlin.formic.client.NewInstanceCallback
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
import de.tu_berlin.formic.datatype.json.client.FormicJsonObject
import de.tu_berlin.formic.datatype.linear.client.FormicString
import de.tu_berlin.formic.datatype.tree.client.FormicIntegerTree
import org.scalajs.jquery.jQuery

/**
  * @author Ronny Bräunlich
  */
class ExampleCallback(val main: Main) extends NewInstanceCallback {

  override def newCallbackFor(instance: FormicDataType, dataType: DataTypeName): () => Unit = {
    instance match {
      case str: FormicString => main.updateUIForString(instance.dataTypeInstanceId)
      case tree: FormicIntegerTree => main.updateUIForTree(instance.dataTypeInstanceId)
      case json: FormicJsonObject => main.newCallbackForBattleship(json)
    }
  }

  override def doNewInstanceCreated(instance: FormicDataType, dataType: DataTypeName): Unit = {
    instance match {
      case str: FormicString =>
        main.strings += str
        val id = str.dataTypeInstanceId.id
        val inputId = id
        jQuery("body").append("<p>String data type with id " + id + "</p>")
        jQuery("body").append("<textarea rows=\"30\" cols=\"50\" class=\"stringInput\" id=\"" + inputId + "\"></textarea>")
        jQuery("#" + inputId).keypress(main.keyPressHandler(inputId))
        main.updateUIForString(str.dataTypeInstanceId)()

      case tree: FormicIntegerTree =>
        main.trees += tree
        if(jQuery("body").has("#"+tree.dataTypeInstanceId.id).length == 0){
          main.insertBasicTreeElements(tree.dataTypeInstanceId.id)
        }
        main.updateUIForTree(tree.dataTypeInstanceId)()

      case json: FormicJsonObject =>
        main.jsons += json
        //in order to force Battleship to update the UI we invoke the callback here manually
        json.callback()
    }
  }
}
