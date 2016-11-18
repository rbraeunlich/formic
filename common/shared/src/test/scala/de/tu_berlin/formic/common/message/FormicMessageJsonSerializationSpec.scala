package de.tu_berlin.formic.common.message

import de.tu_berlin.formic.common.datatype.{DataTypeName, DataTypeOperation, OperationContext}
import de.tu_berlin.formic.common.json.{FormicJsonDataTypeProtocol, FormicJsonProtocol}
import de.tu_berlin.formic.common.json.FormicJsonProtocol._
import de.tu_berlin.formic.common.{ClientId, DataTypeInstanceId, OperationId}
import org.scalatest._
import upickle.Js
import upickle.default._

/**
  * Simple test to verify that the JSON de-/serialization of the messages works
  *
  * @author Ronny Bräunlich
  */
class FormicMessageJsonSerializationSpec extends FlatSpec with Matchers {

  case class TestOperation(id: OperationId, operationContext: OperationContext, clientId: ClientId) extends DataTypeOperation

  val testProtocol = new FormicJsonDataTypeProtocol {
    override val name: DataTypeName = DataTypeName("test")

    override def serializeOperation(op: DataTypeOperation): String = {
      Js.Obj(
        ("operationId", Js.Str(op.id.id)),
        ("operationContext", Js.Arr(op.operationContext.operations.map(o => Js.Str(o.id)): _*)),
        ("clientId", Js.Str(op.clientId.id))
      ).toString()
    }

    override def deserializeOperation(json: String): DataTypeOperation = {
      val valueMap = upickle.json.read(json).obj
      TestOperation(
        OperationId(valueMap("operationId").str),
        OperationContext(valueMap("operationContext").arr.map(v => OperationId(v.str)).toList),
        ClientId(valueMap("clientId").str))
    }
  }

  "JSON library" should "serialize a CreateResponse to JSON" in {
    val serialized = write(CreateResponse(DataTypeInstanceId.valueOf("123")))

    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.message.CreateResponse\",\"dataTypeInstanceId\":{\"id\":\"123\"}}")
  }

  it should "serialize a CreateRequest to JSON" in {
    val serialized = write(CreateRequest(ClientId.valueOf("678"), DataTypeInstanceId.valueOf("91011"), DataTypeName("test")))

    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.message.CreateRequest\",\"clientId\":{\"id\":\"678\"},\"dataTypeInstanceId\":{\"id\":\"91011\"},\"dataType\":{\"$type\":\"de.tu_berlin.formic.common.datatype.DataTypeName\",\"name\":\"test\"}}")
  }

  it should "serialize a HistoricOperationRequest to JSON" in {
    val serialized = write(HistoricOperationRequest(ClientId.valueOf("123"), DataTypeInstanceId.valueOf("456"), OperationId.valueOf("1")))

    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.message.HistoricOperationRequest\",\"clientId\":{\"id\":\"123\"},\"dataTypeInstanceId\":{\"id\":\"456\"},\"sinceId\":{\"id\":\"1\"}}")
  }

  it should "serialize an UpdateResponse to JSON" in {
    val serialized = write(UpdateResponse(DataTypeInstanceId.valueOf("1"), DataTypeName("test"), "{data}"))

    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.message.UpdateResponse\",\"dataTypeInstanceId\":{\"id\":\"1\"},\"dataType\":{\"$type\":\"de.tu_berlin.formic.common.datatype.DataTypeName\",\"name\":\"test\"},\"data\":\"{data}\"}")
  }

  it should "serialize an UpdateRequest to JSON" in {
    val serialized = write(UpdateRequest(ClientId.valueOf("1"), DataTypeInstanceId.valueOf("1")))

    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.message.UpdateRequest\",\"clientId\":{\"id\":\"1\"},\"dataTypeInstanceId\":{\"id\":\"1\"}}")
  }

  it should "serialize a OperationMessage to JSON" in {
    FormicJsonProtocol.registerProtocol(testProtocol)

    val serialized = write(OperationMessage(ClientId.valueOf("1"), DataTypeInstanceId.valueOf("1"), DataTypeName("test"), List(TestOperation(OperationId("2"), OperationContext(List(OperationId("1"))), ClientId("1")))))

    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.message.OperationMessage\",\"clientId\":\"1\",\"dataTypeInstanceId\":\"1\",\"dataTypeName\":\"test\",\"operations\":[{\"operationId\":\"2\",\"operationContext\":[\"1\"],\"clientId\":\"1\"}]}")

    FormicJsonProtocol.clear()
  }

  it should "deserialize a CreateResponse" in {
    val deserialized = read[FormicMessage]("{\"$type\":\"de.tu_berlin.formic.common.message.CreateResponse\",\"dataTypeInstanceId\":{\"id\":\"123\"}}")

    deserialized shouldBe a[CreateResponse]
    deserialized.asInstanceOf[CreateResponse].dataTypeInstanceId should be(DataTypeInstanceId("123"))
  }

  it should "deserialize an UpdateRequest" in {
    val deserialized = read[FormicMessage]("{\"$type\":\"de.tu_berlin.formic.common.message.UpdateRequest\",\"clientId\":{\"id\":\"1\"},\"dataTypeInstanceId\":{\"id\":\"1\"}}")

    deserialized shouldBe a[UpdateRequest]
    deserialized.asInstanceOf[UpdateRequest].dataTypeInstanceId should be(DataTypeInstanceId("1"))
    deserialized.asInstanceOf[UpdateRequest].clientId should be(ClientId("1"))
  }

  it should "deserialize a CreateRequest" in {
    val deserialized = read[FormicMessage]("{\"$type\":\"de.tu_berlin.formic.common.message.CreateRequest\",\"clientId\":{\"id\":\"678\"},\"dataTypeInstanceId\":{\"id\":\"91011\"},\"dataType\":{\"$type\":\"de.tu_berlin.formic.datatype.common.datatype.DataTypeName\",\"name\":\"test\"}}")

    deserialized shouldBe a[CreateRequest]
    deserialized.asInstanceOf[CreateRequest].clientId should be(ClientId("678"))
    deserialized.asInstanceOf[CreateRequest].dataTypeInstanceId should be(DataTypeInstanceId("91011"))
    deserialized.asInstanceOf[CreateRequest].dataType should be(DataTypeName("test"))
  }

  it should "deserialize a HistoricOperationRequest" in {
    val deserialized = read[FormicMessage]("{\"$type\":\"de.tu_berlin.formic.common.message.HistoricOperationRequest\",\"clientId\":{\"id\":\"123\"},\"dataTypeInstanceId\":{\"id\":\"456\"},\"sinceId\":{\"id\":\"1\"}}")

    deserialized shouldBe a[HistoricOperationRequest]
    deserialized.asInstanceOf[HistoricOperationRequest].dataTypeInstanceId should be(DataTypeInstanceId("456"))
    deserialized.asInstanceOf[HistoricOperationRequest].clientId should be(ClientId("123"))
    deserialized.asInstanceOf[HistoricOperationRequest].sinceId should be(OperationId("1"))
  }

  it should "deserialize an UpdateResponse" in {
    val deserialized = read[FormicMessage]("{\"$type\":\"de.tu_berlin.formic.common.message.UpdateResponse\",\"dataTypeInstanceId\":{\"id\":\"1\"},\"dataType\":{\"$type\":\"de.tu_berlin.formic.datatype.common.datatype.DataTypeName\",\"name\":\"test\"},\"data\":\"{data}\"}")

    deserialized shouldBe a[UpdateResponse]
    deserialized.asInstanceOf[UpdateResponse].dataTypeInstanceId should be(DataTypeInstanceId("1"))
    deserialized.asInstanceOf[UpdateResponse].dataType should be(DataTypeName("test"))
    deserialized.asInstanceOf[UpdateResponse].data should be("{data}")
  }

  it should "deserialize an OperationMessage" in {
    FormicJsonProtocol.registerProtocol(testProtocol)

    val deserialized = read[FormicMessage]("{\"$type\":\"de.tu_berlin.formic.common.message.OperationMessage\",\"clientId\":\"1\",\"dataTypeInstanceId\":\"1\",\"dataTypeName\":\"test\",\"operations\":[{\"operationId\":\"2\",\"operationContext\":[\"1\"],\"clientId\":\"1\"}]}")

    deserialized shouldBe a[OperationMessage]
    deserialized.asInstanceOf[OperationMessage].clientId should be(ClientId("1"))
    deserialized.asInstanceOf[OperationMessage].dataTypeInstanceId should be(DataTypeInstanceId("1"))
    deserialized.asInstanceOf[OperationMessage].dataType should be(DataTypeName("test"))
    deserialized.asInstanceOf[OperationMessage].operations should contain(TestOperation(OperationId("2"), OperationContext(List(OperationId("1"))), ClientId("1")))

    FormicJsonProtocol.clear()
  }
}
