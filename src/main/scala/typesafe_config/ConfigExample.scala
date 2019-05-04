package typesafe_config

import com.typesafe.config.ConfigFactory

object ConfigExample extends App {

  val config = ConfigFactory.defaultApplication().resolve()

  println(config.getString("roger"))
  println(config.getString("myApp.username"))
  println(config.getString("myApp.password"))

}
