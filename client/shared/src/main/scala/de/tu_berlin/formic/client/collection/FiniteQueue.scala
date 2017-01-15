package de.tu_berlin.formic.client.collection

import scala.language.implicitConversions

/**
  * @author Ronny Bräunlich
  */
class FiniteQueue[A](q: scala.collection.mutable.Queue[A]) {
  def enqueueFinite(elem: A, maxSize: Int): Unit = {
    while (q.size >= maxSize) {
      q.dequeue()
    }
    q.enqueue(elem)
  }
}

object FiniteQueue {
  implicit def queue2finitequeue[A](q: scala.collection.mutable.Queue[A]): FiniteQueue[A] = new FiniteQueue[A](q)
}