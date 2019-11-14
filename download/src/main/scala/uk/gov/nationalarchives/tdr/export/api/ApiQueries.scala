package uk.gov.nationalarchives.tdr.export.api

import java.time.Instant

import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

object ApiQueries {

  @tailrec
  def getConsignmentFiles(
                           consignmentId: Int,
                           hasNextPage: Boolean = true,
                           currentCursor: String = "",
                           files: Seq[File] = List()): Seq[File] = {

    val apiClient = new ApiClient

    if(hasNextPage) {
      val connectionResults = getConsignmentFilesPage(consignmentId, currentCursor, apiClient)
      val response = connectionResults.get
      val filesAccumulator: Seq[File] = response.edges.map(e => e.node) ++ files
      getConsignmentFiles(consignmentId, response.pageInfo.hasNextPage, response.pageInfo.endCursor, filesAccumulator)
    } else {
      files
    }
  }

  private def getConsignmentFilesPage(consignmentId: Int, currentCursor: String, apiClient: ApiClient): Try[Connections] = {
    val batchSize = sys.env.getOrElse("CONSIGNMENT_FILES_BATCH_SIZE", 1000)
    val paginatedQuery =
        s"""query { getConsignment(id: $consignmentId) {
            keySetConnections(limit: $batchSize, currentCursor: "$currentCursor") {
              edges {
                node {id, path, fileSize, lastModifiedDate, fileStatus { clientSideChecksum } }
              }
              pageInfo {
                hasNextPage,
                endCursor
              }
            }
    }}""".stripMargin

    val responseBody = apiClient.sendQueryToApi(paginatedQuery)
    val apiResponse: Either[circe.Error, GetFileConnectionsResponse] = decode[GetFileConnectionsResponse](responseBody)

    apiResponse match {
      case Right(connectionsResponse) => Success(connectionsResponse.data.getConsignment.keySetConnections)
      case Left(e) => Failure(e)
    }
  }

  def getConsignmentInfo(consignmentId: Int): Try[Consignment] = {
    val apiClient = new ApiClient

    val consignmentInfoQuery =
      s"""query { getConsignment(id: $consignmentId) {
         |  id,
         |  transferringBody,
         |  series { name }
         |} }""".stripMargin

    val responseBody = apiClient.sendQueryToApi(consignmentInfoQuery)
    val apiResponse: Either[circe.Error, GetConsignmentResponse] = decode[GetConsignmentResponse](responseBody)

    apiResponse match {
      case Right(consignmentResponse) => Success(consignmentResponse.data.getConsignment)
      case Left(e) => Failure(e)
    }
  }
}

case class GetConsignmentResponse(data: GetConsignmentResponseData)
case class GetConsignmentResponseData(getConsignment: Consignment)
case class Consignment(id: Int, transferringBody: String, series: Series)
case class File(id: String, path: String, fileSize: Long, lastModifiedDate: Instant, fileStatus: FileStatus)
case class FileStatus(clientSideChecksum: String)
case class Series(name: String)

case class GetFileConnectionsResponse(data: GetFileConnectionsResponseData)
case class GetFileConnectionsResponseData(getConsignment: ConsignmentFileConnections)
case class ConsignmentFileConnections(keySetConnections: Connections)
case class Connections(pageInfo: PageInfo, edges: Seq[Edge])
case class PageInfo(endCursor: String, hasNextPage: Boolean)
case class Edge(node: File)
