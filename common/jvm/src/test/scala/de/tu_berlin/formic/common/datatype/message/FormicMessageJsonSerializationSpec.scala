package de.tu_berlin.formic.common.datatype.message

import de.tu_berlin.formic.common.DataTypeInstanceId
import de.tu_berlin.formic.common.message.{CreateResponse, FormicMessage}
import org.scalatest._
import spray.json._
import de.tu_berlin.formic.common.json.FormicJsonProtocol._

/**
  * Simple test to verify that the JSON de-/serialization works
  *
  * @author Ronny Bräunlich
  */
class FormicMessageJsonSerializationSpec extends FlatSpec with Matchers {

  "JSON library" should "serialize a CreateResponse to JSON" in {
    val serialized = CreateResponse(DataTypeInstanceId.valueOf("123")).toJson.toString

    serialized should be("{\"dataTypeInstanceId\":{\"id\":\"123\"}}")
  }

/*  "JSON library" should "serialize a CreateRequest to JSON" in {
    val serialized = write(CreateRequest(ClientId.valueOf("678"), DataTypeInstanceId.valueOf("91011"), testDataTypeName))
    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.datatype.message.CreateRequest\",\"clientId\":{\"id\":\"678\"},\"dataTypeInstanceId\":{\"id\":\"91011\"}},\"dataTypeName\":{\"name\":\"testType\"}")
  }

  "JSON library" should "serialize a HistoricOperationRequest to JSON" in {
    val serialized = write(HistoricOperationRequest(ClientId.valueOf("123"), DataTypeInstanceId.valueOf("456"), OperationId.valueOf("1")))
    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.datatype.message.HistoricOperationRequest\",\"clientId\":{\"id\":\"123\"},\"dataTypeInstanceId\":{\"id\":\"123\"}},\"clientId\":{\"id\":\"1\"}")
  }

  "JSON library" should "serialize a UpdateResponse to JSON" in {
    val serialized = write(UpdateResponse(DataTypeInstanceId.valueOf("1"), testDataTypeName, "test"))
    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.datatype.message.UpdateResponse\",\"dataTypeInstanceId\":{\"id\":\"1\"}},\"dataTypeName\":{\"name\":\"testType\"},\"data\":\"test\"")
  }

  "JSON library" should "serialize a UpdateRequest to JSON" in {
    val serialized = write(UpdateRequest(ClientId.valueOf("1"), DataTypeInstanceId.valueOf("1")))
    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.datatype.message.UpdateRequest\",\"clientId\":{\"id\":\"1\"},\"dataTypeInstanceId\":{\"id\":\"1\"}}")
  }

  "JSON library" should "serialize a OperationMessage to JSON" in {
    val serialized = write(OperationMessage(ClientId.valueOf("1"), DataTypeInstanceId.valueOf("1"), testDataTypeName, List.empty[DataTypeOperation]))
    serialized should be("{\"$type\":\"de.tu_berlin.formic.common.datatype.message.OperationMessage\",\"dataTypeInstanceId\":{\"id\":\"123\"}},\"dataTypeName\":{\"name\":\"testType\"},\"operations\":\"[]\"")
  }
*/
  "JSON library" should "deserialize a CreateResponse to JSON" in {
   val deserialized = JsonParser("{\"dataTypeInstanceId\":{\"id\":\"123\"}}").convertTo[FormicMessage]

    deserialized shouldBe a[CreateResponse]
    deserialized.asInstanceOf[CreateResponse].dataTypeInstanceId should be(DataTypeInstanceId("123"))
  }

}
