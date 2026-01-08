val zioVersion            = "2.0.13"
val zioJsonVersion        = "0.5.0"
val zioConfigVersion      = "3.0.7"
val zioLoggingVersion     = "2.1.11"
val logbackClassicVersion = "1.4.7"
val zioHttpVersion        = "3.0.0-RC1"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        organization := "com.example",
        scalaVersion := "3.7.4",
      )
    ),
    name           := "weather-forecast-server",
    libraryDependencies ++= Seq(
      "dev.zio"       %% "zio"                 % zioVersion,
      "dev.zio"       %% "zio-http"            % zioHttpVersion,
      "dev.zio"       %% "zio-config"          % zioConfigVersion,
      "dev.zio"       %% "zio-config-typesafe" % zioConfigVersion,
      "dev.zio"       %% "zio-json"            % zioJsonVersion,

      // logging
      "dev.zio"       %% "zio-logging"       % zioLoggingVersion,
      "dev.zio"       %% "zio-logging-slf4j" % zioLoggingVersion,
      "ch.qos.logback" % "logback-classic"   % logbackClassicVersion,

      // test
      "dev.zio"      %% "zio-test"                        % zioVersion            % Test,
      "dev.zio"      %% "zio-test-sbt"                    % zioVersion            % Test,
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  )
  .enablePlugins(JavaAppPackaging)
