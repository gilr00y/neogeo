package neogeo

import java.util.stream.{Stream => JStream}
import java.util.{List => JList}

import neogeo.JavaHelper.GeoNode
import org.mongodb.scala.bson.BsonArray
import org.mongodb.scala.bson.collection.immutable.{Document => MongoDocument}
import org.mongodb.scala.model.Filters.{geoWithinCenterSphere, geoWithinPolygon}
import org.neo4j.graphdb.{Label, Node}
import org.neo4j.procedure.{Name, Procedure}

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class GeoProcs extends JavaHelper {

  @Procedure(value="neogeo.withinRadius")
  def withinRadius(@Name("latitude") lat: Double,
                     @Name("longitude") lng: Double,
                     @Name("radiusInKm") radius: Double): JStream[GeoNode] = {

    // TODO: Initialize Mongo cx elsewhere - only here bc can't store non-static state.
    val MDB = new GeoData("neogeo")
    val indexQuery = geoWithinCenterSphere("location", lng, lat, radius)
    val geoResults = Await.result(MDB.getGeoEntities(indexQuery), Duration.Inf)
    val geoIds = geoResults.map(_.neo_id)

    val geoLabel = Label.label("TEST")
    val tx = db.beginTx
    try {
      val nodeStream = JStream.of(geoIds.map { geoId: String =>
        new GeoNode(tx.findNode(geoLabel, "_node_id", geoId))
      } : _*)

      nodeStream
    } finally {
//      tx.commit()
    }

  }

  @Procedure(value="neogeo.withinPolygon")
  def withinPolygon(
                     @Name("polygon") polygon: JList[JList[Double]],
                   ): JStream[GeoNode] = {
    // TODO: Initialize Mongo cx elsewhere - only here bc can't store non-static state.
    val MDB = new GeoData("neogeo")
    val indexQuery = geoWithinPolygon("location", polygon.asScala.map(_.asScala))
    val geoResults = Await.result(MDB.getGeoEntities(indexQuery), Duration.Inf)
    val geoIds = geoResults.map(_.neo_id)

    val geoLabel = Label.label("TEST")
    val tx = db.beginTx
    try {
      val nodeStream = JStream.of(geoIds.map { geoId: String =>
        new GeoNode(tx.findNode(geoLabel, "_node_id", geoId))
      } : _*)

      nodeStream
    } finally {
      //      tx.commit()
    }

  }



  @Procedure(value="neogeo.addNode")
  def addNode(
             @Name("node") node: Node
             ): JStream[GeoNode] = {
    val MDB = new GeoData("neogeo")
    val neoId = node.getProperty("_node_id").asInstanceOf[String]
    val lat = node.getProperty("latitude").asInstanceOf[Double]
    val lng = node.getProperty("longitude").asInstanceOf[Double]
    val nodeInsertQuery = MongoDocument(
      "neo_id" -> neoId,
      "location" -> MongoDocument(
        "type" -> "Point",
        "coordinates" -> BsonArray(lng, lat)
      )
    )
    Await.result(MDB.addNode(nodeInsertQuery), Duration.Inf)
    JStream.of[GeoNode](new GeoNode(node))
  }

}

