package de.tu_berlin.formic.common.server.datatype

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.common.datatype.DataStructureName
import de.tu_berlin.formic.common.message.CreateRequest

import scala.reflect.ClassTag

/**
  * @author Ronny Bräunlich
  */
//Why the ClassTag? See http://stackoverflow.com/questions/18692265/no-classtag-available-for-t-not-for-array
abstract class AbstractServerDataStructureFactory[T <: AbstractServerDataStructure : ClassTag] extends PersistentActor with ActorLogging {

  override def persistenceId: String = name.name

  val receiveCommand: Receive = {
    case req: CreateRequest =>
      val logText = s"Factory for $name received CreateRequest: $req"
      log.debug(logText)
      val newDataType = context.actorOf(Props(create(req.dataStructureInstanceId)), req.dataStructureInstanceId.id)
      persist(req) { request =>
        sender ! NewDataStructureCreated(request.dataStructureInstanceId, newDataType)
      }
  }

  val receiveRecover: Receive = {
    case CreateRequest(_, dataStructureInstanceId, _) =>
      context.actorOf(Props(create(dataStructureInstanceId)), dataStructureInstanceId.id)
    case RecoveryCompleted =>
      val logText = s"Data type factory $name recovered"
      log.info(logText)
  }

  def create(dataStructureInstanceId: DataStructureInstanceId): T

  val name: DataStructureName
}
