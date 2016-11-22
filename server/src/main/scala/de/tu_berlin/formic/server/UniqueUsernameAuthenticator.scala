package de.tu_berlin.formic.server

import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials.{Missing, Provided}

/**
  * @author Ronny Bräunlich
  */
object UniqueUsernameAuthenticator {

  private var usernames: Set[String] = Set.empty

  def authenticate(creds: Credentials): Option[String] = {
    creds match {
      case Provided(identifier) => if (usernames.contains(identifier)) Option.empty
      else {
        usernames = usernames + identifier
        Option(identifier)
      }
      case Missing => Option.empty
    }
  }

  def clear() = usernames = Set.empty
}
