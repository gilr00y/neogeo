assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case x => MergeStrategy.first
}

name := "neo-geo"

version := "0.1.2"

scalaVersion := "2.12.11"

// Required for cats lib
scalacOptions += "-Ypartial-unification"

libraryDependencies ++= Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0",
  "org.neo4j" % "neo4j" % "4.1.3" % Provided,
  "org.neo4j.test" % "neo4j-harness" % "4.1.3" % Test,
  "org.neo4j.driver" % "neo4j-java-driver" % "4.1.1" % Test
)
