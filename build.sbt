import sbt.Keys.version

val awsSdkConfigFiles = Set(
  "api-2.json",
  "customization.config",
  "docs-2.json",
  "examples-1.json",
  "paginators-1.json",
  "service-2.json",
  "waiters-2.json"
)

lazy val commonSettings = Seq(
  name := "tdr-prototype-export-files",
  version := "0.1",
  scalaVersion := "2.13.0",
  libraryDependencies += "software.amazon.awssdk" % "s3" % "2.7.15",
  assemblyMergeStrategy in assembly := {
    case "META-INF/io.netty.versions.properties" => MergeStrategy.first
    // AWS SDK v2 configuration files - can be discarded
    case PathList(ps@_*) if awsSdkConfigFiles.contains(ps.last) =>
      MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)

lazy val download = (project in file("download"))
  .settings(commonSettings: _*)
  .settings(
    assemblyJarName in assembly := "tdr-download.jar"
  )

lazy val exportZip = (project in file("export-zip"))
  .settings(commonSettings: _*)
  .settings(
    assemblyJarName in assembly := "tdr-export.jar"
  )
