package ajr.rundeck.telegram

import java.io.FileInputStream
import java.util.Properties
import scala.util.Try
import scalaj.http._

class TelegramIds(telegramProperties: String = "/etc/rundeck/telegram.properties") {
  val props = new Properties
  
  try {
    val input = new FileInputStream(telegramProperties);
    props.load(input)
    input.close
  }
  catch {
    case _: Throwable =>
      System.err.println(s"Failed to load $telegramProperties") 
  }

  def lookupChat(chatNameOrId: String): Option[Long] = {
    lookupNameOrId("chat", chatNameOrId).flatMap(x=> Try(x.toLong).toOption)
  }
  
  def lookupBot(botNameOrId: String): Option[String] = lookupNameOrId("bot", botNameOrId)
    
  private def lookupNameOrId(typ: String, nameOrId: String): Option[String] = {
    if (nameOrId == "")
      None
    else {
      val idStr = Option(props.getProperty(s"telegram.ids.$typ.$nameOrId")).getOrElse(nameOrId)
      Some(idStr)
    }
  }
}


object TelegramIdsTest extends App {
  val tids = new TelegramIds("/tmp/telegram.properties")
  val authKey = tids.lookupBot("trackabus_bot").get
  val telegram = new TelegramMessenger(authKey)
  
  val message = "Hi this is a test message: " + System.currentTimeMillis()
  
  
  def getJobLog(jobId: Int, authToken: String, baseUrl: String) = {
    val url = s"$baseUrl/api/6/execution/$jobId/output"
    val request = Http(url).params(Map("authtoken" -> authToken, "format" -> "json")).asString   
    request.body
  }
  
  
  val chatO = tids.lookupChat("devops-alerts-test")
  chatO match {
    case Some(chat) =>
      //val res = telegram.sendMessage(chat, message)
      
      val log = getJobLog(18, "RKWTzVQYAjYyKGECtsxkp4HFtdw6d6yE", "http://192.168.0.34:4440")
      
      val (code, body) = telegram.sendDocument(chat, "This is a big\nold document\nof several\nlines.\n\nFun eh?\n".getBytes, "mydoc.txt")
      println(code, body)
      
    case _ =>
      println("Invalid auth key or chat ID")
  }
}