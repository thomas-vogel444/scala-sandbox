package akka_stream

import java.nio.ByteOrder

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep, Sink, Source, Tcp }
import akka.util.ByteString
import akka_stream.ByteBufferUtils.getBSONCString
import akka_stream.MongoWireParser.mongoBS

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

object Audit {

  def shouldAudit(byteString: ByteString): Boolean = {
    // decoding Mongo Wire format
    val t = Try {
      println(s"Bytestring: ${byteString.map(_.toHexString).mkString}")

      println("Roger1")
      val bb = mongoBS.asByteBuffer
      println("Roger2")

      println(s"bb.hasArray: ${bb.hasArray}")

      bb.order(ByteOrder.LITTLE_ENDIAN)

      // MsgHeader
      val messageLength = bb.getInt()
      println(s"messageLength: $messageLength")

      val requestID = bb.getInt()
      println(s"requestID: $requestID")

      val responseTo = bb.getInt()
      println(s"responseTo: $responseTo")

      val opCode = bb.getInt()
      println(s"opCode: $opCode")

      // OP_MSG specifics
      val flagBits = bb.getInt()
      println(s"flagBits: $flagBits")

      // Sections1
      val s1_kind = bb.get() // Assume it is equal to 0 == a single BSON object
      println(s"s1_kind: $s1_kind")
      val s1_size = bb.getInt()
      println(s"s1_size: $s1_size")

      // Bson document
      val e1_type = bb.get()
      println(s"e1_type: $e1_type")
      val e1_name = getBSONCString(bb)
      println(s"e1_name: $e1_name")

      e1_name match {
        case "drop" => true
        case _ => false
      }
    }

    t match {
      case Success(value) => println("Roger that!")
      case Failure(exception) => println(s"Booh: $exception")
    }

    t.getOrElse(false)
  }

  def audit(byteString: ByteString): Unit = {
    println(byteString)
  }

  val auditSink: Sink[ByteString, NotUsed] =
    Flow[ByteString]
      .filter(byteString => shouldAudit(byteString))
      .to(Sink.foreach(audit))

}

object MongoDBProxy extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val connections: Source[Tcp.IncomingConnection, Future[Tcp.ServerBinding]] =
    Tcp().bind("127.0.0.1", 8888)

  val mongoConnection: Flow[ByteString, ByteString, Future[Tcp.OutgoingConnection]] =
    Tcp().outgoingConnection("127.0.0.1", 27017)

  connections
    .runForeach {
      _.handleWith {
        val x: Flow[ByteString, ByteString, Future[Tcp.OutgoingConnection]] =
          //          mongoConnection
          mongoConnection.alsoTo(Audit.auditSink)
        x
      }
    }
    .onComplete(_ => system.terminate())
}
