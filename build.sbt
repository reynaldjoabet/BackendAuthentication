// The simplest possible sbt build file is just one line:

scalaVersion := "2.13.14"

name := "BackendAuthentication"

version := "1.0"

val http4sVersion = "0.23.27"

val cirisVersion = "3.6.0"

val circeVersion = "0.14.8"

val catsEffectVersion = "3.4.11"
val fs2Version        = "3.9.4"
val redis4catsVersion = "1.7.0"
val flywayVersion     = "9.21.2"
val postgresVersion   = "42.7.1"
val doobieVersion     = "1.0.0-RC4"

val logbackVersion    = "1.4.14"

val pureConfigVersion = "0.17.12"
val javaMailVersion   = "1.6.2"

def circe(artifact: String): ModuleID =
  "io.circe" %% s"circe-$artifact" % circeVersion

def ciris(artifact: String): ModuleID = "is.cir" %% artifact % cirisVersion

def http4s(artifact: String): ModuleID =
  "org.http4s" %% s"http4s-$artifact" % http4sVersion

val prometheusMetrics = "org.http4s" %% "http4s-prometheus-metrics" % "0.24.7"

val circeGenericExtras = circe("generic-extras")
val circeCore          = circe("core")
val circeGeneric       = circe("generic")
val cireParser         = "io.circe"         %% "circe-parser" % circeVersion
val retry              = "com.github.cb372" %% "cats-retry"   % "3.1.3"
val cirisCore          = ciris("ciris")
val catsEffect         = "org.typelevel"    %% "cats-effect"  % catsEffectVersion
val fs2                = "co.fs2"           %% "fs2-core"     % fs2Version
val http4sDsl          = http4s("dsl")
val http4sServer       = http4s("ember-server")
val http4sClient       = http4s("ember-client")
//val blazeClient= ???
//val blazeServer= "org.http4s" %% "http4s-blaze-server" % "0.23.15"
val http4sCirce = http4s("circe")

val doobie_hikari   = "org.tpolecat"  %% "doobie-hikari"   % doobieVersion
val postgres        = "org.postgresql" % "postgresql"      % postgresVersion
val flyway          = "org.flywaydb"   % "flyway-core"     % flywayVersion
val doobie          = "org.tpolecat"  %% "doobie-core"     % doobieVersion
val doobie_postgres = "org.tpolecat"  %% "doobie-postgres" % doobieVersion
val logback         = "ch.qos.logback" % "logback-classic" % logbackVersion
// https://mvnrepository.com/artifact/org.tpolecat/skunk-core
val skunk = "org.tpolecat" %% "skunk-core" % "1.1.0-M3"

val auth0    = "com.auth0"    % "java-jwt"   % "4.4.0"
val javaMail = "com.sun.mail" % "javax.mail" % javaMailVersion

val redis4cats         = "dev.profunktor" %% "redis4cats-effects"  % redis4catsVersion
val redis4catsLog4cats = "dev.profunktor" %% "redis4cats-log4cats" % redis4catsVersion
libraryDependencies ++= Seq(
  cirisCore,
  http4sDsl,
  http4sServer,
  http4sClient,
  http4sCirce,
  circeCore,
  circeGeneric,
  logback,
  catsEffect,
  fs2,
  retry,
  redis4cats,
  cireParser,
  doobie_hikari,
  flyway,
  doobie,
  doobie_postgres,
  postgres,
  prometheusMetrics,
  skunk,
  auth0,
  javaMail,
  redis4cats,
  redis4catsLog4cats
)

//scalacOptions += "-target:17" // ensures the Scala compiler generates bytecode optimized for the Java 17 virtual machine

//We can also set the soruce and target compatibility for the Java compiler by configuring the JavaOptions in build.sbt

// javaOptions ++= Seq(
//   "source",
//   "17",
//   "target",
//   "17"
// )

ThisBuild / semanticdbEnabled := true

ThisBuild / usePipelining := true
