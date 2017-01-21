package de.tu_berlin.formic.gatling.action.linear

import java.util.concurrent.{CountDownLatch, TimeUnit}

import de.tu_berlin.formic.client.{FormicSystem, NewInstanceCallback}
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.FormicDataType
import de.tu_berlin.formic.datatype.linear.client.{FormicList, FormicString}
import de.tu_berlin.formic.gatling.action.{CollectingCallbackWithListener, FormicActions, SessionVariables}
import io.gatling.commons.util.TimeHelper
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine

import scala.concurrent.Promise
import scala.util.{Failure, Success}

/**
  * @author Ronny Bräunlich
  */
case class LinearSubscription(dataTypeInstanceId: Expression[String], statsEngine: StatsEngine, next: Action) extends ChainableAction {

  override def name: String = "LinearSubscription action"

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val formicSystemOption = session(SessionVariables.FORMIC_SYSTEM).asOption[FormicSystem]
    val validatedDataTypeInstanceId = dataTypeInstanceId.apply(session)
    validatedDataTypeInstanceId.foreach { id =>
      formicSystemOption match {

        case Some(formicSystem) =>
          formicSystem.requestDataType(DataTypeInstanceId.valueOf(id))

          val callback = session(SessionVariables.CALLBACK).validate[CollectingCallbackWithListener].get //when the FormicSystem is present, this one must be, too
          val promise = Promise[FormicList[Any]]()
          val latch = new CountDownLatch(1)
          val callbackMethod = (d: FormicDataType) => {
            val suc = promise success d.asInstanceOf[FormicList[Any]]
            latch.countDown()
            suc
          }
          val callbackCondition = (d: FormicDataType) => d.dataTypeInstanceId == DataTypeInstanceId.valueOf(id)
          callback.addListener(callbackCondition, callbackMethod)
          //gotta block here, because the session is immutable
          latch.await(10, TimeUnit.SECONDS)
          promise.future.value match {
            case None => FormicActions.logKoTimingValues(start, TimeHelper.nowMillis, session, statsEngine, name)
            case Some(Failure(ex)) =>
              FormicActions.logKoTimingValues(start, TimeHelper.nowMillis, session, statsEngine, name)
              throw ex
            case Some(Success(string)) =>
              val modifiedSession = session.set(id, string)
              FormicActions.logOkTimingValues(start, TimeHelper.nowMillis, session, statsEngine, name)
              next ! modifiedSession
          }
        case None => throw new IllegalArgumentException("Users have to connect first!")
      }
    }
  }
}
