ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "2.12.15"

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

// https://mvnrepository.com/artifact/org.apache.spark/spark-core
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.1.2"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.2.1"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.2.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % Test

scalacOptions += "-target:jvm-1.8"

lazy val root = (project in file("."))
  .settings(
    name := "AccidentSeverityPrediction"
  )

initialize := {
  val _ = initialize.value // run the previous initialization
  val required = "1.8"
  val current  = sys.props("java.specification.version")
  assert(current == required, s"Unsupported JDK: java.specification.version $current != $required")
}
