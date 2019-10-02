package uk.gov.nationalarchives.tdr.export

import java.nio.file.{Files, Path, Paths}

import uk.gov.nationalarchives.tdr.export.api.ApiQueries
import uk.gov.nationalarchives.tdr.export.bagit.ConsignmentBagger
import uk.gov.nationalarchives.tdr.export.s3.S3Download

import scala.util.{Failure, Success}

object ExportPackager extends App {

  private val consignmentId = sys.env("CONSIGNMENT_ID").toInt
  private val consignmentResults = ApiQueries.getConsignment(consignmentId)

  private val downloadedFilesDirectory = tempDirectory(sys.env.get("FILE_DOWNLOAD_DIR"))
  private val bagDirectory = tempDirectory(sys.env.get("BAG_DIR"))

  consignmentResults match {
    case Success(consignment) => S3Download.downloadFiles(consignment.files, downloadedFilesDirectory, consignmentId)
    case Failure(e) => throw e
  }
  val consignment = consignmentResults.get

  ConsignmentBagger.saveBag(consignment, downloadedFilesDirectory, bagDirectory)

  private def tempDirectory(configuredLocation: Option[String]): Path = {
    configuredLocation match {
      case Some(folder) => Paths.get(folder)
      case None => Files.createTempDirectory("tdr-export")
    }
  }
}
