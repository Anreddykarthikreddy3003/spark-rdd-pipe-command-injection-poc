package com.example.sparkpoc

import org.apache.spark.{SparkConf, SparkContext}
import scala.io.Source
import scala.sys.process._
import java.io.{File, PrintWriter}
import java.nio.file.{Files, Paths}
import java.nio.file.attribute.{PosixFilePermissions, FileAttribute}
import scala.util.{Try, Success, Failure}

object CommandInjectionPoC {
  def main(args: Array[String]): Unit = {
    println("Starting Command Injection PoC with Spark 3.5.5")

    val conf: SparkConf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("SparkCommandInjectionPoC")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.authenticate", "true")
      .set("spark.network.crypto.enabled", "true")
      .set("spark.acls.enable", "true")

    val sc: SparkContext = new SparkContext(conf)

    try {
      // Create a secure temporary file
      val perms = PosixFilePermissions.fromString("rw-------")
      val fileAttributes: FileAttribute[java.util.Set[java.nio.file.attribute.PosixFilePermission]] = 
        PosixFilePermissions.asFileAttribute(perms)
      val tempFile = Files.createTempFile("spark_secure", ".tmp", fileAttributes)
      val outputFile: File = tempFile.toFile

      // Write initial content
      val writer: PrintWriter = new PrintWriter(outputFile)
      writer.write("Initial content\n")
      writer.close()

      // Execute command using ProcessBuilder for better security
      val command: Seq[String] = Seq("sh", "-c", s"whoami >> ${outputFile.getAbsolutePath}")
      val process: ProcessBuilder = Process(command)
      val exitCode: Int = process.!

      if (exitCode == 0) {
        // Read and display the content of the file
        val content: String = Source.fromFile(outputFile).getLines().mkString("\n")
        println(s"File content:\n$content")
      } else {
        println(s"Command execution failed with exit code: $exitCode")
      }

      // Clean up
      outputFile.delete()

    } catch {
      case e: Exception =>
        println(s"Error during execution: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      sc.stop()
    }
  }
}
