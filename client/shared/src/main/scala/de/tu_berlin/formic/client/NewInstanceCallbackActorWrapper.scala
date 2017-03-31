package de.tu_berlin.formic.client

import akka.actor.Actor
import de.tu_berlin.formic.common.datastructure.client.AbstractClientDataStructureFactory.NewDataStructureCreated

/**
  * An actor implementation that wraps the NewInstanceCallbacks the client provides. The client shall
  * not interact with Actors, therefore this wrapper is needed.
  * @author Ronny Bräunlich
  */
class NewInstanceCallbackActorWrapper(val callback: NewInstanceCallback) extends Actor {

  def receive = {
    case created: NewDataStructureCreated =>
      callback.newInstanceCreated(created.wrapper, created.wrapper.dataStructureName)
  }
}
