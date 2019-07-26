name := "tdr-prototype-export-files"

version := "0.1"

scalaVersion := "2.13.0"

val awsSdkConfigFiles = Set(
  "api-2.json",
  "customization.config",
  "docs-2.json",
  "examples-1.json",
  "paginators-1.json",
  "service-2.json",
  "waiters-2.json"
)

assemblyMergeStrategy in assembly := {
  case "META-INF/io.netty.versions.properties" => MergeStrategy.first
  // AWS SDK v2 configuration files - can be discarded
  case PathList(ps@_*) if awsSdkConfigFiles.contains(ps.last) =>
    MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

libraryDependencies += "software.amazon.awssdk" % "aws-sdk-java" % "2.7.11"