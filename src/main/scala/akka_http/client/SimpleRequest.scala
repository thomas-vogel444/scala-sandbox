package akka_http.client

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, StatusCodes, Uri }
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.util.{ Failure, Success }

object SimpleRequest {

  def main(args: Array[String]): Unit = {

    val config = ConfigFactory.defaultApplication()

    implicit val system = ActorSystem("my-system", config)
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    (1 to 20).foreach { int =>
      val request = HttpRequest(uri = Uri(s"http://localhost:4000/$int"))

      Http().singleRequest(request).onComplete {
        case Success(response) =>
          response.status match {
            case StatusCodes.NoContent => println(s"[${LocalDateTime.now}]: Success for $int!")
            case _ => println(s"[${LocalDateTime.now}]: Server misbehaving for $int...")
          }
        case Failure(exception) => println(s"[${LocalDateTime.now}]: Failure with $exception...")
      }

    }

  }
}
