# formic
formic - formidable internet collaboration

[![Build Status](https://travis-ci.org/rbraeunlich/formic.svg?branch=master)](https://travis-ci.org/rbraeunlich/formic) [![Scala.js](https://www.scala-js.org/assets/badges/scalajs-0.6.13.svg)](https://www.scala-js.org)

*formic* is a library to enable [Operational Transformation](https://en.wikipedia.org/wiki/Operational_transformation)(OT) in applications.
The goal is to hide the details of OT and let developers work with data types as if the were not edited concurrently.

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
 
 Suggestions about how to improve *formic* are appreciated.
 
 Next to its functional intention, *formic* can also be seen as an example application for Scala, ScalaJS, Akka and AkkaJS.
  The example application also contains some Selenium based tests.