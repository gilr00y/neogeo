package neogeo
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.collection.immutable.Document

case class GeoEntity(_id: ObjectId = new ObjectId,
                     neo_id: String = ""
                    ) {
  def toDocument: Document = org.mongodb.scala.bson.Document(
    "_id" -> this._id,
    "neo_id" -> this.neo_id
  )
}
