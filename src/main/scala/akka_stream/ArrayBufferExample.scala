package akka_stream

import scala.collection.mutable.ArrayBuffer

object ArrayBufferExample extends App {

  val arrayBuffer = new ArrayBuffer[Byte]()
  arrayBuffer.append(1.toByte)
  arrayBuffer.append(2.toByte)
  arrayBuffer.append(3.toByte)
  arrayBuffer.append(4.toByte)

  println(arrayBuffer.size)

  arrayBuffer.foreach(println(_))

}
