import java.io.FileInputStream
import java.nio.file.Paths
import java.util.UUID

import com.jcraft.jsch._

object ExportZip extends App {
  println("In export zip app")

  val archiveFileInput = sys.env.get("ARCHIVE_FILEPATH")

  val archivePath = archiveFileInput match {
    case Some(path) => Paths.get(path)
    case None => throw new IllegalArgumentException("Missing environment variable 'ARCHIVE_FILEPATH'")
  }

  val transferredFileName = s"transfer-${UUID.randomUUID}.tar.gz"

  val session = connectSession()

  println("Opening SFTP channel")
  val channel = session.openChannel("sftp")
  channel.connect()
  val sftpChannel = channel.asInstanceOf[ChannelSftp]
  println("SFTP channel opened")

  println("Contents:")
  println(sftpChannel.ls("/tdr-prototype-ftp"))

  val inputStream = new FileInputStream(archivePath.toString)

  println("Transferring file")
  sftpChannel.put(inputStream, transferredFileName)
  println("File transferred")

  // TODO: Clean up resources even if exception thrown
  println("Disconnecting")
  sftpChannel.exit()
  session.disconnect()

  def connectSession(): Session = {
    var attemptCount = 0
    val maxAttempts = 40

    val timeoutMs = 1000

    val privateKey = sys.env.getOrElse("SSH_KEY_FILE", "~/.ssh/id_rsa")

    // The connection to the FTP server is very flaky. It connects about 1 time in 10. Retrying only seems to work if
    // you recreate the JSch object AND sleep for a few seconds between connections.
    while (attemptCount < maxAttempts) {
      println(s"Connecting session - attempt $attemptCount")

      try {
        val jsch = new JSch
        jsch.addIdentity(privateKey)

        val port = 22
        val session = jsch.getSession("smh-test", "s-ad045f92709e441a8.server.transfer.eu-west-2.amazonaws.com", 22)
        val config = new java.util.Properties
        // TODO: Set host key rather than skipping key checking
        config.put("StrictHostKeyChecking", "no")
        session.setConfig(config)

        session.connect(timeoutMs)

        return session
      } catch {
        case e: JSchException => {
          println(e)
          attemptCount += 1
          Thread.sleep(5000)
        }
      }
    }

    throw new RuntimeException(s"Failed to connect to FTP server after $maxAttempts attempts")
  }
}
