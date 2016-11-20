package de.tu_berlin.formic.server

import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server.directives.Credentials
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Ronny Bräunlich
  */
class UniqueUsernameAuthenticatorSpec extends FlatSpec with Matchers {

  "UniqueUserNameAuthenticator" should "accept new usernames" in {
    val username = "foo"
    val auth = UniqueUsernameAuthenticator.authenticate(Credentials(Option(BasicHttpCredentials(username, ""))))
    val username2 = "foo1"
    val auth2 = UniqueUsernameAuthenticator.authenticate(Credentials(Option(BasicHttpCredentials(username2, ""))))
    val username3 = "foo2"
    val auth3 = UniqueUsernameAuthenticator.authenticate(Credentials(Option(BasicHttpCredentials(username3, ""))))

    auth should equal(Option(username))
    auth2 should equal(Option(username2))
    auth3 should equal(Option(username3))
  }

  "UniqueUserNameAuthenticator" should "reject missing credentials" in {
    val username = "foo"
    val auth = UniqueUsernameAuthenticator.authenticate(Credentials(Option.empty))

    auth should equal(None)
  }

  "UniqueUserNameAuthenticator" should "reject duplicate usernames" in {
    val username = "duplicate"
    UniqueUsernameAuthenticator.authenticate(Credentials(Option(BasicHttpCredentials(username, ""))))
    val auth = UniqueUsernameAuthenticator.authenticate(Credentials(Option(BasicHttpCredentials(username, ""))))

    auth should equal(None)
  }

}
