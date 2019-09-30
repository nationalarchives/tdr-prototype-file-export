package uk.gov.nationalarchives.tdr.export.api

import java.io.ByteArrayInputStream

import ca.ryangreen.apigateway.generic.{GenericApiGatewayClientBuilder, GenericApiGatewayRequestBuilder, GenericApiGatewayResponse}
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.http.HttpMethodName
import com.amazonaws.regions.{Region, Regions}
import io.circe.generic.auto._
import io.circe.syntax._

import scala.jdk.CollectionConverters._

class ApiClient {

  def sendQueryToApi(queryString: String): String = {
    case class Query(query: String)

    val query = Query(queryString)

    val apiGatewayClient = new GenericApiGatewayClientBuilder()
      .withClientConfiguration(new ClientConfiguration)
      .withCredentials(new DefaultAWSCredentialsProviderChain)
      .withEndpoint(sys.env("GRAPHQL_SERVER"))
      .withRegion(Region.getRegion(Regions.EU_WEST_2))
      .build

    val headers = Map("Content-Type" -> "application/json").asJava

    val requestBody = query.asJson.toString()
    val request = new GenericApiGatewayRequestBuilder()
      .withBody(new ByteArrayInputStream(requestBody.getBytes))
      .withHttpMethod(HttpMethodName.POST)
      .withHeaders(headers)
      .withResourcePath(sys.env("GRAPHQL_PATH"))
      .build

    val response: GenericApiGatewayResponse = apiGatewayClient.execute(request)

    println("Response:")
    println(response.getHttpResponse.getStatusCode)
    response.getBody
  }
}
