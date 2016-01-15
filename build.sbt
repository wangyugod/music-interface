import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

name := "music-interface"

organization  := "infothunder.reactive"

version := "0.2"

packageArchetype.java_application

scalaVersion := "2.10.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers +=
  "Spray repository" at "http://repo.spray.io/"


libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.2"
  Seq(
    "io.spray" %%  "spray-json" % "1.3.2",
    "io.spray"            %%   "spray-can"      % sprayV,
    "io.spray"            %%   "spray-routing"  % sprayV,
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "org.json4s" %% "json4s-native" % "3.2.4",
    "c3p0" % "c3p0" % "0.9.1.2"
  )
}

Revolver.settings    