package de.tu_berlin.formic.datatype.tree

/**
  * Represents the path a a certain node within a tree.
  *
  * @author Ronny Bräunlich
  */
class AccessPath(path: List[Int]) {

  val list: List[Int] = path

  /**
    * After descending one level inside the tree, the first element of the path can be dropped.
    * This method returns a copy with only element less int the list.
    */
  def dropFirstElement = AccessPath(path.drop(1))


  def canEqual(other: Any): Boolean = other.isInstanceOf[AccessPath]

  override def equals(other: Any): Boolean = other match {
    case that: AccessPath =>
      (that canEqual this) &&
        list == that.list
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(list)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"AccessPath($list)"
}

object AccessPath {
  def apply(path: List[Int]): AccessPath = new AccessPath(path)

  def apply(path: Int*): AccessPath = new AccessPath(path.toList)
}