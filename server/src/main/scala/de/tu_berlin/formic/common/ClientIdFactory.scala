package de.tu_berlin.formic.common

/**
  * @author Ronny Bräunlich
  */

trait ClientId {
  val id: String

  def >(otherId: ClientId): Boolean = {
    id > otherId.id
  }

  def <(otherId: ClientId): Boolean = {
    id < otherId.id
  }
}

private case class ClientIdImpl(id: String) extends ClientId

object ClientIdFactory {

  def apply(): ClientId = {
    ClientIdImpl(java.util.UUID.randomUUID.toString)
  }

  def apply(s : String): ClientId = {
    ClientIdImpl(s)
  }
}
