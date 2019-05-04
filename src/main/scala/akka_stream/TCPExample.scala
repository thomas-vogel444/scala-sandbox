package akka_stream

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Framing, Source, Tcp }
import akka.util.ByteString

import scala.concurrent.Future
import scala.io._

object TCPExample extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  // ****************************************************************
  val connections: Source[Tcp.IncomingConnection, Future[Tcp.ServerBinding]] =
    Tcp().bind("127.0.0.1", 8888)

  val x: Future[Done] = connections.runForeach { connection =>
    println(s"New connection from: ${connection.remoteAddress}")

    val echo =
      Flow[ByteString]
        .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
        .map(_.utf8String)
        .map(_ + "!!!\n")
        .map(ByteString(_))

    connection.handleWith(echo)
  }
  // ****************************************************************
  val connection = Tcp().outgoingConnection("127.0.0.1", 8888)

  val replParser =
    Flow[String]
      .takeWhile(_ != "q")
      .concat(Source.single("BYE"))
      .map(elem => ByteString(s"$elem\n"))

  val repl =
    Flow[ByteString]
      .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
      .map(_.utf8String)
      .map(text => println("Server: " + text))
      .map(_ => StdIn.readLine("> "))
      .via(replParser)

  val connected = connection.join(repl).run()

  // ****************************************************************
  x.onComplete(_ => system.terminate())
}
