import uk.gov.nationalarchives.tdr.export.api.ApiQueries
import uk.gov.nationalarchives.tdr.export.s3.S3Download

import scala.util.{Failure, Success}

object DownloadFiles extends App {

  val consignmentId = sys.env("CONSIGNMENT_ID").toInt
  val fileDetails = ApiQueries.getFiles(consignmentId)

  fileDetails match {
    case Success(files) => S3Download.downloadFiles(files, consignmentId)
    case Failure(e) => throw e
  }
}
