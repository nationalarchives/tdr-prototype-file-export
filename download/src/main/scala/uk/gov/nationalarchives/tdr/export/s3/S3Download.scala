package uk.gov.nationalarchives.tdr.export.s3

import java.nio.file.{Files, Path, Paths}

import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import uk.gov.nationalarchives.tdr.export.api.File

object S3Download {

  private val s3Client = S3Client.create

  private val bucketName = sys.env.getOrElse("INPUT_BUCKET_NAME", "tdr-upload-files-dev")

  def downloadFiles(files: Seq[File], consignmentId: Int): Unit = {
    val s3FolderName = sys.env.getOrElse("INPUT_FOLDER_NAME", consignmentId.toString)

    val tempFolderName = sys.env.get("OUTPUT_DIR")
    val tempFolder = tempFolderName match {
      case Some(folder) => Paths.get(folder)
      case None => Files.createTempDirectory("tdr-export")
    }

    println(s"Saving files to ${tempFolder.toAbsolutePath}")

    files.foreach(fileDetails => {
      println(s"Downloading file '${fileDetails.id}' to its original path '${fileDetails.path}'")
      download(fileDetails, s3FolderName, tempFolder)
    })
  }

  private def download(fileDetails: File, s3FolderName: String, tempFolder: Path): Unit = {
    val objectRequest = GetObjectRequest.builder
      .bucket(bucketName)
      .key(s"$s3FolderName/${fileDetails.id}")
      .build
    val outputPath: Path = Paths.get(s"$tempFolder/${fileDetails.path}")

    createParentDirectories(outputPath)

    s3Client.getObject(objectRequest, outputPath)
  }

  private def createParentDirectories(path: Path): Unit = {
    new java.io.File(path.getParent.toString).mkdirs
  }
}
