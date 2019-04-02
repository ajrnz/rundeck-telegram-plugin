import mill._
import scalalib._
import publish._
import JavaModule.manifestFormatter
import java.util.jar.Attributes.Name

import mill.modules.Jvm

trait RundeckPlugin extends JavaModule {
  def pluginVersion: T[String]
  def pluginClassNames: T[Seq[String]]

  def manifest = T {
    val m = new java.util.jar.Manifest()
    val attr =  Seq[(String,String)](
      "Manifest-Version" -> "1.0",
      "Rundeck-Plugin-File-Version" -> pluginVersion(),
      "Rundeck-Plugin-Classnames" -> pluginClassNames().mkString(","),
      "Rundeck-Plugin-Archive" -> "true",
      "Rundeck-Plugin-Version" -> "1.2",
      "Rundeck-Plugin-Description" -> "Sends job notifications via Telegram",
      "Rundeck-Plugin-URL" -> "https://github.com/ajrnz/rundeck-telegram-plugin",
      "Rundeck-Plugin-Author" -> "Andrew Richards",
      "Rundeck-Plugin-Name" -> "Rundeck Telegram Plugin"
   )
    val ma = m.getMainAttributes
    m.getEntries
    attr.foreach{case(k,v) => ma.put(new Name(k),v)}
    m
  }

  def prependShellScript = ""
}


object plugin extends ScalaModule with RundeckPlugin {
  def scalaVersion = "2.12.8"

  def compileIvyDeps = Agg(
    ivy"org.rundeck:rundeck-core:3.0.16-20190223"
  )

  def ivyDeps = Agg(
    ivy"org.freemarker:freemarker:2.3.19",
    ivy"org.scalaj::scalaj-http:2.4.1"
  )

  def pluginClassNames = Seq("ajr.rundeck.telegram.TelegramNotificationPlugin")
  def pluginVersion = "1.1.0"

}
