package de.tu_berlin.formic.datatype.tree

import scala.collection.mutable.ArrayBuffer

/**
  * @author Ronny Bräunlich
  */
case class TreeNode[T](value: T, children: ArrayBuffer[TreeNode[T]]) {

}
