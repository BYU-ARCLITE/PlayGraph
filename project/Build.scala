import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "PlayGraph2"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "com.google.gdata.gdata-java-client" % "gdata-client-1.0" % "1.47.0",
    "mysql" % "mysql-connector-java" % "5.1.18"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    resolvers += "Google Gdata Maven Repository" at "http://maven.burtsev.net/"
  )

}
