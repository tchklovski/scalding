import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object ScaldingBuild extends Build {
  val sharedSettings = Project.defaultSettings ++ assemblySettings ++ Seq(
    organization := "com.twitter",
    version := "0.8.3-SNAPSHOT",

    //TODO: Change to 2.10.* when Twitter moves to Scala 2.10 internally
    scalaVersion := "2.9.2",
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
      "org.scala-tools.testing" %% "specs" % "1.6.9" % "test"
    ),

    resolvers ++= Seq(
      "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      "releases"  at "http://oss.sonatype.org/content/repositories/releases",
      "Concurrent Maven Repo" at "http://conjars.org/repo"
    ),

    parallelExecution in Test := false,

    scalacOptions ++= Seq("-unchecked", "-deprecation"),

    // Uncomment if you don't want to run all the tests before building assembly
    // test in assembly := {},

    // Publishing options:

    publishMavenStyle := true,

    publishArtifact in Test := false,

    pomIncludeRepository := { x => false },

    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("sonatype-snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("sonatype-releases"  at nexus + "service/local/staging/deploy/maven2")
    },

    // Janino includes a broken signature, and is not needed:
    excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
      val excludes = Set("jsp-api-2.1-6.1.14.jar", "jsp-2.1-6.1.14.jar",
                         "jasper-compiler-5.5.12.jar", "janino-2.5.16.jar")
      cp filter { jar => excludes(jar.data.getName)}
    },

    // Some of these files have duplicates, let's ignore:
    mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
      {
        case s if s.endsWith(".class") => MergeStrategy.last
        case s if s.endsWith(".html") => MergeStrategy.last
        case x => old(x)
      }
    },

    pomExtra := (
      <url>https://github.com/twitter/scalding</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
          <distribution>repo</distribution>
          <comments>A business-friendly OSS license</comments>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:twitter/scalding.git</url>
        <connection>scm:git:git@github.com:twitter/scalding.git</connection>
      </scm>
      <developers>
        <developer>
          <id>oscar</id>
          <name>Oscar Boykin</name>
          <url>http://twitter.com/posco</url>
        </developer>
        <developer>
          <id>sritchie</id>
          <name>Avi Bryant</name>
          <url>http://twitter.com/avibryant</url>
        </developer>
        <developer>
          <id>sritchie</id>
          <name>Argyris Zymnis</name>
          <url>http://twitter.com/argyris</url>
        </developer>
      </developers>)
  )

  lazy val scalding = Project(
    id = "scalding",
    base = file("."),
    settings = sharedSettings
    ).settings(
    test := { }
  ).aggregate(scaldingCore)

  lazy val scaldingCore = Project(
    id = "scalding-core",
    base = file("scalding-core"),
    settings = sharedSettings
  ).settings(
    name := "scalding-core",
    libraryDependencies ++= Seq(
      "cascading" % "cascading-core" % "2.0.7",
      "cascading" % "cascading-local" % "2.0.7",
      "cascading" % "cascading-hadoop" % "2.0.7",
      "cascading.kryo" % "cascading.kryo" % "0.4.6",
      "com.twitter" % "maple" % "0.2.5",
      "com.twitter" %% "chill" % "0.1.2",
      "com.twitter" %% "algebird" % "0.1.6",
      "commons-lang" % "commons-lang" % "2.4",
      "com.joestelmach" % "natty" % "0.7",
      "io.backchat.jerkson" %% "jerkson" % "0.7.0"
    )
  )
}
