package de.tu_berlin.formic.datatype.json

/**
  * @author Ronny Bräunlich
  */
case class JsonPath(path: String*){

  def dropFirstElement = JsonPath(path.drop(1):_*)

}
