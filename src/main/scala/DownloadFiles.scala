import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.util.function.Consumer

import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, ListObjectsV2Request, S3Object}
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable

object DownloadFiles extends App {

  val client = S3Client.create

  val consumer: Consumer[ListObjectsV2Request.Builder] = (requestBuilder: ListObjectsV2Request.Builder) => {
    requestBuilder
      .bucket("tdr-files")
//      .prefix("tmp-play-app")
  }
  val response: ListObjectsV2Iterable = client.listObjectsV2Paginator(consumer)

  val tempFolder = Files.createTempDirectory("tdr-export")
  println(s"Saving files to ${tempFolder.toAbsolutePath}")

  println("S3 objects:")
  response.contents.stream.forEach(s3Object => {
    if (s3Object.key.endsWith("/")) {
      println(s"Creating directory '${s3Object.key}'")
      createDirectory(s3Object.key, tempFolder)
    } else if (s3Object.size == 0) {
      println(s"Skipping '${s3Object.key}' because size is zero")
    } else {
      println(s"Downloading '${s3Object.key}'")
      download(s3Object, tempFolder.toAbsolutePath)
    }
  })

  private def download(s3Object: S3Object, tempFolder: Path): Unit = {
    val parentPath = Paths.get(s3Object.key).getParent
    if (parentPath != null) createDirectory(parentPath.toString, tempFolder)

    val objectRequest = GetObjectRequest.builder
      .bucket("tdr-files")
      .key(s3Object.key)
      .build
    val outputPath: Path = Paths.get(s"$tempFolder/${s3Object.key}")
    client.getObject(objectRequest, outputPath)
  }

  private def createDirectory(directoryPath: String, tempFolder: Path): Unit = {
    val absolutePath = Paths.get(s"${tempFolder.toAbsolutePath}/$directoryPath")
    new File(absolutePath.toString).mkdirs
  }
}
