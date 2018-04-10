// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `streamarchitect-io-plattform-ingest` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.scalaCheck % Test,
        library.scalaTest  % Test,
        library.moquette,
        library.typesafeConfig,
        library.domain
      ),
      libraryDependencies ++= library.log,
      libraryDependencies ++= library.akkaBundle,
      libraryDependencies ++= library.kafkaBundle
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val scalaCheck     = "1.13.5"
      val scalaTest      = "3.0.1"
      val logback        = "1.2.3"
      val scalaLogging   = "3.8.0"
      val typesafeConfig = "1.3.1"
      val alpakka        = "0.9"
      val akka           = "2.5.6"
      val moquette       = "0.10"
      val domain         = "1.0.0-SNAPSHOT"
      val kafka          = "1.1.0"
      val akkaKafkaStreams = "0.20"

    }
    val scalaCheck       = "org.scalacheck"           %% "scalacheck"               % Version.scalaCheck
    val scalaTest        = "org.scalatest"            %% "scalatest"                % Version.scalaTest
    val logback          = "ch.qos.logback"             %   "logback-classic"           % Version.logback
    val scalaLogging     = "com.typesafe.scala-logging" %%  "scala-logging"             % Version.scalaLogging
    val typesafeConfig   = "com.typesafe"             % "config"                    % Version.typesafeConfig

    val moquette   = "io.moquette"    % "moquette-broker" % Version.moquette

    val alpakka          = "com.lightbend.akka"       %% "akka-stream-alpakka-mqtt" % Version.alpakka
    val akka             = "com.typesafe.akka"        %% "akka-actor"               % Version.akka
    val akkaLog          = "com.typesafe.akka"        %% "akka-slf4j"               % Version.akka
    val akkaStream       = "com.typesafe.akka"        %% "akka-stream"              % Version.akka
    val akkaTestKit      = "com.typesafe.akka"        %% "akka-testkit"             % Version.akka

    val kafka            = "org.apache.kafka"         %% "kafka"                    % Version.kafka
    val akkaKafkaStreams = "com.typesafe.akka"        %% "akka-stream-kafka"        % Version.akkaKafkaStreams

    val domain           = "io.streamarchitect"       %% "streamarchitect-io-platform-domain" % Version.domain

    val akkaBundle = Seq(akka, akkaLog, akkaStream, akkaTestKit)
    val kafkaBundle = Seq(kafka, akkaKafkaStreams)
    val log = Seq(logback, scalaLogging)

    /**
      * Listing of the dependencies that are being globally excluded
      */
    object GlobalExclusions {

      val commonsLogging = "commons-logging"          % "commons-logging"
      val logbackClassic = "ch.qos.logback"           % "logback-classic"
      val logbackCore    = "ch.qos.logback"           % "logback-core"
      val tinyLog        = "org.tinylog"              % "tinylog"
      val log4j1         = "log4j"                    % "log4j"
      val log4jextras    = "log4j"                    % "apache-log4j-extras"
      val log4j2         = "org.apache.logging.log4j" % "log4j-slf4j-impl"
      val slf4jlog4j12   = "org.slf4j"                % "slf4j-log4j12"

      val log4j1deps    = Seq(log4j1, log4jextras, slf4jlog4j12, log4j2)
      val logExclusions = Seq(commonsLogging, logbackClassic, tinyLog) ++ log4j1deps

    }
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  scalafmtSettings++
  publishSettings ++
  releaseSettings

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    scalaVersion := "2.11.12",
    organization := "io.streamarchitect",
    organizationName := "Bastian Kraus",
    startYear := Some(2018),
    licenses += ("GPL-3.0", url("http://www.gnu.org/licenses/gpl-3.0.en.html")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8",
      "-Ypartial-unification",
      "-Ywarn-unused-import"
    ),
    resolvers += Resolver.jcenterRepo,
    excludeDependencies ++= library.GlobalExclusions.logExclusions,
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    credentials += credentialsProvider(),
    updateOptions := updateOptions.value.withGigahorse(false),
    wartremoverWarnings in (Compile, compile) ++= Warts.unsafe
)

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )

val nexusHttpMethod     = Option(System.getenv("NEXUS_HTTP_METHOD")).getOrElse("http")
val nexusUrl            = Option(System.getenv("NEXUS_URL")).getOrElse("nexus.streamarchitect.io")
val nexusRepositoryPath = Option(System.getenv("NEXUS_REPOSITORY_PATH")).getOrElse("repository/streamarchitect-snapshot/")
val nexusColonPort      = Option(System.getenv("NEXUS_PORT")).map(":" + _).getOrElse("")
val nexusUsername       = System.getenv("NEXUS_USERNAME_VARIABLE")
val nexusPassword       = System.getenv("NEXUS_PASSWORD_VARIABLE")
val nexusAddress        = s"$nexusHttpMethod://$nexusUrl$nexusColonPort"
val publishRepository = MavenRepository(
  "Sonatype Nexus Repository Manager",
  s"$nexusAddress/$nexusRepositoryPath"
)

def credentialsProvider(): Credentials = {
  val fileExists = (Path.userHome / ".sbt" / ".credentials-streamarchitect").exists()

  if (fileExists) {
    Credentials(Path.userHome / ".sbt" / ".credentials-streamarchitect")
  } else {
    Credentials(
      "Sonatype Nexus Repository Manager",
      nexusUrl,
      nexusUsername,
      nexusPassword
    )
  }
}

def isSnapshot(): Boolean = nexusRepositoryPath.toLowerCase.contains("snapshot")

lazy val publishSettings = Seq(
  resolvers ++= Seq(
    "nexus" at s"$nexusAddress/repository/maven-public/"
  ),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := Some(publishRepository),
  updateOptions := updateOptions.value.withGigahorse(false)
)

// -----------------------------------------------------------------------------
// release settings

import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._

val nextVersion = "0.0.1"

releaseNextVersion := { ver =>
  import sbtrelease._

  println(s"Release Version: ${ver} - Preset next Version: ${nextVersion}")

  if (nextVersion > ver) {
    nextVersion
  } else {
    println(
      "nextVersion has not been defined, or been too low compared to current version, therefore it's bumped to next BugFix version"
    )
    Version(ver).map(_.bumpBugfix.asSnapshot.string).getOrElse(versionFormatError)
  }
}

def releaseStepsProvider(): Seq[ReleaseStep] = {
  ConsoleOut.systemOut.println(s"is snapshot: ${isSnapshot()}")
  if (isSnapshot) {
    Seq[ReleaseStep](
      inquireVersions,
      publishArtifacts
    )
  } else {
    Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  }
}

lazy val releaseSettings = Seq(
  releaseProcess := releaseStepsProvider()
)
