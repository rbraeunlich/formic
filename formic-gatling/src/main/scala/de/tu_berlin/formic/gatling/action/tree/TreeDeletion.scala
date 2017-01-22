package de.tu_berlin.formic.gatling.action.tree

import de.tu_berlin.formic.datatype.tree.AccessPath
import de.tu_berlin.formic.datatype.tree.client.FormicTree
import de.tu_berlin.formic.gatling.action.FormicActions
import io.gatling.commons.util.TimeHelper
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine

/**
  * @author Ronny Bräunlich
  */
case class TreeDeletion(dataTypeInstanceId: Expression[String], statsEngine: StatsEngine, next: Action, pathElements: Seq[Expression[Int]]) extends ChainableAction {
  override def name: String = "TreeInsert action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    dataTypeInstanceId.apply(session).foreach { id =>
      val dataTypeAttribute = session(id)
      val validatedPath = pathElements.map(elem => elem.apply(session).get)
      val path = AccessPath(validatedPath: _*)
      dataTypeAttribute.asOption[FormicTree[Any]] match {
        case None => throw new IllegalArgumentException("Data type not found. Create it first!")
        case Some(dataType) => dataType.remove(path)
      }
      val end = TimeHelper.nowMillis
      FormicActions.logOkTimingValues(start, end, session, statsEngine, name)
      next ! session
    }
  }
}
