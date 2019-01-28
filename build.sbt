name := "rundeck-telegram-plugin"

version := "1.0.7"

scalaVersion := "2.12.8"

crossPaths := false

ajr.rundeck.RunDeckPlugin.pluginSettings

exportJars := true

libraryDependencies ++= Seq(
  "org.rundeck" % "rundeck-core" % "2.6.2" % "provided",
  "org.freemarker" % "freemarker" % "2.3.19",
  "org.scalaj" %% "scalaj-http" % "2.4.1"
)

pluginClassNames := "ajr.rundeck.telegram.TelegramNotificationPlugin"
