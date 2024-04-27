name := """DecodingUs"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.3.3"

val PLAY_SILHOUETTE_VERSION = "10.0.0"
val PLAY_SLICK_VERSION = "6.1.0"
libraryDependencies ++= Seq(
  guice,
  "org.playframework.silhouette" %% "play-silhouette" % PLAY_SILHOUETTE_VERSION,
  "org.playframework.silhouette" %% "play-silhouette-crypto-jca" % PLAY_SILHOUETTE_VERSION,
  "org.playframework.silhouette" %% "play-silhouette-password-bcrypt" % PLAY_SILHOUETTE_VERSION,
  "org.playframework.silhouette" %% "play-silhouette-persistence" % PLAY_SILHOUETTE_VERSION,
  "org.playframework.silhouette" %% "play-silhouette-totp" % PLAY_SILHOUETTE_VERSION,
  "org.playframework" %% "play-guice" % "3.0.2",
  "org.playframework" %% "play-mailer" % PLAY_SILHOUETTE_VERSION,
  "org.playframework" %% "play-mailer-guice" % PLAY_SILHOUETTE_VERSION,
  "org.playframework" %% "play-slick" % PLAY_SLICK_VERSION,
  "org.playframework" %% "play-slick-evolutions" % PLAY_SLICK_VERSION,
  "org.postgresql" % "postgresql" % "42.7.3",
  "com.github.tminglei" %% "slick-pg" % "0.22.1",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.22.1",
  "com.iheart" %% "ficus" % "1.5.2",
  "net.codingwell" %% "scala-guice" % "7.0.0",
  "com.github.samtools" % "htsjdk" % "4.1.0",
  "software.amazon.awssdk" % "s3" % "2.25.39",
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
  "org.webjars" %% "webjars-play" % "3.0.1",
  "org.webjars" % "bootstrap" % "5.3.3",
  "org.webjars.npm" % "htmx.org" % "1.9.12"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
