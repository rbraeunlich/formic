package de.tu_berlin.formic.example

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server.directives.Credentials
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, FlatSpec, FlatSpecLike, Matchers}

/**
  * @author Ronny Bräunlich
  */
class UniqueUsernameAuthenticatorSpec extends TestKit(ActorSystem("UniqueUsernameAuthenticatorSpec")) with FlatSpecLike with Matchers with BeforeAndAfterAll{

  "UniqueUserNameAuthenticator" should "accept new usernames" in {
    val authenticator = UniqueUsernameAuthenticator(system)
    val username = "UniqueUserNameAuthenticator"
    val auth = authenticator.authenticate(Credentials(Option(BasicHttpCredentials(username, ""))))
    val username2 = "UniqueUserNameAuthenticator1"
    val auth2 = authenticator.authenticate(Credentials(Option(BasicHttpCredentials(username2, ""))))
    val username3 = "UniqueUserNameAuthenticator2"
    val auth3 = authenticator.authenticate(Credentials(Option(BasicHttpCredentials(username3, ""))))

    auth should equal(Option(username))
    auth2 should equal(Option(username2))
    auth3 should equal(Option(username3))
  }

  it should "reject missing credentials" in {
    val authenticator = UniqueUsernameAuthenticator(system)
    val auth = authenticator.authenticate(Credentials(Option.empty))

    auth should equal(None)
  }
  it should "reject duplicate usernames" in {
    val authenticator = UniqueUsernameAuthenticator(system)
    val username = "duplicate"
    authenticator.authenticate(Credentials(Option(BasicHttpCredentials(username, ""))))
    val auth = authenticator.authenticate(Credentials(Option(BasicHttpCredentials(username, ""))))

    auth should equal(None)
  }

}
