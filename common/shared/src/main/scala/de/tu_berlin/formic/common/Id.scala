package de.tu_berlin.formic.common

/**
  * This trait defines common method for the different types of ids used within formic.
  * @author Ronny Bräunlich
  */
trait Id {
  val id: String

  def >(otherId: ClientId): Boolean = {
    id > otherId.id
  }

  def <(otherId: ClientId): Boolean = {
    id < otherId.id
  }
}
