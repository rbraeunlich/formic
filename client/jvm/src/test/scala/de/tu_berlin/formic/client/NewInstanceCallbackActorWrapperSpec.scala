package de.tu_berlin.formic.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataStructureFactory.NewDataStructureCreated
import de.tu_berlin.formic.common.DataStructureInstanceId
import de.tu_berlin.formic.common.datatype.client.ClientDataStructureEvent
import de.tu_berlin.formic.common.datatype.{DataStructureName, FormicDataStructure}
import org.scalatest.{Matchers, WordSpecLike}

/**
  * @author Ronny Bräunlich
  */
class NewInstanceCallbackActorWrapperSpec extends TestKit(ActorSystem("DispatcherSpec"))
  with WordSpecLike
  with ImplicitSender
  with StopSystemAfterAll
  with Matchers {

  "NewInstanceCallbackActorWrapperSpec" must {
    "forward messages to the callback" in {
      val callback = new TestNewInstanceCallback
      val wrapper = system.actorOf(Props(new NewInstanceCallbackActorWrapper(callback)))
      val dataTypeWrapper = new TestFormicDataStructure(wrapper)

      wrapper ! NewDataStructureCreated(DataStructureInstanceId(), TestProbe().ref, dataTypeWrapper)

      Thread.sleep(1000)
      callback.called should be(true)
      dataTypeWrapper.callback should equal(callback.method)
    }
  }
}

class TestNewInstanceCallback extends NewInstanceCallback {

  var called = false

  val method = (_:ClientDataStructureEvent) => {}

  override def newCallbackFor(instance: FormicDataStructure, dataType: DataStructureName): (ClientDataStructureEvent) => Unit = method

  override def doNewInstanceCreated(instance: FormicDataStructure, dataType: DataStructureName): Unit = {
    called = true
  }
}