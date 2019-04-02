package ajr.rundeck.telegram

import java.net.URL
import java.net.HttpURLConnection
import java.util.ArrayList
import org.apache.commons.httpclient.NameValuePair
import java.net.URLEncoder
import scalaj.http._

class TelegramMessenger(botAuthKey: String, val baseUrl: String = "https://api.telegram.org", http: BaseHttp = Http) {

  def command(command: String, params: Map[String,String]) = {
    val url = s"$baseUrl/bot$botAuthKey/$command"
    val request = http(url).params(params).asString
    (request.code, request.body)
  }

  def sendMessage(chat: Long, message: String) = {
    command("sendMessage", Map("chat_id" -> chat.toString, "text" -> message))
  }
  
  def sendDocument(chat: Long, fileBytes: Array[Byte], fileName: String, mimeType: String = "text/plain") = {
    val url = s"$baseUrl/bot$botAuthKey/sendDocument"
    val params = Map("chat_id" -> chat.toString)
    val request = http(url).params(params).postMulti(MultiPart("document", fileName, mimeType, fileBytes)).asString
    (request.code, request.body)
  }
}