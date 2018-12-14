package org.atnos.registry


trait Json
case class JsonString(s: String) extends Json
case class JsonNumber(n: Int) extends Json
case class JsonArray(arr: List[Json]) extends Json
case class JsonObject(map: Map[String, Json]) extends Json

object Json {

  def string(s: String): Json = JsonString(s)
  def number(n: Int): Json = JsonNumber(n)
  def arr(js: List[Json]): Json = JsonArray(js)
  def obj(js: (String, Json)*): Json = JsonObject(js.toMap)

  implicit class JsonOps(s: String) {
    def :=(json: Json): (String, Json) = (s, json)
  }

}

trait Encoder[A] {
  def encode(a: A): Json
}
