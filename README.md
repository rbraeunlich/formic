# formic
formic - formidable internet collaboration

[![Build Status](https://travis-ci.org/rbraeunlich/formic.svg?branch=master)](https://travis-ci.org/rbraeunlich/formic) [![Scala.js](https://www.scala-js.org/assets/badges/scalajs-0.6.13.svg)](https://www.scala-js.org)

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)

*formic* is a library to enable [Operational Transformation](https://en.wikipedia.org/wiki/Operational_transformation) (OT) in applications and thus enable collaborative work.
The goal is to hide the details of OT and let developers work with data structures as if they were not edited concurrently. All OT data structures are eventually consistent.

## Why the name *formic*?

**Caution, minor Ender's Game spoiler!**

The Formic in Ender's Game by Orson Scott Card are capable of instant, faster-than-light thought communication. Except for the faster-than-light part, this library shall help with non-blocking, instant modification of data.

## The library

The library itself was developed as part of a graduation project. It consists of three main modules:

- common
- client
- server

All modules depend on the common module. The server part can run as a standalone server and is based on Scala and Akka. The client part, a ScalaJS implementation, is intended to be intergrated into a larger application, your application! The client part can be used either from JavaScript or from Scala/ScalaJS.


In addition to those modules, three data structures are already supported. The modules provide the classes that can be directly used. It is not necessary to use all of them. Nevertheless, the JSON module depends on the tree module. The concrete data structures are:

- linear structures
- trees
- JSON objects

The data structure modules contain both the implementations for the client and the server. Developers who want like to use *formic* work mainly with the client part. The server only needs some basic configuration and an Akka route to work.

Client and server are basically independent of each other. The client implementation is not limited to ScalaJS and could be implemented in any other language. The only things that are expected are a WebSocket connection and the Wave OT algorithm (for more information about that one, see [here](http://www.codecommit.com/blog/java/understanding-and-applying-operational-transformation)).

## Communication

Client and server communicate via JSON messages over a WebSocket connection. The messages are sent as plain text messages.
 
The existing messages for client <-> server communication are defined in [FormicMessage](https://github.com/rbraeunlich/formic/blob/master/common/shared/src/main/scala/de/tu_berlin/formic/common/message/FormicMessage.scala) and the JSON serialization in [FormicJsonProtocol](https://github.com/rbraeunlich/formic/blob/master/common/shared/src/main/scala/de/tu_berlin/formic/common/json/FormicJsonProtocol.scala).
Internally, [uPickle](http://www.lihaoyi.com/upickle-pprint/upickle/) is used for the serialization.

### Custom operations

Because the possible operations that can be applied to a data structure shall not be limited up-front, every data structure implementation has to provide an implementation of a [FormicJsonDataStructureProtocol](https://github.com/rbraeunlich/formic/blob/master/common/shared/src/main/scala/de/tu_berlin/formic/common/json/FormicJsonDataStructureProtocol.scala) and register it at the `FormicJsonProtocol`. 
The custom protocol is used to de-/serialize the operations of a data structure.

## Adding data structures

In order to add the data structures one needs or custom ones to the client and server, the cake pattern was used. The data structures all provide an implementation of the traits `ClientDataStructureProvider` and `ServerDataStructureProvider`. When instantiating the server, it can simply be done with the desired data structures, e.g.:
```
val server = new FormicServer with ServerDataStructures {
  override val dataStructureProvider: Set[ServerDataStructureProvider] =
    Set(LinearServerDataStructureProvider(), 
    	TreeServerDataStructureProvider(), 
    	JsonServerDataStructureProvider())
}
```
For the client the same applies, only the class `FormicSystem` has to be used.

## Running the example

In order to start the sample application clone the project and start sbt in the root directory. Then switch into the exampleJVM project and then enter `reStart`:
```
sbt
project exampleJVM
reStart
```

Simply using `run` might conflict with the main class ScalaJS expects. The webserver then starts on 0.0.0.0:8080, so you can access it either using your current ip or `localhost`.
The example for strings and trees is present at the root page or `index`. If you want to play collborative battleship you have to navigate to `localhost:8080/battleship`.
If another player wants to join the Battleship game he/she has to copy the id into the input field next to start and press it.

## Starting

### Server

In order to start the server, create a new instance with the data structures you need (see Adding data structures). When calling `start()`, an Akka `Http.ServerBinding` has to be passed to it. This network route tells the server to which addresses it should listen. You can configure the routes any way you want to, but one route has to use the `newUserProxy` method the server provides. This is necessary to know which users connect and to handle their messages. Your route could look like this:

```
    path("formic") {
      authenticateBasic[String]("FormicRealm", (creds) => authenticator.authenticate(creds)) {
        identifier =>
          get {
            handleWebSocketMessages(newUserMethod(identifier))
          }
      }
    }
```

### Client

A client connection is easily established. The configuration has to be provided (see Configuration section) and then `FormicSystemFactory.create()` is called with the config and the data structure provider. Invoking `init` on the `FormicSystem` will establish the connection.

## Configuration

### Server

The server can be configured like any Akka application. An `application.conf` has to be placed in the resources directory, so that Akka can find it. For the *formic* server

-address
-port
-incomingBufferSize
-outgoingBufferSize

can be configured. The log level is controlled via Akka. A configuration could look like this:
```
akka {
  loglevel = debug
  http.client.idle-timeout = 10 minutes
}

formic {
  server {
    address = "127.0.0.1"
    port = 8080
  }
  client {
    buffersize = 100
  }
}
```

### Client

Configuration on the client depends on the environment. Within Scala, it is sufficient to provide an `application.conf`. Within JavaScript this does not work. There, it is the best way to pass the configuration string to the `com.typesafe.config.ConfigFactory`. The returned object can then be passed to the `FormicSystemFactory`. The client needs the server address and port to be able to connect. Apart from that, its buffer size can be configured, e.g.:

```
formic {
  server {
    address = "127.0.0.1"
    port = 8080
  }
  client {
    buffersize = 100
  }
}
```

## Performance

*formic*'s performance was evaluated using the simulations in the `formic-gatling` module and the test you can find in [this](https://github.com/vinhqdang/collaborative_editing_measurement) repository. It was shown that *formic* can compete with e.g. GoogleDocs and ShareDB.

If you intend to replay the Gatling tests, simply import the `de.tu_berlin.formic.gatling.Predef` class. It provides you with the entry point by calling `formic("foo")`.  Please note that if you intend to run the tests in a distributed way the data structure instance ids have to be generated up front. If all virtual users are on a single computer, a simple feeder will suffice.

## Persistence

All data structures can be persisted on the server. Persistence is completely based on [Akka Persistence](http://doc.akka.io/docs/akka/current/scala/persistence.html). The configuration has to be placed in the `application.conf` of the server. A configuration could look like this:
```
akka {
  loglevel = info
  http.server.idle-timeout = 10 minutes

  persistence {
    journal {
      plugin = "akka.persistence.journal.leveldb"
      leveldb {
        dir = ${TMPDIR}"/persistence/journal"
        native = on
      }
    }
    snapshot-store {
      plugin = akka.persistence.snapshot-store.local
      local.dir = ${TMPDIR}"/persistence/snapshots"
    }
  }
}
```
where TMPDIR must be set in the environment, e.g. `.bashrc`.

## Final thoughts

Suggestions about how to improve *formic* are appreciated.
 
Next to its functional intention, *formic* can also be seen as an example application for Scala, ScalaJS, Akka and AkkaJS.
The example application also contains some Selenium based tests.