ThisBuild / organization := "se.bjornregnell"
ThisBuild / version      := "1.0.0"
ThisBuild / scalaVersion := "2.13.15"

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.2.10"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.20"

scalacOptions ++= Seq("-unchecked", "-deprecation")

fork := true
run / connectInput := true
outputStrategy := Some(StdoutOutput)

ThisBuild / useSuperShell := false