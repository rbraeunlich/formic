package de.tu_berlin.formic.datatype.tree

/**
  * Represents the path a a certain node within a tree.
  *
  * @author Ronny Bräunlich
  */
case class AccessPath(path: Int*) {

  /**
    * After descending one level inside the tree, the first element of the path can be dropped.
    * This method returns a copy with only element less int the list.
    */
  def dropFirstElement = AccessPath(path.drop(1):_*)
}