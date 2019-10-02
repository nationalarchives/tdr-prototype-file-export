package uk.gov.nationalarchives.tdr.export.s3

import java.nio.file.{Path, Paths}

import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import uk.gov.nationalarchives.tdr.export.api.File

object S3Download {

  private val s3Client = S3Client.create

  private val bucketName = sys.env.getOrElse("INPUT_BUCKET_NAME", "tdr-upload-files-dev")

  def downloadFiles(files: Seq[File], downloadFolderPath: Path, consignmentId: Int): Unit = {
    println(s"Saving files to ${downloadFolderPath.toAbsolutePath}")

    val s3FolderName = sys.env.getOrElse("INPUT_FOLDER_NAME", consignmentId.toString)

    files.foreach(fileDetails => {
      println(s"Downloading file '${fileDetails.id}' to its original path '${fileDetails.path}'")
      download(fileDetails, s3FolderName, downloadFolderPath)
    })
  }

  private def download(fileDetails: File, s3FolderName: String, downloadFolderPth: Path): Unit = {
    val objectRequest = GetObjectRequest.builder
      .bucket(bucketName)
      .key(s"$s3FolderName/${fileDetails.id}")
      .build
    val outputPath: Path = Paths.get(s"$downloadFolderPth/${fileDetails.path}")

    createParentDirectories(outputPath)

    s3Client.getObject(objectRequest, outputPath)
  }

  private def createParentDirectories(path: Path): Unit = {
    new java.io.File(path.getParent.toString).mkdirs
  }
}
