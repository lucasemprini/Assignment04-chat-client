package model.utility

object Log {
  val degug: Boolean = true

  def debug(msg: String): Unit = if (degug) println(msg)
}
