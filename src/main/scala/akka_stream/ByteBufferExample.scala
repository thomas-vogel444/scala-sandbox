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

  val intBytes = ByteBuffer.allocate(12)
  intBytes.putInt(32)
  intBytes.putInt(4224)
  intBytes.putInt(2414244)

  //  intBytes.flip()

  val byteString = ByteString(intBytes)

  println(byteString.length)

  //  val bb = byteString.asByteBuffer
  //  println(bb.getInt)
  //  println(bb.getInt)
  //  println(bb.getInt)
  //  println(byteString.length)
  //  println(bb.position())
  //  println(bb.remaining())
  //  bb.flip()
  //
  //  println(byteString.toString())
  //  bb.putInt(88)
  //  println(byteString.toString())
}
