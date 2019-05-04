package akka_http.service

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ StatusCode, StatusCodes, Uri }
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.Random

object SimpleService {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route: Route =
      path(IntNumber) { int =>

        extractUri { uri =>
          Thread.sleep(5000)

          if (Random.nextBoolean()) {
            println(s"[${LocalDateTime.now}] --> ${uri.authority.host}: Success for $int!")
            complete(StatusCodes.NoContent)
          } else {
            println(s"[${LocalDateTime.now}] --> ${uri.authority.host}: Failure for $int...")
            complete(StatusCodes.InternalServerError)
          }
        }
      }

    val binding = Http().bindAndHandle(route, "0.0.0.0", 4000)
    StdIn.readLine()
    binding.flatMap(_.unbind()).onComplete(_ => system.terminate())

  }

}
