package de.tu_berlin.formic.example

import akka.actor.ActorSystem
import akka.event.{LogSource, Logging}
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials.{Missing, Provided}

/**
  * @author Ronny Bräunlich
  */
class UniqueUsernameAuthenticator(actorSystem: ActorSystem) {

  private var usernames: Set[String] = Set.empty

  val log = Logging.getLogger(actorSystem, this)

  def authenticate(creds: Credentials): Option[String] = {
    log.debug(s"Client trying to connect with credentials: $creds")
    creds match {
      case Provided(identifier) =>
        if (usernames.contains(identifier)) {
          log.warning(s"Rejecting user $identifier because of duplicated username")
          Option.empty
        }
        else {
          usernames = usernames + identifier
          Option(identifier)
        }
      case Missing =>
        log.warning(s"Rejecting connection because of missing credentials")
        Option.empty
    }
  }
}

object UniqueUsernameAuthenticator {
  def apply(actorSystem: ActorSystem): UniqueUsernameAuthenticator = new UniqueUsernameAuthenticator(actorSystem)
}
