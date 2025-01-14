ThisBuild / scalaVersion                  := _root_.scalafix.sbt.BuildInfo.scala212
ThisBuild / organization                  := "com.alejandrohdezma"
ThisBuild / pluginCrossBuild / sbtVersion := "1.2.8"
ThisBuild / Test / parallelExecution      := false

addCommandAlias("ci-test", "fix --check; mdoc; test; publishLocal; scripted")
addCommandAlias("ci-docs", "github; mdoc; headerCreateAll; publishToGitHubPages")
addCommandAlias("ci-publish", "github; ci-release")

val `sbt-mdoc` = "org.scalameta" % "sbt-mdoc" % "[2.0,)" % Provided // scala-steward:off

val `sbt-header` = "de.heikoseeberger" % "sbt-header" % "[5.6.0,)" % Provided // scala-steward:off

lazy val documentation = project
  .enablePlugins(MdocPlugin)
  .settings(mdocOut := file("."))

lazy val site = project
  .enablePlugins(MdocPlugin)
  .settings(mdocIn := baseDirectory.value / "docs")
  .settings(watchTriggers += mdocIn.value.toGlob / "*.md")
  .settings(mdocVariables += "EXCLUDED" -> excludedContributors.value.mkString("- ", "\n- ", ""))
  .enablePlugins(GitHubPagesPlugin)
  .settings(gitHubPagesSiteDir := mdocOut.value)

lazy val `sbt-github` = module
  .enablePlugins(SbtPlugin)
  .settings(scriptedLaunchOpts += s"-Dplugin.version=${version.value}")
  .settings(libraryDependencies += "org.typelevel" %% "jawn-parser" % "1.4.0")
  .settings(libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-dsl" % "0.23.16" % Test)
  .settings(libraryDependencies += "org.http4s" %% "http4s-blaze-server" % "0.23.12" % Test)

lazy val `sbt-github-mdoc` = module
  .enablePlugins(SbtPlugin)
  .dependsOn(`sbt-github`)
  .settings(description := "Provides most of the info downloaded by sbt-github as mdoc variables")
  .settings(scriptedLaunchOpts += s"-Dplugin.version=${version.value}")
  .settings(addSbtPlugin(`sbt-mdoc`))

lazy val `sbt-github-header` = module
  .enablePlugins(SbtPlugin)
  .dependsOn(`sbt-github`)
  .settings(description := "Integration between sbt-github and sbt-header")
  .settings(scriptedLaunchOpts += s"-Dplugin.version=${version.value}")
  .settings(addSbtPlugin(`sbt-header`))
