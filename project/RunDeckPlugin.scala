package ajr.rundeck

import sbt._
import Keys._
import java.util.jar.Attributes.Name._
import sbt.Defaults._
import sbt.Package.ManifestAttributes

object RunDeckPlugin extends Plugin {
  val rundeckPlugin = TaskKey[File]("rundeckPlugin", "Create a Rundeck plugin")
  val pluginClassNames = TaskKey[String]("Rundeck plugin entrypoint class names")

  val pluginSettings: Seq[Def.Setting[_]] = inTask(rundeckPlugin)(Seq(
    artifactPath := artifactPathSetting(artifact).value,
    cacheDirectory := (cacheDirectory / rundeckPlugin.key.label).value
  )) ++ Seq(
    pluginClassNames := "",
    artifact := artifact.value.copy(name=artifact.value.name + "-classes"),
    artifact in rundeckPlugin := moduleName(Artifact(_)).value,
    packageOptions in rundeckPlugin := Seq(ManifestAttributes(
      "Rundeck-Plugin-File-Version" -> version.value,
      "Rundeck-Plugin-Classnames" -> pluginClassNames.value,
      "Rundeck-Plugin-Archive" -> "true",
      "Rundeck-Plugin-Version" -> "1.1",
      "Rundeck-Plugin-Libs" -> {
        Attributed.data((dependencyClasspath in Runtime).value)
          .map(f => s"lib/${f.name}")
          .filter(_ != "lib/classes")
          .mkString(" ")
      }
    )),
    mappings in rundeckPlugin := {
      Attributed.data((dependencyClasspath in Runtime).value)
        .map(f => (f, (file("lib") / f.name).getPath))
    },
    rundeckPlugin := {
      val artifact = (artifactPath in rundeckPlugin).value
      val packageConf = new Package.Configuration((mappings in rundeckPlugin).value, artifact,
                                                  (packageOptions in rundeckPlugin).value)

      Package(packageConf, (cacheDirectory in rundeckPlugin).value, streams.value.log)
      artifact
    }
  )
}

