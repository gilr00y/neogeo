package neogeo

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}

import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonDateTime, conversions}
import org.mongodb.scala.{Completed, MongoClient, MongoCollection, MongoDatabase}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

class GeoData(databaseName: String) {
  val codecRegistry: CodecRegistry = fromRegistries(fromProviders(
    classOf[GeoEntity]
  ), DEFAULT_CODEC_REGISTRY )

  // TODO: Correct mongo string.
val mongoUrl: String = "mongodb://neogeo:neogeo@mongo:27017"
  val mongoClient: MongoClient = MongoClient(mongoUrl)
  val collName = "neogeo"
  val mongoDatabase: MongoDatabase = mongoClient
    .getDatabase(databaseName)
    .withCodecRegistry(codecRegistry)

  val geoColl: MongoCollection[GeoEntity] = mongoDatabase.getCollection(collName)

  def RFC_1123_STR_TO_BSON_TIME(rfcIn: String): BsonDateTime = {
    val format = DateTimeFormatter.RFC_1123_DATE_TIME
    val javaTime = LocalDateTime.parse(rfcIn, format)

    // Must convert to Millis for Bson
    val epochTime = javaTime.toEpochSecond(ZoneOffset.UTC) * 1000
    val bsonTime = new org.bson.BsonDateTime(epochTime)
    bsonTime
  }

  def getGeoEntities(query: conversions.Bson, limit: Int = 1000): Future[Seq[GeoEntity]] = {
    var geos = List[GeoEntity]()
    val p = Promise[Seq[GeoEntity]]
    geoColl.find(query).limit(limit).subscribe(
      (res: GeoEntity) => geos = geos :+ res,
      (e: Throwable) => p.failure(e),
      () => p.success(geos)
    )
    p.future
  }

  def getCollection(collectionName: String): MongoCollection[Document] = {
    mongoDatabase.getCollection(collectionName)
  }

  def insertMany(collection: MongoCollection[Document], documents: Seq[Document]): Future[String] = {
    val p = Promise[String]

    collection.insertMany(documents).subscribe(
      (result: Completed) => p.success(result.toString),
      (e: Throwable) => p.failure(e),
      () => null
    )
    p.future
  }

  def insertOne(collection: MongoCollection[Document], document: Document): Future[String] = {
    val p = Promise[String]
    collection.insertOne(document).subscribe(
      (result: Completed) => p.success(result.toString),
      (e: Throwable) => p.failure(e),
      () => null
    )
    p.future
  }

  // Return batched iterable
  def find(collection: MongoCollection[Document], queryDoc: Document): Future[Seq[Document]] = {
    Future {Seq(Document())}
  }
}
