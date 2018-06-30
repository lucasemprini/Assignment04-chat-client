package model

object Log {
  val degug: Boolean = true

  def debug(msg: String) = if (degug) println(msg)
}
