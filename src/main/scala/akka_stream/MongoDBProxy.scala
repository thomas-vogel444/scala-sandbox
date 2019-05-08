package akka_stream

import java.nio.ByteOrder

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, Tcp}
import akka.util.ByteString
import akka.util.ByteString.{ByteString1, ByteString1C, ByteStrings}
import akka_stream.ByteBufferUtils.getBSONCString

import scala.concurrent.Future
import scala.util.{Random, Try}

object Audit {

  def shouldAudit(byteString: ByteString): Boolean = {

    // decoding Mongo Wire format
    val t = Try {

      val bb = byteString match {
        case byteStrings: ByteStrings => byteStrings.asByteBuffer
        case byteString1: ByteString1 => byteString1.asByteBuffer
        case byteString1C: ByteString1C => byteString1C.asByteBuffer
      }

      bb.order(ByteOrder.LITTLE_ENDIAN)

      // MsgHeader
      val messageLength = bb.getInt()
      val requestID = bb.getInt()
      val responseTo = bb.getInt()
      val opCode = bb.getInt()

      // OP_MSG specifics
      val flagBits = bb.getInt()

      // Sections1
      val s1_kind = bb.get() // Assume it is equal to 0 == a single BSON object
      val s1_size = bb.getInt()

      // Bson document
      val e1_type = bb.get()
      val e1_name = getBSONCString(bb)

      e1_name match {
        case "drop" => true
        case _ => false
      }
    }

    t.getOrElse(false)
  }

  def audit(byteString: ByteString, id: Int): Unit = {
    println(s"Dropping tables, now are you mister $id?")
  }

  def auditSink(id: Int): Sink[ByteString, NotUsed] =
    Flow[ByteString]
      .filter(shouldAudit)
      .to(Sink.foreach(byteString=> audit(byteString, id)))

}

object MongoDBProxy extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val connections: Source[Tcp.IncomingConnection, Future[Tcp.ServerBinding]] =
    Tcp().bind("127.0.0.1", 8888)

  def mongoConnection: Flow[ByteString, ByteString, Future[Tcp.OutgoingConnection]] =
    Tcp().outgoingConnection("127.0.0.1", 27017)

  connections
    .runForeach {
      _.handleWith {
        val id = Random.nextInt()

        Flow[ByteString].alsoTo(Audit.auditSink(id)).via(mongoConnection)
      }
    }
    .onComplete(_ => system.terminate())
}
