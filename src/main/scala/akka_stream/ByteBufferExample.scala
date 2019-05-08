package akka_stream

import java.nio.{ Buffer, ByteBuffer }

import akka.util.ByteString
import org.apache.commons.codec.binary.Hex

object ByteBufferExample extends App {

  /**
   * 2 tyoes of ByteBuffer:
   * - backed by byte arrays on the heap, created either by wrapping [Byte] or allocating memory on the Heap
   * - backed by direct byte buffers, created off-heap by calling allocateDirect
   *
   * 4 concepts:
   * - capacity: total number of bytes between the beginning and the limit
   * - limit: one position after the last byte written by the buffer
   * - position: position one past the last byte written by the buffer
   * - mark: optional bookmark position. Can use reset() to put the position back there.
   *
   * Operations:
   * - duplicate
   * - slice
   * - hasArray
   */

  val bb = ByteBuffer.allocate(22)
  // fill in with "ismaster" as a BSON cstring
  bb.put(105.toByte)
  bb.put(115.toByte)
  bb.put(109.toByte)
  bb.put(97.toByte)
  bb.put(115.toByte)
  bb.put(116.toByte)
  bb.put(101.toByte)
  bb.put(114.toByte)
  bb.put(0.toByte)

  bb.put(23.toByte)
  bb.put(444.toByte)
  bb.put(124.toByte)
  bb.put(114.toByte)
  bb.put(114.toByte)
  bb.put(114.toByte)

  val arrayString = bb.array() // no need to flip!
  val s = new String(arrayString)
  println(s)

  val i = arrayString.indexOf(0.toByte)
  println(s"index: $i")

  val x: Array[Byte] = arrayString.take(i)
  println(x.length)
  println(new String(x))

}
