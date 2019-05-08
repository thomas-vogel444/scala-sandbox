package akka_stream

import java.nio.ByteBuffer

import akka.util.ByteString
import org.apache.commons.codec.binary.Hex

object ByteStringExample extends App {

  val bb = ByteBuffer.allocate(4)
  bb.putInt(24124)
  //  bb.putInt(24124124)
  //  bb.putInt(24)
  //  bb.putInt(2124)
  //  bb.putInt(2124)
  bb.flip()

  val byteString = ByteString.fromByteBuffer(bb)
  val s = byteString.map(byte => byte.toHexString).mkString(" ")
  println(s)
  byteString.foreach(byte => println(byte.toHexString))

  println(s"byteString1 length: ${byteString.length}")

  val bb2 = byteString.asByteBuffer

  val array: Array[Byte] = new Array[Byte](byteString.length)
  bb2.get(array)

  println(Hex.encodeHexString(array))

  bb2.flip()
  (1 to byteString.length).foreach { x =>
    val byte2: Byte = bb2.get()

    println(byte2.toHexString)

  }

}
