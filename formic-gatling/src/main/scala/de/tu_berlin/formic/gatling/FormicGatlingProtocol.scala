package de.tu_berlin.formic.gatling

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import de.tu_berlin.formic.client.FormicSystemFactory
import de.tu_berlin.formic.common.ClientId
import de.tu_berlin.formic.datatype.json.client.JsonClientDataTypeProvider
import de.tu_berlin.formic.datatype.linear.client.LinearClientDataTypeProvider
import de.tu_berlin.formic.datatype.tree.client.TreeClientDataTypeProvider
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}

/**
  * @author Ronny Bräunlich
  */
case class FormicGatlingProtocol(serverUrl: String) extends Protocol {

  //FIXME
  val config = ConfigFactory.parseString("akka {\n  loglevel = info\n  http.client.idle-timeout = 10 minutes\n}\n\nformic {\n  server {\n    address = \"127.0.0.1\"\n    port = 8080\n  }\n  client {\n    buffersize = 100\n  }\n}")
  val formicSystem = FormicSystemFactory.create(config, Set(LinearClientDataTypeProvider(), TreeClientDataTypeProvider(), JsonClientDataTypeProvider()))
}

object FormicGatlingProtocol {
  val FormicGatlingProtocolKey: ProtocolKey = new ProtocolKey {

    override def newComponents(system: ActorSystem, coreComponents: CoreComponents): (FormicGatlingProtocol) => FormicGatlingComponents= {
      formicGatlingProtocol => FormicGatlingComponents(formicGatlingProtocol)
    }

    override def defaultValue(configuration: GatlingConfiguration): FormicGatlingProtocol =
      FormicGatlingProtocol(serverUrl = configuration.config.getString("formic.server.url"))

    override def protocolClass: Class[Protocol] = classOf[FormicGatlingProtocol]

    override type Components = FormicGatlingComponents
    override type Protocol = FormicGatlingProtocol
  }
}
