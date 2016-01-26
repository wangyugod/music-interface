import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

name := "music-interface"

organization  := "infothunder.reactive"

version := "0.2"

packageArchetype.java_application

scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers +=
  "Spray repository" at "http://repo.spray.io/"


libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.2"
  Seq(
  	"org.scala-lang.modules" %% "scala-xml" % "1.0.5",
    "io.spray" %%  "spray-json" % "1.3.2",
    "io.spray"            %%   "spray-can"      % sprayV,
    "io.spray"            %%   "spray-routing"  % sprayV,
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "org.json4s" %% "json4s-native" % "3.3.0",
    "c3p0" % "c3p0" % "0.9.1.2",
    "org.scalikejdbc" %% "scalikejdbc"       % "2.3.4",
    "ch.qos.logback"  %  "logback-classic"   % "1.1.3",
    "commons-codec" % "commons-codec" % "1.9"
  )
}

Revolver.settings    