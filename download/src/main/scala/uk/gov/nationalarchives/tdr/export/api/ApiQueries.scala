package uk.gov.nationalarchives.tdr.export.api

import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode

import scala.util.{Failure, Success, Try}

object ApiQueries {
  def getFiles(consignmentId: Int): Try[Seq[File]] = {
    val apiClient = new ApiClient

    val graphqlQuery = s"""query { getConsignment(id: $consignmentId) { files { id, path } } }"""

    val responseBody = apiClient.sendQueryToApi(graphqlQuery)
    val apiResponse: Either[circe.Error, GetConsignmentResponse] = decode[GetConsignmentResponse](responseBody)

    apiResponse match {
      case Right(consignmentResponse) => Success(consignmentResponse.data.getConsignment.files)
      case Left(e) => Failure(e)
    }
  }
}

case class GetConsignmentResponse(data: GetConsignmentResponseData)
case class GetConsignmentResponseData(getConsignment: ConsignmentData)
case class ConsignmentData(files: Seq[File])
case class File(id: String, path: String)
