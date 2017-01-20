package de.tu_berlin.formic.gatling.protocol

import java.net.URL

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.{FormicSystemFactory, NewInstanceCallback}
import de.tu_berlin.formic.common.ClientId
import de.tu_berlin.formic.common.datatype.{DataTypeName, FormicDataType}
import de.tu_berlin.formic.datatype.json.client.JsonClientDataTypeProvider
import de.tu_berlin.formic.datatype.linear.client.LinearClientDataTypeProvider
import de.tu_berlin.formic.datatype.tree.client.TreeClientDataTypeProvider
import de.tu_berlin.formic.gatling.FormicGatlingComponents
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}

/**
  * @author Ronny Bräunlich
  */
case class FormicGatlingProtocol(serverUrl: String, username: ClientId, bufferSize: Int, logLevel: String) extends Protocol {

  val url = new URL(serverUrl)
  val config = ConfigFactory.parseString(s"""akka {\n  loglevel = $logLevel\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"${url.getHost}\"\n    port = ${url.getPort}\n  }\n  client {\n    buffersize = $bufferSize\n  }\n}""")
  val formicSystem = FormicSystemFactory.create(config,Set(LinearClientDataTypeProvider(), TreeClientDataTypeProvider(), JsonClientDataTypeProvider()))
  formicSystem.init(MockCallback, username)
}

object FormicGatlingProtocol {
  val FormicGatlingProtocolKey: ProtocolKey = new ProtocolKey {

    override def newComponents(system: ActorSystem, coreComponents: CoreComponents): (FormicGatlingProtocol) => FormicGatlingComponents= {
      formicGatlingProtocol => FormicGatlingComponents(formicGatlingProtocol)
    }

    override def defaultValue(configuration: GatlingConfiguration): FormicGatlingProtocol =
      throw new IllegalStateException("Can't provide a default value for FormicGatlingProtocol")

    override def protocolClass: Class[io.gatling.core.protocol.Protocol] = classOf[FormicGatlingProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    override type Components = FormicGatlingComponents
    override type Protocol = FormicGatlingProtocol
  }
}

object MockCallback extends NewInstanceCallback {
  /**
    * Set a new callback interface at a data type instance that was created remotely.
    */
  override def newCallbackFor(instance: FormicDataType, dataType: DataTypeName): () => Unit = () => {}

  /**
    * Perform any initializations necessary for a new, remote data type.
    */
  override def doNewInstanceCreated(instance: FormicDataType, dataType: DataTypeName): Unit = {}
}
