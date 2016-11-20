package de.tu_berlin.formic.server

import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server.directives.Credentials
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

/**
  * @author Ronny Bräunlich
  */
class UniqueUsernameAuthenticatorSpec extends FlatSpec with Matchers with BeforeAndAfterAll{

  override def afterAll(): Unit = {
    UniqueUsernameAuthenticator.clear()
  }

  "UniqueUserNameAuthenticator" should "accept new usernames" in {
    val username = "UniqueUserNameAuthenticator"
    val auth = UniqueUsernameAuthenticator.authenticate(Credentials(Option(BasicHttpCredentials(username, ""))))
    val username2 = "UniqueUserNameAuthenticator1"
    val auth2 = UniqueUsernameAuthenticator.authenticate(Credentials(Option(BasicHttpCredentials(username2, ""))))
    val username3 = "UniqueUserNameAuthenticator2"
    val auth3 = UniqueUsernameAuthenticator.authenticate(Credentials(Option(BasicHttpCredentials(username3, ""))))

    auth should equal(Option(username))
    auth2 should equal(Option(username2))
    auth3 should equal(Option(username3))
  }

  it should "reject missing credentials" in {
    val auth = UniqueUsernameAuthenticator.authenticate(Credentials(Option.empty))

    auth should equal(None)
  }
  it should "reject duplicate usernames" in {
    val username = "duplicate"
    UniqueUsernameAuthenticator.authenticate(Credentials(Option(BasicHttpCredentials(username, ""))))
    val auth = UniqueUsernameAuthenticator.authenticate(Credentials(Option(BasicHttpCredentials(username, ""))))

    auth should equal(None)
  }

}
