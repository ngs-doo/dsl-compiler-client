object Developers {
  lazy val members = Map(
    "melezov"   -> "Marko Elezović"
  , "rinmalavi" -> "Marin Vila"
  , "zapov"     -> "Rikard Pavelić"
  )

  def toXml =
    <developers>
      {members map { case (nick, name) =>
        <developer>
          <id>{ nick }</id>
          <name>{ name }</name>
          <url>https://github.com/{ nick }</url>
        </developer>
      }}
    </developers>
}
