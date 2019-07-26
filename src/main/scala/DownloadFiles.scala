import java.nio.file.{Path, Paths}
import java.util.function.Consumer

import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, ListObjectsV2Request}
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable

object DownloadFiles extends App {

  val client = S3Client.create

//  val consumer: Consumer[ListObjectsV2Request.Builder] = (requestBuilder: ListObjectsV2Request.Builder) => {
//    requestBuilder
//      .bucket("tdr-files")
//      .prefix("tmp-play-app")
//  }
//  val response: ListObjectsV2Iterable = client.listObjectsV2Paginator(consumer)
//
//  println("S3 objects:")
//  response.contents.stream.forEach(s3Object => {
//    // Includes folder
//    // Includes key prefix
//    println(s3Object.key())
//  })



  val objectRequest = GetObjectRequest.builder.bucket("tdr-files").key("tmp-play-app/some-nested-folder/").build
  val outputPath: Path = Paths.get("/tmp/tdr-export/some-nested-folder/")
  val response = client.getObject(objectRequest, outputPath)

  println(response.lastModified)
}
