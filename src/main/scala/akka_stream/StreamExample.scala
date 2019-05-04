package akka_stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep, RunnableGraph, Sink, Source }

import scala.collection.immutable
import scala.concurrent.Future

object StreamExample {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val source: Source[Int, NotUsed] =
      Source(List(1, 2, 3, 4, 5))

    val stream: RunnableGraph[(Future[immutable.Seq[Int]], Future[Int])] =
      source
        .alsoToMat(Sink.takeLast(3))(Keep.right)
        .toMat(Sink.fold(0)(_ + _))(Keep.both)

    val (x, y) = stream.run()

    Future.sequence(Seq(x, y))
      .onComplete { t =>
        println(t)
        system.terminate()
      }

  }
}
