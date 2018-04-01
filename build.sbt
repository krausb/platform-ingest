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
        library.typesafeConfig
      ),
      libraryDependencies ++= library.log,
      libraryDependencies ++= library.akkaBundle
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val scalaCheck     = "1.13.5"
      val scalaTest      = "3.0.1"
      val log4j          = "2.8.1"
      val disruptor      = "3.3.0"
      val jackson        = "2.9.2"
      val typesafeConfig = "1.3.1"
      val alpakka        = "0.9"
      val akka           = "2.5.6"
      val moquette       = "0.10"
    }
    val scalaCheck       = "org.scalacheck"           %% "scalacheck"               % Version.scalaCheck
    val scalaTest        = "org.scalatest"            %% "scalatest"                % Version.scalaTest
    val log4jCore        = "org.apache.logging.log4j" % "log4j-core"                % Version.log4j
    val log4j            = "org.apache.logging.log4j" % "log4j-api"                 % Version.log4j
    val log4jSlf4j       = "org.apache.logging.log4j" % "log4j-slf4j-impl"          % Version.log4j
    val disruptor        = "com.lmax"                 % "disruptor"                 % Version.disruptor
    val typesafeConfig   = "com.typesafe"             % "config"                    % Version.typesafeConfig

    val jacksonDatabind = "com.fasterxml.jackson.core"       % "jackson-databind"       % Version.jackson
    val jacksonCore     = "com.fasterxml.jackson.core"       % "jackson-core"           % Version.jackson
    val jacksonXml      = "com.fasterxml.jackson.dataformat" % "jackson-dataformat-xml" % Version.jackson

    val moquette   = "io.moquette"    % "moquette-broker" % Version.moquette

    val alpakka          = "com.lightbend.akka"       %% "akka-stream-alpakka-mqtt" % Version.alpakka
    val akka             = "com.typesafe.akka"        %% "akka-actor"               % Version.akka
    val akkaLog          = "com.typesafe.akka"        %% "akka-slf4j"               % Version.akka
    val akkaStream       = "com.typesafe.akka"        %% "akka-stream"              % Version.akka
    val akkaTestKit      = "com.typesafe.akka"        %% "akka-testkit"             % Version.akka

    val akkaBundle = Seq(akka, akkaLog, akkaStream, akkaTestKit)
    val log = Seq(log4j, log4jCore, log4jSlf4j, disruptor, jacksonCore, jacksonDatabind, jacksonXml)
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  scalafmtSettings

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    // scalaVersion := "2.12.4",
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
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    wartremoverWarnings in (Compile, compile) ++= Warts.unsafe
)

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )
