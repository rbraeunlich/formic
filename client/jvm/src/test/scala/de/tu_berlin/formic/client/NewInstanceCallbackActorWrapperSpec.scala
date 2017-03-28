package de.tu_berlin.formic.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import de.tu_berlin.formic.common.datatype.client.AbstractClientDataTypeFactory.NewDataTypeCreated
import de.tu_berlin.formic.common.DataStructureInstanceId$
import de.tu_berlin.formic.common.datatype.client.ClientDataTypeEvent
import de.tu_berlin.formic.common.datatype.{DataStructureName, FormicDataType}
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
      val dataTypeWrapper = new TestFormicDataType(wrapper)

      wrapper ! NewDataTypeCreated(DataStructureInstanceId(), TestProbe().ref, dataTypeWrapper)

      Thread.sleep(1000)
      callback.called should be(true)
      dataTypeWrapper.callback should equal(callback.method)
    }
  }
}

class TestNewInstanceCallback extends NewInstanceCallback {

  var called = false

  val method = (_:ClientDataTypeEvent) => {}

  override def newCallbackFor(instance: FormicDataType, dataType: DataStructureName): (ClientDataTypeEvent) => Unit = method

  override def doNewInstanceCreated(instance: FormicDataType, dataType: DataStructureName): Unit = {
    called = true
  }
}