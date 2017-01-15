package de.tu_berlin.formic.example

import java.time.Instant

import akka.actor.ActorSystem
import org.openqa.selenium.chrome.ChromeDriver
import org.scalatest.selenium.WebBrowser
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

/**
  * @author Ronny Bräunlich
  */
class WebSiteSpec extends FlatSpec
  with Matchers
  with WebBrowser
  with BeforeAndAfterAll
  with PersistenceCleanup {

  //val service = new ChromeDriverService.Builder().withVerbose(true).build()
  //implicit val webDriver = new ChromeDriver(service)
  implicit val webDriver = new ChromeDriver()

  val host = "http://localhost:8080"

  var serverThread: ServerThread = _

  override def beforeAll(): Unit = {
    serverThread = new ServerThread
    serverThread.setDaemon(true)
    serverThread.run()
    println("Starting FormicServer for WebSiteSpec")
    implicitlyWait(Span(10, Seconds))
  }

  override def afterAll(): Unit = {
    deleteStorageLocations(serverThread.exampleServer.server.system)
    serverThread.terminate()
    close()
  }

  "The home page" should "redirect to the index page" in {
    go to host
    currentUrl should be(host + "/index")
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
    val inputTextArea = textArea(className("stringInput"))
    inputTextArea.underlying.sendKeys("a")
    Thread.sleep(500)
    inputTextArea.underlying.sendKeys("b")
    Thread.sleep(500)
    inputTextArea.underlying.sendKeys("c")
  }

  "A second user" should "be able to subscribe to other string" in {
    go to host + "/index"
    click on id("new-string-button")
    val inputTextArea = textArea(className("stringInput"))
    val stringId = inputTextArea.underlying.getAttribute("id")
    inputTextArea.underlying.sendKeys("a")
    Thread.sleep(200)
    inputTextArea.underlying.sendKeys("b")
    Thread.sleep(200)
    inputTextArea.underlying.sendKeys("c")
    Thread.sleep(5000)
    val secondUserDriver = new ChromeDriver()
    go.to(host + "/index")(secondUserDriver)
    textField("subscribe-id")(secondUserDriver).value = stringId
    click.on("subscribe-button")(secondUserDriver)
    Thread.sleep(5000)
    textArea(stringId)(secondUserDriver).value should be("abc")
    secondUserDriver.quit()
  }

  "The creation page" should "offer a button to create a tree" in {
    go to host + "/index"
    click on id("new-tree-button")
  }

  "The button to create a tree" should "create a div containing input, buttons and a list" in {
    go to host + "/index"
    click on id("new-tree-button")
    Thread.sleep(2000)
    val treeHeadTag = tagName("div").findElement.get
    val treeId = treeHeadTag.attribute("id").get.replaceFirst("head", "")
    id("insert" + treeId).findElement should not be empty
    id("delete" + treeId).findElement should not be empty
    id("input" + treeId).findElement should not be empty
    Thread.sleep(10000)
    id(treeId).findElement should not be empty
    val treeTag = id(treeId).findElement.get
    treeTag.text should include("Tree data type with id " + treeId)
    id("path" + treeId).findElement should not be empty
    xpath(s"//div[@id='$treeId']/div/ul/li").findElement.get.text should be("empty")
  }

  "A single user" should "be able to modify the tree" in {
    go to host + "/index"
    click on id("new-tree-button")
    Thread.sleep(5000)
    val treeHeadTag = tagName("div").findElement.get
    val treeId = treeHeadTag.attribute("id").get.replaceFirst("head", "")
    numberField("input" + treeId).value = "2"
    click on id("insert" + treeId)
    Thread.sleep(3000)
    singleSel("path" + treeId).value should equal("0")
    xpath(s"//div[@id='$treeId']/div/ul/li").findElement.get.text should be("2")
  }

  "A second user" should "be able to subscribe to a tree" in {
    go to host + "/index"
    click on id("new-tree-button")
    Thread.sleep(5000)
    val treeHeadTag = tagName("div").findElement.get
    val treeId = treeHeadTag.attribute("id").get.replaceFirst("head", "")
    numberField("input" + treeId).value = "3"
    click on id("insert" + treeId)
    Thread.sleep(5000)
    val secondUserDriver = new ChromeDriver()
    go.to(host + "/index")(secondUserDriver)
    textField("subscribe-id")(secondUserDriver).value = treeId
    click.on("subscribe-button")(secondUserDriver)
    Thread.sleep(3000)
    xpath(s"//div[@id='$treeId']/div/ul/li").findElement(secondUserDriver).get.text should be("3")
    secondUserDriver.quit()
  }

  "Two users" should "be able to concurrently edit the tree" ignore {
    go to host + "/index"
    click on id("new-tree-button")
    Thread.sleep(3000)
    val treeHeadTag = tagName("div").findElement.get
    val treeId = treeHeadTag.attribute("id").get.replaceFirst("head", "")
    numberField("input" + treeId).value = "1"
    click on id("insert" + treeId)
    Thread.sleep(3000)
    val secondUserDriver = new ChromeDriver()
    go.to(host + "/index")(secondUserDriver)
    textField("subscribe-id")(secondUserDriver).value = treeId
    click.on("subscribe-button")(secondUserDriver)
    Thread.sleep(3000)
    xpath(s"//div[@id='$treeId']/div/ul/li").findElement(secondUserDriver).get.text should be("1")

    val user1Id = id("userId").findElement.get.text.replace("User: ", "")
    val user2Id = id("userId").findElement(secondUserDriver).get.text.replace("User: ", "")
    val firstValue = "10"
    val secondValue = "100"

    numberField("input" + treeId).value = if (user1Id > user2Id) firstValue else secondValue
    numberField("input" + treeId)(secondUserDriver).value = if (user2Id <= user1Id) secondValue else firstValue
    val t1 = new Thread(new Runnable {
      override def run(): Unit = {
        println("Before Click1 " + Instant.now)
        click on id("insert" + treeId)
        println("After Click1 " + Instant.now)
      }
    })
    val t2 = new Thread(new Runnable {
      override def run(): Unit = {
        println("Before Click2 " + Instant.now)
        click.on("insert" + treeId)(secondUserDriver)
        println("After Click2 " + Instant.now)
      }
    })
    t1.start()
    t2.start()
    //println("Before Click1 " + Instant.now)
    //click on id("insert" + treeId)
    //println("After Click1 " + Instant.now)
    //click.on("insert" + treeId)(secondUserDriver)
    //println("After Click2 " + Instant.now)
    Thread.sleep(5000)
    val elementsUser1 = xpath(s"//div[@id='$treeId']/div/ul/li/ul/li").findAllElements.toList
    elementsUser1.head.text should be(firstValue)
    elementsUser1(1).text should be(secondValue)
    val elementsUser2 = xpath(s"//div[@id='$treeId']/div/ul/li/ul/li").findAllElements(secondUserDriver).toList
    elementsUser2.head.text should be(firstValue)
    elementsUser2(1).text should be(secondValue)

    secondUserDriver.quit()
  }

}


