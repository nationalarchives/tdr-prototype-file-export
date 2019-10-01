package uk.gov.nationalarchives.tdr.export.api

import java.time.Instant

import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode

import scala.util.{Failure, Success, Try}

object ApiQueries {
  def getConsignment(consignmentId: Int): Try[Consignment] = {
    val apiClient = new ApiClient

    val graphqlQuery =
      s"""query { getConsignment(id: $consignmentId) {
         |  id,
         |  transferringBody,
         |  files { id, path, fileSize, lastModifiedDate, fileStatus { clientSideChecksum } }
         |  series { name }
         |} }""".stripMargin

    val responseBody = apiClient.sendQueryToApi(graphqlQuery)
    val apiResponse: Either[circe.Error, GetConsignmentResponse] = decode[GetConsignmentResponse](responseBody)

    apiResponse match {
      case Right(consignmentResponse) => Success(consignmentResponse.data.getConsignment)
      case Left(e) => Failure(e)
    }
  }
}

case class GetConsignmentResponse(data: GetConsignmentResponseData)
case class GetConsignmentResponseData(getConsignment: Consignment)
case class Consignment(id: Int, transferringBody: String, files: Seq[File], series: Series)
case class File(id: String, path: String, fileSize: Int, lastModifiedDate: Instant, fileStatus: FileStatus)
case class FileStatus(clientSideChecksum: String)
case class Series(name: String)
