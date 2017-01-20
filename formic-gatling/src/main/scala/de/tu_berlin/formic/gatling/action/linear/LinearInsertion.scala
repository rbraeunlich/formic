package de.tu_berlin.formic.gatling.action.linear

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.datatype.linear.client.FormicList
import de.tu_berlin.formic.gatling.action.FormicActions
import io.gatling.commons.util.TimeHelper
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Session, _}
import io.gatling.core.stats.StatsEngine

/**
  * @author Ronny Bräunlich
  */
class LinearInsertion(val toInsert: Any, val index: Expression[Int], val statsEngine: StatsEngine, val next: Action) extends ChainableAction {

  override def name: String = "LinearInsert action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val dataTypeAttribute = session("linear")
    val validatedIndex = index.apply(session)
    validatedIndex.foreach(i =>
      dataTypeAttribute.asOption[FormicList[Any]] match {
        case None => throw new IllegalArgumentException("Data type not found. Create it first!")
        case Some(dataType) => dataType.add(i, toInsert)
      })
    val end = TimeHelper.nowMillis
    FormicActions.logTimingValues(start, end, session, statsEngine, name)
    next ! session
  }

}
