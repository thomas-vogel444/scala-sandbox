package akka_stream

import java.nio.{ ByteBuffer, ByteOrder }

import akka.util.ByteString
import akka_stream.ByteBufferUtils.{ getBSONCString, getBSONString }
import org.apache.commons.codec.binary.Hex

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks

object ByteBufferUtils {
  // Modifies the position of the byte buffer
  def getBSONCString(byteBuffer: ByteBuffer): String = {
    val arrayBuffer = new ArrayBuffer[Byte](20)
    Breaks.breakable {
      while (true) {
        val nextByte: Byte = byteBuffer.get()

        if (nextByte == 0.toByte) {
          Breaks.break()
        } else {
          arrayBuffer.append(nextByte)
        }
      }
    }

    new String(arrayBuffer.toArray)
  }

  def getBSONString(byteBuffer: ByteBuffer): String = {
    val stringSize = byteBuffer.getInt()
    println(s"stringSize: $stringSize")

    println(stringSize.toHexString)
    val byteArray = new Array[Byte](stringSize)
    byteBuffer.get(byteArray)

    byteBuffer.get() // Remove the trailing null byte

    new String(byteArray)
  }
}

object MongoWireParser extends App {

  // test data coming from Wireshark
  val hexadecimalStream = "580000001800000000000000dd0700000000000000430000000264726f7000020000007800036c736964001e0000000569640010000000044439b328ec69448ba038420ba644191900022464620005000000746573740000"
  //  val hexadecimalStream = "4c0000001500000000000000dd0700000000000000370000000169734d617374657200000000000000f03f01666f725368656c6c00000000000000f03f022464620005000000746573740000"
  val mongoBS: ByteString = ByteString(Hex.decodeHex(hexadecimalStream))

  // decoding Mongo Wire format
  val bb = mongoBS.toByteBuffer
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
  println(s"bb position: ${bb.position()}")
  val e1_name = getBSONCString(bb)
  println(s"bb position: ${bb.position()}")

  //  val e1_length = bb.getInt()
  //  println(s"bb position: ${bb.position()}")

  val e1_value = getBSONString(bb)
  println(s"bb position: ${bb.position()}")

  // ******************************** OUTPUT **************************************
  println(s"messageLength: $messageLength")
  println(s"requestID: $requestID")
  println(s"responseTo: $responseTo")
  println(s"opCode: $opCode")
  println(s"flagBits: $flagBits")
  println(s"s1_kind: $s1_kind")
  println(s"s1_size: $s1_size")
  println(s"e1_type: $e1_type")
  println(s"e1_name: $e1_name")
  println(s"e1_value: $e1_value")
}
