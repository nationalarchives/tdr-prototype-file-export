import java.nio.file.Paths
import java.util.UUID
import java.util.function.Consumer

import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

object ExportZip extends App {
  println("In export zip app")

  val archiveFileInput = sys.env.get("ARCHIVE_FILEPATH")

  val archivePath = archiveFileInput match {
    case Some(path) => Paths.get(path)
    case None => throw new IllegalArgumentException("Missing environment variable 'ARCHIVE_FILEPATH'")
  }

  val client = S3Client.create

  val objectKey = s"transfer-${UUID.randomUUID}.tar.gz"

  val consumer: Consumer[PutObjectRequest.Builder] = (requestBuilder: PutObjectRequest.Builder) => {
    requestBuilder
      .bucket("tdr-prototype-export")
      .key(objectKey)
  }

  client.putObject(consumer, archivePath)
}
