package com.dslplatform.sbt

import java.io.{PrintWriter, StringWriter}

import com.dslplatform.compiler.client.Context
import com.dslplatform.compiler.client.parameters.{DisableColors, LogOutput}
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.Color
import sbt.Logger

private[sbt] class DslContext(logger: Logger) extends Context {

  private var inColor = logger.ansiCodesSupported

  if (!inColor) {
    put(DisableColors.INSTANCE, "")
  }

  private lazy val withLog = contains(LogOutput.INSTANCE)

  override def show(values: String*): Unit = {
    for (v <- values) {
      logger.info(v)
    }
  }

  override def log(value: String): Unit = {
    if (inColor) {
      logger.debug(Context.inColor(Color.YELLOW, value))
    } else {
      logger.debug(value)
    }
  }

  override def log(value: Array[Char], len: Int): Unit = {
    log(new String(value, 0, len))
  }

  override def warning(value: String): Unit = {
    if (inColor) {
      logger.warn(Context.inColor(Color.MAGENTA, value))
    } else {
      logger.warn(value)
    }
  }

  override def warning(ex: Exception): Unit = {
    warning(ex.getMessage)
    if (withLog) {
      val sw = new StringWriter
      ex.printStackTrace(new PrintWriter(sw))
      warning(sw.toString)
    }
  }

  override def error(value: String): Unit = {
    if (inColor) {
      logger.error(Context.inColor(Color.RED, value))
    } else {
      logger.error(value)
    }
  }

  override def error(ex: Exception): Unit = {
    error(ex.getMessage)
    if (withLog) {
      val sw = new StringWriter
      ex.printStackTrace(new PrintWriter(sw))
      error(sw.toString)
    }
  }

  private def askSafe(question: String, color: Color): Unit = {
    if (inColor) {
      try {
        print(Ansi.ansi.fgBright(color).bold.a(question + " ").boldOff.reset.toString)
      } catch {
        case _: NoSuchMethodError =>
          inColor = false
          print(question + " ")
      }
    } else print(question + " ")
  }

  override def ask(question: String): String = {
    askSafe(question, Color.DEFAULT)
    val reader = new jline.console.ConsoleReader()
    reader.readLine()
  }

  override def askSecret(question: String): Array[Char] = {
    askSafe(question, Color.CYAN)
    val reader = new jline.console.ConsoleReader()
    reader.readLine('*').toCharArray
  }
}
