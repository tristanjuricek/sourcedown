name := "sourcedown"

version := "0.1.0"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
  "com.tristanhunt" %% "knockoff" % "0.8.0-16",
  "org.fusesource.scalate" % "scalate-core" % "1.5.2",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.2.0",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.2.0",
  "org.scalatest" %% "scalatest" % "1.6.1" % "test"
)

seq(ProguardPlugin.proguardSettings :_*)

proguardOptions += keepMain("com.tristanhunt.sourcedown.Sourcedown")