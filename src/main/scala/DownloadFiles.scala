import java.nio.file.{Path, Paths}

import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest

object DownloadFiles extends App {

  val client = S3Client.create
  val objectRequest = GetObjectRequest.builder.bucket("tdr-files").key("tmp-play-app/MyPlainTextCat.txt").build
  val outputPath: Path = Paths.get("/tmp/tdr-export/some-cat.txt")
  val response = client.getObject(objectRequest, outputPath)

  println(response.lastModified)
}
