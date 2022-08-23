import mill._
import scalalib._
import publish._

import mill.modules.Jvm.JarManifest
trait RundeckPlugin extends JavaModule {
  def pluginVersion: T[String]
  def pluginClassNames: T[Seq[String]]

  def manifest = T{
    super.manifest().add(
      "Manifest-Version" -> "1.0",
      "Rundeck-Plugin-File-Version" -> pluginVersion(),
      "Rundeck-Plugin-Classnames" -> pluginClassNames().mkString(","),
      "Rundeck-Plugin-Archive" -> "true",
      "Rundeck-Plugin-Version" -> "1.2",
      "Rundeck-Plugin-Description" -> "Sends job notifications via Telegram",
      "Rundeck-Plugin-URL" -> "https://github.com/ajrnz/rundeck-telegram-plugin/",
      "Rundeck-Plugin-Author" -> "Andrew Richards",
      "Rundeck-Plugin-Name" -> "Rundeck Telegram Plugin"
    )
  }

  def prependShellScript = ""
}


object plugin extends ScalaModule with RundeckPlugin {
  def scalaVersion = "2.12.11"

  def compileIvyDeps = Agg(
    ivy"org.rundeck:rundeck-core:3.0.26-20190829"
  )

  def ivyDeps = Agg(
    ivy"org.freemarker:freemarker:2.3.19",
    ivy"org.scalaj::scalaj-http:2.4.2",
    ivy"com.vdurmont:emoji-java:4.0.0"
  )

  def pluginClassNames = Seq("ajr.rundeck.telegram.TelegramNotificationPlugin")
  def pluginVersion = "1.1.4"
}
