package spray_json

import spray.json.DefaultJsonProtocol
import spray.json._
import scala.io.Source

object SprayJsonExample extends DefaultJsonProtocol {

  def main(args: Array[String]): Unit = {

    case class JobResponse(builds: List[Build])
    case class Build(fullDisplayName: String, actions: List[Action])

    case class BuildResponse(actions: List[Action])
    case class Action(_class: Option[String], causes: Option[List[Cause]])
    case class Cause(_class: String)

    implicit val causeFormat = jsonFormat1(Cause)
    implicit val actionFormat = jsonFormat2(Action)
    implicit val buildResponseFormat = jsonFormat1(BuildResponse.apply)

    implicit val buildFormat = jsonFormat2(Build)
    implicit val jobResponseFormat = jsonFormat1(JobResponse.apply)

    val json: String = Source.fromResource("job.json").getLines().mkString

    val jsonObject: JsValue = json.parseJson

    println(jobResponseFormat.read(jsonObject).builds.head.actions.find(_._class.contains("hudson.model.CauseAction")))

  }
}
