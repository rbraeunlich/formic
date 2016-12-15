package de.tu_berlin.formic.datatype.tree

/**
  * Represents the path a a certain node within a tree.
  * @author Ronny Bräunlich
  */
class AccessPath(path: List[Int]) {

  private val _path = scala.collection.mutable.ArrayBuffer(path:_*)

  def list: List[Int] = _path.toList

  /**
    * After descending one level inside the tree, the first element of the path can be dropped.
    */
  def dropFirstElement = _path.drop(1)


  override def toString = s"AccessPath(${_path})"


  def canEqual(other: Any): Boolean = other.isInstanceOf[AccessPath]

  override def equals(other: Any): Boolean = other match {
    case that: AccessPath =>
      (that canEqual this) &&
        _path == that._path
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(_path)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object AccessPath {
  def apply(path: List[Int]): AccessPath = new AccessPath(path)

  def apply(path: Int*): AccessPath = new AccessPath(path.toList)
}