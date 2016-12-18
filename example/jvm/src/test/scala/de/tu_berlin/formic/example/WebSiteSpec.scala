package de.tu_berlin.formic.example

import org.scalatest.selenium.Firefox
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

/**
  * @author Ronny Bräunlich
  */
class WebSiteSpec extends FlatSpec
  with Matchers
  with Firefox
  with BeforeAndAfterAll{

  val host = "http://localhost:8080"

  var serverThread: ServerThread = _

  override def beforeAll(): Unit = {
    serverThread = new ServerThread
    serverThread.setDaemon(true)
    serverThread.run()
    Thread.sleep(3000)

  }

  override def afterAll(): Unit = {
    serverThread.terminate()
    webDriver.quit()
  }

  "The home page" should "redirect to the index page" in {
    go to host
    currentUrl should be (host + "/index")
  }

  "The creation page" should "offer a button to create a text" in {
    go to host + "/index"
    click on id("new-string-button")
    println(pageSource)
  }

}


class ServerThread extends Thread {

  override def run() {
    ExampleServer.main(Array.empty)
  }

  def terminate(): Unit = {
    ExampleServer.shutdown
  }
}