package uk.gov.nationalarchives.tdr.export.bagit

import java.io
import java.io.{FileInputStream, FileOutputStream, PrintWriter}
import java.nio.file.Path

import com.github.tototoshi.csv.CSVWriter
import gov.loc.repository.bagit.creator.BagCreator
import gov.loc.repository.bagit.hash.{StandardSupportedAlgorithms, SupportedAlgorithm}
import gov.loc.repository.bagit.reader.BagReader
import gov.loc.repository.bagit.verify.BagVerifier
import gov.loc.repository.bagit.writer.BagWriter
import org.apache.commons.codec.digest.DigestUtils

import scala.jdk.CollectionConverters._

class Bagger(dataDirectory: Path, bagDirectory: Path) {

  // Include all files because we can assume that TDR has already helped the user to choose the correct files to
  // preserve
  private val includeHiddenFiles = false
  private val checksumAlgorithm: SupportedAlgorithm = StandardSupportedAlgorithms.SHA256

  def saveMandatoryFiles(topLevelMetadata: Map[String, String]): Unit = {
    val bag = BagCreator.bagInPlace(
      dataDirectory,
      List(checksumAlgorithm).asJava,
      includeHiddenFiles
    )

    for ((key, value) <- topLevelMetadata) bag.getMetadata.add(key, value)

    println(s"Saving bagit bag to $bagDirectory")
    BagWriter.write(bag, bagDirectory)
  }

  def addCustomMetadataCsv(fileName: String, csvData: Seq[Seq[Any]]): Unit = {
    val csvFilePath = bagDirectory.resolve(fileName)
    val csvFile = new io.File(csvFilePath.toString)

    writeCustomMetadataCsv(csvFile, csvData)
    updateTagManifests(csvFile)
  }

  def validate(): Unit = {
    println("Validating bag")
    val bagReader = new BagReader
    val bagToVerify = bagReader.read(bagDirectory)
    val ignoreHiddenFiles = !includeHiddenFiles

    val verifier = new BagVerifier
    verifier.isComplete(bagToVerify, ignoreHiddenFiles)
    println("Bag is complete")
    verifier.isValid(bagToVerify, ignoreHiddenFiles)
    println("Bag is valid")

    verifier.close()
  }

  private def writeCustomMetadataCsv(csvFile: io.File, csvData: Seq[Seq[Any]]): Unit = {
    println(s"Saving custom metadata file '${csvFile.getAbsolutePath}'")
    val csvWriter = CSVWriter.open(csvFile)
    csvWriter.writeAll(csvData)
    csvWriter.close()
  }

  private def updateTagManifests(customTagFile: io.File): Unit = {
    println(s"Adding custom metadata file '${customTagFile.getName}' to tag manifests")

    val inputStream = new FileInputStream(customTagFile)
    val clientMetadataChecksum = DigestUtils.sha256Hex(inputStream)

    val tagManifestPath = bagDirectory.resolve("tagmanifest-sha256.txt")
    val tagManifest = new io.File(tagManifestPath.toString)
    val appendMode = true
    val printWriter = new PrintWriter(new FileOutputStream(tagManifest, appendMode))
    printWriter.append(s"$clientMetadataChecksum  ${customTagFile.getName}\n")
    printWriter.close()
  }
}
