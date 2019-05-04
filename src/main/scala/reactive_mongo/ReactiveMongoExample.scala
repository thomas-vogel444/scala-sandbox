package reactive_mongo

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import com.typesafe.config.{ Config, ConfigFactory, ConfigValue, ConfigValueFactory }
import reactivemongo.akkastream.State
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{ DefaultDB, MongoConnection, MongoDriver }
import reactivemongo.bson.{ BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros }

import scala.concurrent.{ Await, Future }
import reactivemongo.akkastream.cursorProducer

import scala.collection.immutable
import scala.concurrent.duration._

object ReactiveMongoExample extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  case class Person(name: String, age: Int)

  // ************************************************************************
  // With reactive mongo
  val mongoUri = "mongodb://localhost:27017/"

  val config = ConfigFactory.empty().withValue("mongo-async-driver.akka.loglevel", ConfigValueFactory.fromAnyRef("INFO"))

  val driver = MongoDriver(config)
  val connection: MongoConnection = MongoConnection.parseURI(mongoUri).map(driver.connection(_)).get
  val database: DefaultDB = Await.result(connection.database("alpakka"), 5 seconds)

  val collection: BSONCollection = database.collection[BSONCollection]("person")

  implicit val numberWriter: BSONDocumentWriter[Person] = Macros.writer[Person]
  implicit val numberReader: BSONDocumentReader[Person] = Macros.reader[Person]

  // ************************************************************************
  val dropCollection = collection.drop(failIfNotFound = false)

  // ************************************************************************
  // As Sink
  val sourceNumbers =
    Source(List(Person("Roger", 68), Person("John", 67), Person("Annie", 69), Person("Tom", 31), Person("Laurie", 31), Person("Pau", 31)))

  val insertPersons = sourceNumbers.runWith(Sink.foreach(collection.insert.one[Person]))

  // ************************************************************************
  // As Source
  val query = BSONDocument("age" -> 31)
  val selector = BSONDocument("age" -> 31)

  val mongoSource: Source[Person, Future[State]] =
    collection.find(query).cursor[Person]().documentSource()

  val getPersons = mongoSource.runWith(Sink.seq)

  // ************************************************************************
  // Do stuff
  val runF =
    for {
      _ <- dropCollection
      _ <- insertPersons
      persons <- getPersons
    } yield persons

  val persons: immutable.Seq[Person] = Await.result(runF, 5.seconds)

  persons.foreach(println)
  connection.askClose()(5 second)
  driver.close()
  system.terminate()
}
