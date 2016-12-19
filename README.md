# formic
formic - formidable internet collaboration

[![Build Status](https://travis-ci.org/rbraeunlich/formic.svg?branch=master)](https://travis-ci.org/rbraeunlich/formic) [![Scala.js](https://www.scala-js.org/assets/badges/scalajs-0.6.13.svg)](https://www.scala-js.org)

*formic* is a library to enable [Operational Transformation](https://en.wikipedia.org/wiki/Operational_transformation)(OT) in applications.
The goal is to hide the details of OT and let developers work with data types as if the were not edited concurrently.

## Why the name *formic*?

**Caution, minor Ender's Game spoiler!**

The Formic in Ender's Game by Orson Scott Card are capable of instant, faster-than-light thought communication. Except for the faster-than-light part, this library shall help with non-blocking, instant modification of data.

## The library

Currently, the whole library is a graduation project.

The library is composed of two main parts:
- client
- server

In addition to those two, several data types shall be supported. Those are the data types that can be worked on collaboratively.

- linear structures
- trees
- JSON

The data type submodules contain both the implementations for the client and the server. The server is a complete Scala implementation based on Akka. The client is a ScalaJS implementation.

Developers who would like to use *formic* work mainly with the client part. The server only needs some basic configuration and an Akka route to work.

The client part can be used either from JavaScript or from ScalaJS.

There are basically two modes to run the whole library. The first is to treat the client and server as two different standalone applications. I.e. that the server is placed somewhere and keeps running, while the client runs on either a different server or standalone on some computer.
 The example subproject shows how to run the application in the former mode.
 
 The whole client <-> server communication expects a WebSocket connection between the two.

## Communication

Client and server communicate via JSON messages. Therefore, both implementations are independent of each other and the used programming language. Actually, clients could be implemented in different programming languages and are not limited to using the provided ScalaJS client library.
It would be only necessary to provide a Scala implementation for the server.
 
The existing messages for client <-> server communication are defined in [FormicMessage](https://github.com/rbraeunlich/formic/blob/master/common/shared/src/main/scala/de/tu_berlin/formic/common/message/FormicMessage.scala) and the JSON serialization in [FormicJsonProtocol](https://github.com/rbraeunlich/formic/blob/master/common/shared/src/main/scala/de/tu_berlin/formic/common/json/FormicJsonProtocol.scala).
Internally, [uPickle](http://www.lihaoyi.com/upickle-pprint/upickle/) is used for the serialization.

### Custom operations

Because the possible operations that can be applied to a data type shall not be limited up-front, every data type implementation has to provide an implementation of a [FormicJsonDataTypeProtocol](https://github.com/rbraeunlich/formic/blob/master/common/shared/src/main/scala/de/tu_berlin/formic/common/json/FormicJsonDataTypeProtocol.scala) and register it at the `FormicJsonProtocol`. 
The custom protocol is used to de-/serialize the operations of a data type.
 
 Suggestions about how to improve *formic* are appreciated.
 
 Next to its functional intention, *formic* can also be seen as an example application for Scala, ScalaJS, Akka and AkkaJS.
  The example application also contains some Selenium based tests.