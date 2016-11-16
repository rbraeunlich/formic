package de.tu_berlin.formic.datatype.common

/**
  * The name of a data type. It has to be unique among the different data types in order to be identifiable.
  * In order for the JSON serialization to work, subtypes have to implement proper apply() and unapply() methods.
  *
  * @author Ronny Bräunlich
  */
trait DataTypeName {
  val name: String
}

