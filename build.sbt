// The simplest possible sbt build file is just one line:

scalaVersion := "2.13.12"

name := "BackendAuthentication"

version := "1.0"

val http4sVersion = "0.23.25"
val cirisVersion = "3.1.0"
val circeVersion = "0.14.5"
val catsEffectVersion = "3.4.8"
val fs2Version = "3.7.0"
val redis4catsVersion = "1.4.3"
val flywayVersion = "9.21.0"
val postgresVersion = "42.5.4"
val doobieVersion = "1.0.0-RC4"
val logbackVersion = "1.4.7"
val pureConfigVersion = "0.17.12"
val javaMailVersion = "1.6.2"
def kamon(artifact: String) = "io.kamon" %% s"kamon-$artifact" % "2.6.1"
val kamonCore = kamon("core")
val kamonHttp4s = kamon("http4s-0.23")
val kamonPrometheus = kamon("prometheus")
val kamonZipkin = kamon("zipkin")
val kamonJaeger = kamon("jaeger")

def circe(artifact: String): ModuleID =
  "io.circe" %% s"circe-$artifact" % circeVersion

def ciris(artifact: String): ModuleID = "is.cir" %% artifact % cirisVersion

def http4s(artifact: String): ModuleID =
  "org.http4s" %% s"http4s-$artifact" % http4sVersion

val prometheusMetrics = "org.http4s" %% "http4s-prometheus-metrics" % "0.24.6"

val circeGenericExtras = circe("generic-extras")
val circeCore = circe("core")
val circeGeneric = circe("generic")
val cireParser = "io.circe" %% "circe-parser" % circeVersion
val retry = "com.github.cb372" %% "cats-retry" % "3.1.0"
val cirisCore = ciris("ciris")
val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion
val fs2 = "co.fs2" %% "fs2-core" % fs2Version
val redis4cats = "dev.profunktor" %% "redis4cats-effects" % redis4catsVersion
val http4sDsl = http4s("dsl")
val http4sServer = http4s("ember-server")
val http4sClient = http4s("ember-client")
//val blazeClient= ???
//val blazeServer= "org.http4s" %% "http4s-blaze-server" % "0.23.15"
val http4sCirce = http4s("circe")

val doobie_hikari = "org.tpolecat" %% "doobie-hikari" % doobieVersion
val postgres = "org.postgresql" % "postgresql" % postgresVersion
val flyway = "org.flywaydb" % "flyway-core" % flywayVersion
val doobie = "org.tpolecat" %% "doobie-core" % doobieVersion
val doobie_postgres = "org.tpolecat" %% "doobie-postgres" % doobieVersion
val logback = "ch.qos.logback" % "logback-classic" % logbackVersion
// https://mvnrepository.com/artifact/org.tpolecat/skunk-core
val skunk = "org.tpolecat" %% "skunk-core" % "1.1.0-M3"

val auth0 = "com.auth0" % "java-jwt" % "4.2.1"
val javaMail = "com.sun.mail" % "javax.mail" % javaMailVersion
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
  kamonCore,
  kamonHttp4s,
  kamonPrometheus,
  kamonZipkin,
  kamonJaeger,
  prometheusMetrics,
  skunk,
  auth0,
  javaMail
)

scalacOptions += "-target:17" // ensures the Scala compiler generates bytecode optimized for the Java 17 virtual machine

//We can also set the soruce and target compatibility for the Java compiler by configuring the JavaOptions in build.sbt

javaOptions ++= Seq(
  "-soruce",
  "17",
  "target",
  "17"
)
