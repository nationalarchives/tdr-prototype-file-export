import java.nio.file.Paths
import java.util.UUID
import java.util.function.Consumer

import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

object ExportZip extends App {
  println("In export zip app")

  val archivePath = Paths.get(sys.env("ARCHIVE_FILEPATH"))
  val exportBucket = sys.env.getOrElse("EXPORT_BUCKET", "tdr-prototype-export")

  val client = S3Client.create
  val transferredFileName = s"transfer-${UUID.randomUUID}.tar.gz"

  val consumer: Consumer[PutObjectRequest.Builder] = (requestBuilder: PutObjectRequest.Builder) => {
    requestBuilder
      .bucket(exportBucket)
      .key(transferredFileName)
  }

  println(s"Transferring file '$transferredFileName'")
  client.putObject(consumer, archivePath)
  println("File transferred")
}
