package de.tu_berlin.formic.example

import java.util.function.Consumer

import org.openqa.selenium.Keys
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.logging.LogEntry
import org.scalatest.selenium.{Firefox, WebBrowser}
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

/**
  * @author Ronny Bräunlich
  */
class WebSiteSpec extends FlatSpec
  with Matchers
  with WebBrowser
  with BeforeAndAfterAll{

  implicit val webDriver = new FirefoxDriver()

  val host = "http://localhost:8080"

  var serverThread: ServerThread = _

  override def beforeAll(): Unit = {
    serverThread = new ServerThread
    serverThread.setDaemon(true)
    serverThread.run()
    Thread.sleep(3000)
    implicitlyWait(Span(1, Seconds))
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
  }

  "The button to create a text" should "write the name and append a text area" in {
    go to host + "/index"
    click on id("new-string-button")
    className("stringId").element.text should include("String data type with id")
    className("stringInput").element shouldNot be(null)
  }

  "A single user" should "be able to write some text" in {
    go to host + "/index"
    click on id("new-string-button")
    implicitlyWait(Span(1, Seconds))
    val inputTextArea = textArea(className("stringInput"))
    inputTextArea.underlying.sendKeys("abc")
    Thread.sleep(5000)
  }

  "A second user" should "be able to subscribe to other string" in {
    go to host + "/index"
    click on id("new-string-button")
    implicitlyWait(Span(1, Seconds))
    val inputTextArea = textArea(className("stringInput"))
    val stringId = inputTextArea.underlying.getAttribute("id")
    inputTextArea.underlying.sendKeys("abc")
    Thread.sleep(5000)
    val secondUserDriver = new FirefoxDriver()
    go.to(host+"/index")(secondUserDriver)
    textField("subscribe-id")(secondUserDriver).value = stringId
    click.on("subscribe-button")(secondUserDriver)
    textArea(className("stringInput")).value should be("abc")
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