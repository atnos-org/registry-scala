lazy val specs2Version     = "4.3.2"
lazy val catsVersion       = "1.5.0"

libraryDependencies in Global :=
  specs2     ++
  cats

resolvers ++= Seq(
    Resolver.sonatypeRepo("releases")
  , Resolver.typesafeRepo("releases")
  , Resolver.url("ambiata-oss", new URL("https://ambiata-oss.s3.amazonaws.com"))(Resolver.ivyStylePatterns))

lazy val specs2 = Seq(
    "org.specs2" %% "specs2-core"
  , "org.specs2" %% "specs2-matcher-extra"
  , "org.specs2" %% "specs2-scalacheck"
  , "org.specs2" %% "specs2-html"
  , "org.specs2" %% "specs2-junit").map(_ % specs2Version % "test")

lazy val cats = Seq(
  "org.typelevel" %% "cats-core" % catsVersion)