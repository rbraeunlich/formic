package de.tu_berlin.formic.common.datastructure

/**
  * The name of a data type. It has to be unique among the different data types in order to be identifiable.
  *
  * @author Ronny Bräunlich
  */
sealed case class DataStructureName(name: String)
