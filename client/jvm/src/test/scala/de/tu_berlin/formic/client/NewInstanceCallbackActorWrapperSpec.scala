package de.tu_berlin.formic.client

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import de.tu_berlin.formic.client.datatype.AbstractClientDataTypeFactory.NewDataTypeCreated
import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
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
      val dataTypeWrapper = new TestFormicDataType

      wrapper ! NewDataTypeCreated(DataTypeInstanceId(), TestProbe().ref, dataTypeWrapper)

      Thread.sleep(1000)
      callback.called should be(true)
      dataTypeWrapper.callback should equal(callback.method)
    }
  }
}

class TestNewInstanceCallback extends NewInstanceCallback {

  var called = false

  val method = () => {}

  override def newCallbackFor(instance: FormicDataType, dataType: DataTypeName): () => Unit = method

  override def doNewInstanceCreated(instance: FormicDataType, dataType: DataTypeName): Unit = {
    called = true
  }
}