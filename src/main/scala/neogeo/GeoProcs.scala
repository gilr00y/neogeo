package neogeo

import neogeo.JavaHelper.GeoNode
import org.mongodb.scala.model.Filters.geoWithinCenterSphere
import org.neo4j.graphdb.Label
import org.neo4j.procedure.{Name, Procedure}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class GeoProcs extends JavaHelper {

  @Procedure(value="neogeo.withinRadius")
  def withinRadius(@Name("latitude") lat: Double,
                     @Name("longitude") lng: Double,
                     @Name("radiusInKm") radius: Double): java.util.stream.Stream[GeoNode] = {
    // TODO: Initialize Mongo cx elsewhere - can't store non-static state on GeoProcs.
    val MDB = new GeoData("neogeo")
    val indexQuery = geoWithinCenterSphere("location", lng, lat, radius)
//    Document(
//      "location" -> Document(
//        "$geoWithin" -> Document(
//          "$centerSphere" -> BsonArray(BsonArray(lng, lat), radius)
//        )
//      )
//    )
    val geoResults = Await.result(MDB.getGeoEntities(indexQuery), Duration.Inf)
    val geoIds = geoResults.map(_.neo_id)
    val geoIdString = geoIds.mkString(", ")

    val geoLabel = Label.label("TEST")
    val tx = db.beginTx
    try {
      val nodeStream = java.util.stream.Stream.of(geoIds.map { geoId: String =>
        new GeoNode(tx.findNode(geoLabel, "_node_id", geoId))
      } : _*)
      // TODO: retrieve nodes based on `geoId`s
//      val matchedGeoNodes: ResourceIterator[Node] = tx.execute(
//        f"MATCH (n:TEST) WHERE n._node_id IN [$geoIdString] return n"
        //      ,Map(
        //        "geoLabel"-> geoLabel,
        //        "geoIds" -> geoIdString
        //      ).asJava
//      )

//      val nodeStream = matchedGeoNodes.stream.map[GeoNode](new GeoNode(_))
      nodeStream
    } finally {
//      tx.commit()
    }

  }


}

