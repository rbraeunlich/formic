package de.tu_berlin.formic.common.message

import de.tu_berlin.formic.common.datatype.{DataStructureName, DataTypeOperation, OperationContext}
import de.tu_berlin.formic.common.json.{FormicJsonDataTypeProtocol, FormicJsonProtocol}
import de.tu_berlin.formic.common.json.FormicJsonProtocol._
import de.tu_berlin.formic.common.{ClientId, DataStructureInstanceId$, OperationId}
import org.scalatest._
import upickle.Js
import upickle.default._

/**
  * Simple test to verify that the JSON de-/serialization of the messages works
  *
  * @author Ronny Bräunlich
  */
class FormicMessageJsonSerializationSpec extends FlatSpec with Matchers {

  case class TestOperation(id: OperationId, operationContext: OperationContext,var clientId: ClientId) extends DataTypeOperation

  val testProtocol = new FormicJsonDataTypeProtocol {
    override val name: DataStructureName = DataStructureName("test")

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

  val jsonProtocol = FormicJsonProtocol()
  implicit val writer = jsonProtocol.writer
  implicit val reader = jsonProtocol.reader

  "JSON library" should "serialize a CreateResponse to JSON" in {
    val serialized = write(CreateResponse(DataStructureInstanceId.valueOf("123")))

    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.message.CreateResponse\",\"dataTypeInstanceId\":{\"id\":\"123\"}}")
  }

  it should "serialize a CreateRequest to JSON" in {
    val serialized = write(CreateRequest(ClientId.valueOf("678"), DataStructureInstanceId.valueOf("91011"), DataStructureName("test")))

    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.message.CreateRequest\",\"clientId\":{\"id\":\"678\"},\"dataTypeInstanceId\":{\"id\":\"91011\"},\"dataType\":{\"$type\":\"de.tu_berlin.formic.common.datatype.DataTypeName\",\"name\":\"test\"}}")
  }

  it should "serialize a HistoricOperationRequest with sinceId to JSON" in {
    val serialized = write(HistoricOperationRequest(ClientId.valueOf("123"), DataStructureInstanceId.valueOf("456"), OperationId.valueOf("1")))

    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.message.HistoricOperationRequest\",\"clientId\":{\"id\":\"123\"},\"dataTypeInstanceId\":{\"id\":\"456\"},\"sinceId\":{\"id\":\"1\"}}")
  }

  it should "serialize a HistoricOperationRequest without sinceId to JSON" in {
    val serialized = write(HistoricOperationRequest(ClientId.valueOf("123"), DataStructureInstanceId.valueOf("456"), null))

    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.message.HistoricOperationRequest\",\"clientId\":{\"id\":\"123\"},\"dataTypeInstanceId\":{\"id\":\"456\"},\"sinceId\":null}")
  }

  it should "serialize an UpdateResponse to JSON" in {
    val serialized = write(UpdateResponse(DataStructureInstanceId.valueOf("1"), DataStructureName("test"), "{data}", Option(OperationId("567"))))

    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.message.UpdateResponse\",\"dataTypeInstanceId\":{\"id\":\"1\"},\"dataType\":{\"$type\":\"de.tu_berlin.formic.common.datatype.DataTypeName\",\"name\":\"test\"},\"data\":\"{data}\",\"lastOperationId\":[{\"id\":\"567\"}]}")
  }

  it should "serialize an UpdateRequest to JSON" in {
    val serialized = write(UpdateRequest(ClientId.valueOf("1"), DataStructureInstanceId.valueOf("1")))

    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.message.UpdateRequest\",\"clientId\":{\"id\":\"1\"},\"dataTypeInstanceId\":{\"id\":\"1\"}}")
  }

  it should "serialize a OperationMessage to JSON" in {
    jsonProtocol.registerProtocol(testProtocol)

    val serialized = write(OperationMessage(ClientId.valueOf("1"), DataStructureInstanceId.valueOf("1"), DataStructureName("test"), List(TestOperation(OperationId("2"), OperationContext(List(OperationId("1"))), ClientId("1")))))

    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.message.OperationMessage\",\"clientId\":\"1\",\"dataTypeInstanceId\":\"1\",\"dataTypeName\":\"test\",\"operations\":[{\"operationId\":\"2\",\"operationContext\":[\"1\"],\"clientId\":\"1\"}]}")

    jsonProtocol.remove(testProtocol.name)
  }

  it should "deserialize a CreateResponse" in {
    val deserialized = read[FormicMessage]("{\"$type\":\"de.tu_berlin.formic.common.message.CreateResponse\",\"dataTypeInstanceId\":{\"id\":\"123\"}}")

    deserialized shouldBe a[CreateResponse]
    deserialized.asInstanceOf[CreateResponse].dataTypeInstanceId should be(DataStructureInstanceId("123"))
  }

  it should "deserialize an UpdateRequest" in {
    val deserialized = read[FormicMessage]("{\"$type\":\"de.tu_berlin.formic.common.message.UpdateRequest\",\"clientId\":{\"id\":\"1\"},\"dataTypeInstanceId\":{\"id\":\"1\"}}")

    deserialized shouldBe a[UpdateRequest]
    deserialized.asInstanceOf[UpdateRequest].dataTypeInstanceId should be(DataStructureInstanceId("1"))
    deserialized.asInstanceOf[UpdateRequest].clientId should be(ClientId("1"))
  }

  it should "deserialize a CreateRequest" in {
    val deserialized = read[FormicMessage]("{\"$type\":\"de.tu_berlin.formic.common.message.CreateRequest\",\"clientId\":{\"id\":\"678\"},\"dataTypeInstanceId\":{\"id\":\"91011\"},\"dataType\":{\"$type\":\"de.tu_berlin.formic.datatype.common.datatype.DataTypeName\",\"name\":\"test\"}}")

    deserialized shouldBe a[CreateRequest]
    deserialized.asInstanceOf[CreateRequest].clientId should be(ClientId("678"))
    deserialized.asInstanceOf[CreateRequest].dataTypeInstanceId should be(DataStructureInstanceId("91011"))
    deserialized.asInstanceOf[CreateRequest].dataType should be(DataStructureName("test"))
  }

  it should "deserialize a HistoricOperationRequest with sinceId" in {
    val deserialized = read[FormicMessage]("{\"$type\":\"de.tu_berlin.formic.common.message.HistoricOperationRequest\",\"clientId\":{\"id\":\"123\"},\"dataTypeInstanceId\":{\"id\":\"456\"},\"sinceId\":{\"id\":\"1\"}}")

    deserialized shouldBe a[HistoricOperationRequest]
    deserialized.asInstanceOf[HistoricOperationRequest].dataTypeInstanceId should be(DataStructureInstanceId("456"))
    deserialized.asInstanceOf[HistoricOperationRequest].clientId should be(ClientId("123"))
    deserialized.asInstanceOf[HistoricOperationRequest].sinceId should be(OperationId("1"))
  }

  it should "deserialize a HistoricOperationRequest without sinceId" in {
    val deserialized = read[FormicMessage]("{\"$type\":\"de.tu_berlin.formic.common.message.HistoricOperationRequest\",\"clientId\":{\"id\":\"123\"},\"dataTypeInstanceId\":{\"id\":\"456\"},\"sinceId\":null}")

    deserialized shouldBe a[HistoricOperationRequest]
    deserialized.asInstanceOf[HistoricOperationRequest].dataTypeInstanceId should be(DataStructureInstanceId("456"))
    deserialized.asInstanceOf[HistoricOperationRequest].clientId should be(ClientId("123"))
    deserialized.asInstanceOf[HistoricOperationRequest].sinceId should be(null)
  }

  it should "deserialize an UpdateResponse with lastOperationId" in {
    val deserialized = read[FormicMessage]("{\"$type\":\"de.tu_berlin.formic.common.message.UpdateResponse\",\"dataTypeInstanceId\":{\"id\":\"1\"},\"dataType\":{\"$type\":\"de.tu_berlin.formic.datatype.common.datatype.DataTypeName\",\"name\":\"test\"},\"data\":\"{data}\", \"lastOperationId\":[{\"id\":\"1\"}]}")

    deserialized shouldBe a[UpdateResponse]
    deserialized.asInstanceOf[UpdateResponse].dataTypeInstanceId should be(DataStructureInstanceId("1"))
    deserialized.asInstanceOf[UpdateResponse].dataType should be(DataStructureName("test"))
    deserialized.asInstanceOf[UpdateResponse].data should be("{data}")
    deserialized.asInstanceOf[UpdateResponse].lastOperationId.get should be(OperationId("1"))
  }

  it should "deserialize an UpdateResponse without lastOperationId" in {
    val deserialized = read[FormicMessage]("{\"$type\":\"de.tu_berlin.formic.common.message.UpdateResponse\",\"dataTypeInstanceId\":{\"id\":\"1\"},\"dataType\":{\"$type\":\"de.tu_berlin.formic.datatype.common.datatype.DataTypeName\",\"name\":\"test\"},\"data\":\"{data}\", \"lastOperationId\": []}")

    deserialized shouldBe a[UpdateResponse]
    deserialized.asInstanceOf[UpdateResponse].dataTypeInstanceId should be(DataStructureInstanceId("1"))
    deserialized.asInstanceOf[UpdateResponse].dataType should be(DataStructureName("test"))
    deserialized.asInstanceOf[UpdateResponse].data should be("{data}")
    deserialized.asInstanceOf[UpdateResponse].lastOperationId shouldBe empty
  }

  it should "deserialize an OperationMessage" in {
    jsonProtocol.registerProtocol(testProtocol)

    val deserialized = read[FormicMessage]("{\"$type\":\"de.tu_berlin.formic.common.message.OperationMessage\",\"clientId\":\"1\",\"dataTypeInstanceId\":\"1\",\"dataTypeName\":\"test\",\"operations\":[{\"operationId\":\"2\",\"operationContext\":[\"1\"],\"clientId\":\"1\"}]}")

    deserialized shouldBe a[OperationMessage]
    deserialized.asInstanceOf[OperationMessage].clientId should be(ClientId("1"))
    deserialized.asInstanceOf[OperationMessage].dataTypeInstanceId should be(DataStructureInstanceId("1"))
    deserialized.asInstanceOf[OperationMessage].dataType should be(DataStructureName("test"))
    deserialized.asInstanceOf[OperationMessage].operations should contain(TestOperation(OperationId("2"), OperationContext(List(OperationId("1"))), ClientId("1")))

    jsonProtocol.remove(testProtocol.name)
  }
}
