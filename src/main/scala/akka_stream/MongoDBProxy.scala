package akka_stream

import java.nio.ByteBuffer

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source, Tcp}
import akka.util.ByteString
import akka_stream.MongoDBWireProtocol.{ClientRequest, parseMongoWireProtocol}
import org.bson.conversions.Bson

import scala.concurrent.Future

object MongoDBWireProtocol {

  case class ClientRequest(
    messageLength: Int,
    requestID: Int,
    responseTo: Int,
    opCode: Int)

  def toInt(array: Array[Byte]): Int = {
    val bb = ByteBuffer.wrap(array)
    bb.getInt
  }

  def parseMongoWireProtocol(byteString: ByteString) = {
    val bb: ByteBuffer = byteString.asByteBuffer

    val messageLengthBytes = new Array[Byte](4)
    bb.get(messageLengthBytes)

    val requestIDBytes = new Array[Byte](4)
    bb.get(requestIDBytes)

    val responseToBytes = new Array[Byte](4)
    bb.get(responseToBytes)

    val opCodeBytes = new Array[Byte](4)
    bb.get(opCodeBytes)

    //    val remaining: Int = bb.remaining()
    val messageLength: Int = toInt(messageLengthBytes.reverse)
    val requestID = toInt(requestIDBytes.reverse)
    val responseTo = toInt(responseToBytes.reverse)
    val opCode = toInt(opCodeBytes.reverse)

    println(s"messageLength: ${intToHexString(messageLength)}")
    println(s"requestID: ${intToHexString(requestID)}")
    println(s"responseTo: ${intToHexString(responseTo)}")
    println(s"opCode: ${intToHexString(opCode)}")

    ClientRequest(
      messageLength = messageLength,
      requestID = requestID,
      responseTo = responseTo,
      opCode = opCode)
  }

  def intToHexString(int: Int): String = {
    val bb = ByteBuffer.allocate(4)
    bb.putInt(int)
    bb.flip()

    val array: Array[Byte] = bb.array()
    array.map(byte => byte.toHexString).mkString(" ")
  }
}

object MongoDBProxy extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val connections: Source[Tcp.IncomingConnection, Future[Tcp.ServerBinding]] =
    Tcp().bind("127.0.0.1", 8888)

  val mongoConnection: Flow[ByteString, ByteString, Future[Tcp.OutgoingConnection]] =
    Tcp().outgoingConnection("127.0.0.1", 27017)

  val flow: Flow[ByteString, ByteString, Future[Tcp.OutgoingConnection]] =
    mongoConnection.map { byteString =>
      val message: ClientRequest = parseMongoWireProtocol(byteString)

      println(s"Message: $message")
      byteString
    }

  Bson

  val x: Future[Done] =
    connections.runForeach { connection =>

      val con: Tcp.IncomingConnection = connection

      val z: Future[Tcp.OutgoingConnection] =
        connection.handleWith(flow)

      z
    }

  x.onComplete(_ => system.terminate())

}
