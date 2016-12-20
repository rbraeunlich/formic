package de.tu_berlin.formic.example

import de.tu_berlin.formic.datatype.tree.{AccessPath, EmptyTreeNode, TreeNode, ValueTreeNode}
import org.scalajs.jquery._

/**
  * @author Ronny Bräunlich
  */
class UITree(val whereId: String, val what: TreeNode) {

  def drawTree(): Unit = {
    val mainElement = jQuery("#" + whereId)
    var tree = new StringBuilder
    tree ++= "<div>"
    tree ++= inputElements(what, whereId)
    tree ++= "<ul>"
    what match {
      case v: ValueTreeNode =>
        tree ++= s"<li>${what.getData}"
        tree ++= "<ul>"
        if (v.children.nonEmpty) drawChildren(tree, v.getNode(AccessPath()).asInstanceOf[ValueTreeNode])
        tree ++= "</ul>"
        tree ++= "</li>"
      case EmptyTreeNode =>
        tree ++= "<li>empty</li>"
    }
    tree ++= "</ul>"
    tree ++= "</div>"
    mainElement.append(tree.toString)
  }

  def drawChildren(tree: StringBuilder, node: ValueTreeNode): Unit = {
    val children = node.children
    node.children.foreach(childNode => {
      tree ++= s"<li>${childNode.getData}"
      if (childNode.children.nonEmpty) {
        tree ++= "<ul>"
        children.foreach(drawChildren(tree, _))
        tree ++= "</ul>"
      }
      tree ++= "</li>"
    }
    )
  }

  def inputElements(what: TreeNode, id: String): String = {
    val elements = new StringBuilder
    elements ++= "<p>"
    elements ++= s"""<button id="insert$id">Insert</button>"""
    elements ++= s"""<input id="input$id" type="number" step="1">"""
    elements ++= """</br>"""
    elements ++= s"""<button id="delete$id">Delete</button>"""
    elements ++= "</p>"
    elements ++= "<p>"
    elements ++= s"""<select id="path$id">"""
    what match {
      case EmptyTreeNode => elements ++= "<option value=\"\">root</option>"
      case v: ValueTreeNode =>
        if(v.children.isEmpty) elements ++= "<option value=\"0\">0/</option>"
        else elements ++= addTreeLevel("", v.children)
    }
    elements ++= "</select>"
    elements ++= "</p>"
    elements.toString
  }

  def addTreeLevel(prefix: String, nodes: List[ValueTreeNode]): String = {
    val elements = new StringBuilder
    nodes.zipWithIndex.foreach(t => {
      val path = s"""$prefix${t._2}/"""
      elements ++= s"""<option value=\"$path\">$path</option>"""
      elements ++= addTreeLevel(path, t._1.children)
    })
    //to insert at the last index, not in between
    elements ++= s"""<option value=\"$prefix${nodes.length}\">$prefix${nodes.length}</option>"""
    elements.toString
  }
}
