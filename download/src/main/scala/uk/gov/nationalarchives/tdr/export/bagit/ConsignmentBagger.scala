package uk.gov.nationalarchives.tdr.export.bagit

import java.nio.file.Path

import uk.gov.nationalarchives.tdr.export.api.Consignment

object ConsignmentBagger {
  def saveBag(consignment: Consignment, filesDirectory: Path, outputDirectory: Path): Unit = {

    val bagger = new Bagger(filesDirectory, outputDirectory)
    bagger.saveMandatoryFiles(extractTopLevelMetadata(consignment))
    bagger.addCustomMetadataCsv("client-metadata.csv", generateClientSideMetadata(consignment))
    bagger.validate()
  }

  private def extractTopLevelMetadata(consignment: Consignment): Map[String, String] = {
    val bagSize = consignment.files.map(f => f.fileSize).sum

    Map(
      // In Beta, we should get the version programatically
      "Bag-Creator" -> "TDR Export 0.1",
      "Bag-Size" -> s"$bagSize B",
      "Consignment-Identifier" -> consignment.id.toString,
      "Consignment-TransferringBodyName" -> consignment.transferringBody,
      "Consignment-Series" -> consignment.series.name
    )
  }

  private def generateClientSideMetadata(consignment: Consignment): Seq[Seq[Any]] = {
    val headerRow = Seq("file", "date_last_modified", "sha256", "filesize")
    val clientMetadata = consignment.files.map(file => {
      val pathInBag = s"data/${file.path}"
      Seq(pathInBag, file.lastModifiedDate, file.fileStatus.clientSideChecksum, file.fileSize)
    })
    headerRow +: clientMetadata
  }
}
