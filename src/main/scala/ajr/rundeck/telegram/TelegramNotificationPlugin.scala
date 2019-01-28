package ajr.rundeck.telegram

import java.net.InetSocketAddress
import java.util.{Map => JMap}

import scala.collection.JavaConversions._
import com.dtolabs.rundeck.core.plugins.Plugin
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty
import com.dtolabs.rundeck.plugins.descriptions.TextArea
import com.dtolabs.rundeck.plugins.notification.NotificationPlugin
import scalaj.http._
import com.dtolabs.rundeck.core.plugins.configuration.PropertyScope
import freemarker.cache.StringTemplateLoader
import freemarker.template.Configuration
import TelegramNotificationPlugin._
import java.io.File
import java.io.StringWriter
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Date

object TelegramNotificationPlugin {
  val fmConfig = new Configuration
  fmConfig.setDefaultEncoding("UTF-8")
}

@Plugin(service = "Notification", name = "TelegramNotification")
@PluginDescription(title = "Telegram")
class TelegramNotificationPlugin extends NotificationPlugin {    
  @PluginProperty(title = "Bot name/token", 
                  description = "Bot name or auth token. Names must be defined in telegram.properties. If blank inherits the project value if it exists", 
                  required = false, scope = PropertyScope.InstanceOnly)
  private var botAuthToken: String = _

  @PluginProperty(title = "Project default Bot name/token", 
                  description = "Bot name or auth token. Names must be defined in telegram.properties", 
                  required = false, scope = PropertyScope.Project)
  private var projectBotAuthToken: String = _
  
  @PluginProperty(title = "Chat name/ID", 
                  description = "Name or ID of chat to send message to. Names must be defined in telegram.properties", 
                  required = false, scope = PropertyScope.InstanceOnly)
  private var chatId: String = _

  @PluginProperty(title = "Telegram config file", 
                  description = "Location of the telegram.properties file for bot/chat name mapping", 
                  required = false, defaultValue = "/etc/rundeck/telegram.properties", scope = PropertyScope.Project)
  private var telegramProperties: String = _

  @PluginProperty(title = "Include job log", scope = PropertyScope.InstanceOnly)
  private var includeJobLog: Boolean = false
  
  @PluginProperty(title = "Template text", description = "Message template. Susbtitution possible eg ${job.name}", 
                  required = false, scope = PropertyScope.InstanceOnly)
  @TextArea
  private var templateMessage: String = _
  
  @PluginProperty(title = "Message Template", 
                  description = "Name of a FreeMarker template used to generate the notification message. " + 
                                "If unspecified a default message template will be used if it exists.", 
                  required = false, scope = PropertyScope.InstanceOnly)
  private var templateName: String = _

  @PluginProperty(title = "Project Message Template", 
                  description = "Name of a FreeMarker template. This will be the default if none is specified at the project level",
                  required = false, scope = PropertyScope.Project)                  
  private var templateNameProject: String = _

  @PluginProperty(title = "Template directory", 
                  description = "Location to load Freemarker templates from", 
                  required = false, defaultValue = "/var/lib/rundeck/templates", 
                  scope = PropertyScope.Project)
  private var templateDir: String = _

  @PluginProperty(title = "Telegram API base URL", 
                  description = "Base URL of Telegram API", 
                  required = false, defaultValue = "https://api.telegram.org", 
                  scope = PropertyScope.Project)
  private var telegramApiBaseUrl: String = _

  @PluginProperty(title = "Rundeck API key", 
                  description = "Rundeck API key so the plugin can get request job information. Required for job logs", 
                  required = false, scope = PropertyScope.Project)
  private var rundeckApiKey: String = _
 
  @PluginProperty(title = "Proxy Host", 
                  description = "Proxy host for telegram API", 
                  required = false, defaultValue = "", scope = PropertyScope.Project)
  private var proxyHost: String = _

  @PluginProperty(title = "Proxy Port", 
                  description = "Proxy port for telegram API", 
                  required = false, defaultValue = "", scope = PropertyScope.Project)
  private var proxyPort: String = _


  private val isoFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")


  def get[T](name: String, map: JMap[_,_], default: => T = null): T = {
    val value = Option(map.get(name).asInstanceOf[T]).getOrElse(default)
    if (value == null)
      missing(name)
    value
  }


  class InvalidConfigException(message: String) extends Exception(message)
  
  def missing(message: String) = throw new InvalidConfigException(s"Missing configuration: $message")
    
  def ifEmpty(str: String, default: => String) = if (str == null || str == "") default else str
  
  override def postNotification(trigger: String, executionData: JMap[_,_], config: JMap[_,_]): Boolean = {
    println(s"TelegramNotification: $trigger\n\nExecutionData: $executionData\n\nConfig: $config")

    val tids = new TelegramIds(telegramProperties)
  
    val proxy = if (proxyHost != null && proxyHost != "" ) {
      println(s"Using proxy: $proxyHost:$proxyPort")
      val addr = new InetSocketAddress(proxyHost, Option(proxyPort).getOrElse("80").toInt)
      Some(new java.net.Proxy(java.net.Proxy.Type.HTTP, addr))
    }
    else None
  
    val myHttp = new BaseHttp(proxy)
    val httpNoProxy = new BaseHttp()
  
    try {
      val telegramAPi = get[String]("telegramApiBaseUrl", config)
      val botAuthO = tids.lookupBot(ifEmpty(get[String]("botAuthToken", config, ""), 
                                            get[String]("projectBotAuthToken", config, missing("botAuthToken or projectBotAuthToken"))))
     
      val chatO = tids.lookupChat(get[String]("chatId", config))
        
      val templateMessage = get[String]("templateMessage", config, "")
      val templatePath = get[String]("templatePath", config, "")
      val templateProject = get[String]("templateProject", config, "")
      
      val message = buildMessage(executionData)
      
      (botAuthO, chatO) match {
        case (Some(botAuth), Some(chat)) =>
          val telegram = new TelegramMessenger(botAuth, telegramAPi, myHttp)
          val (code, response) = telegram.sendMessage(chat, message)
          val ok = resultOk(code)
          if (ok) {
            println("Telegram mesage sent")
            if (get[String]("includeJobLog", config, "false").toBoolean) {
              getJobLog(chat, executionData, config, httpNoProxy) match {
                case Some((log, fileName)) => 
                  val (code, _) = telegram.sendDocument(chat, log.getBytes, fileName)
                  resultOk(code)
      
                case _ =>
                  false
              }
            }
          }
          else {
            System.err.println(s"Send failed: $code, $response")
          }
          
        case _ =>
          System.err.println(s"Missing auth token or chat Id")
          false
      }
      true
    }
    catch {
      case e @ (_: InvalidConfigException | _: NumberFormatException | _: FileNotFoundException) =>
        System.err.println(s"Failed to send Telegram message - check config: $e")
        e.printStackTrace()        
        false
        
      case e: Throwable =>
        System.err.println(s"Failed to send Telegram message: $e")
        e.printStackTrace()
        false
    }
  }

  private def resultOk(code: Int): Boolean = (code >= 200 && code < 300)
   

  private def getJobLog(chat: Long, executionData: JMap[_,_], config: JMap[_,_], http: BaseHttp): Option[(String, String)] = {
    try {
      val rundeckKey = get[String]("rundeckApiKey", config)
      val context = get[JMap[_,_]]("context", executionData)
      val job = get[JMap[_,_]]("job", context)
      val serverUrl = get[String]("serverUrl", job)
      val execId = get[String]("execid", job).toInt
      val name = get[String]("name", job)
      val fileName = s"${name}_$execId.txt"
      getRundeckLog(execId, rundeckKey, serverUrl, http).map((_, fileName))
    }
    catch {
      case e: Throwable =>
        System.err.println(s"Failed to get execution log: $e")
        None
    }
  }


  private def getRundeckLog(execId: Int, authToken: String, baseUrl: String, http: BaseHttp) = {
    val url = s"${baseUrl}api/6/execution/$execId/output"
    val request = http(url).params(Map("authtoken" -> authToken, "format" -> "text")).asString
    if (resultOk(request.code))
      Some(request.body)
    else
      None
  }

  def buildMessage(executionData: JMap[_,_]): String = {
    println(s"templateDir: $templateDir")
    val templateDirFile = new File(templateDir)
    if (!templateDirFile.exists())
      templateDirFile.mkdir()


    if (templateDirFile.exists())
      fmConfig.setDirectoryForTemplateLoading(templateDirFile);


    for(dateType <- Seq("dateStarted", "dateEnded")) {
      val date = executionData.get(dateType)
      val dateStr = isoFormatter.format(date)
      executionData.asInstanceOf[JMap[String,String]].put(s"${dateType}IsoString", dateStr)
    }

    val template =
      if (ifEmpty(templateMessage, "") != "") {
        val stringLoader = new StringTemplateLoader()
        stringLoader.putTemplate("message", templateMessage)
        fmConfig.setTemplateLoader(stringLoader)
        fmConfig.getTemplate("message")
      }
      else if (ifEmpty(templateName, "") != "") {
          fmConfig.getTemplate(templateName)
      }
      else if (ifEmpty(templateNameProject, "") != "") {
          fmConfig.getTemplate(templateNameProject)
      }
      else {
        throw new InvalidConfigException("None of templateMessage, templateName, templateNameProject set")
      }
    val out = new StringWriter()
    template.process(executionData, out)
    out.toString
  }

}
