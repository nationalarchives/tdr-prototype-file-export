import java.nio.file.Paths
import java.util.UUID
import java.util.function.Consumer

import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

object ExportZip extends App {
  println("In export zip app")

  val archiveFilename = "/tmp/tdr-files.tar.gz"
  val archivePath = Paths.get(archiveFilename)

  val client = S3Client.create

  val objectKey = s"transfer-${UUID.randomUUID}.tar.gz"

  val consumer: Consumer[PutObjectRequest.Builder] = (requestBuilder: PutObjectRequest.Builder) => {
    requestBuilder
      .bucket("tdr-prototype-export")
      .key(objectKey)
  }

  client.putObject(consumer, archivePath)
}
