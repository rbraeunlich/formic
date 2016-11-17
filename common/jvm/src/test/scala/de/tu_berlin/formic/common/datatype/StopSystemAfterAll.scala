package de.tu_berlin.formic.common.datatype

import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Suite}

/**
  * @author Ronny Bräunlich
  */
trait StopSystemAfterAll extends BeforeAndAfterAll{
  this:TestKit with Suite =>
  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

}
