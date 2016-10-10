package com.dslplatform.sbt

import java.io.{PrintWriter, StringWriter}

import com.dslplatform.compiler.client.Context
import com.dslplatform.compiler.client.parameters.{DisableColors, DisablePrompt, LogOutput}
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.Color
import sbt.Logger

private[sbt] class DslContext(logger: Option[Logger]) extends Context {

  private var inColor = logger.isDefined && logger.get.ansiCodesSupported

  if (!inColor) {
    put(DisableColors.INSTANCE, "")
  }
  if (logger.isEmpty) {
    put(DisablePrompt.INSTANCE, "")
  }

  private lazy val withLog = contains(LogOutput.INSTANCE)

  override def show(values: String*): Unit = {
    if (logger.isDefined) {
      for (v <- values) {
        logger.get.info(v)
      }
    }
  }

  override def log(value: String): Unit = {
    if (logger.isDefined) {
      if (inColor) {
        logger.get.debug(Context.inColor(Color.YELLOW, value))
      } else {
        logger.get.debug(value)
      }
    }
  }

  override def log(value: Array[Char], len: Int): Unit = {
    log(new String(value, 0, len))
  }

  override def warning(value: String): Unit = {
    if (logger.isDefined) {
      if (inColor) {
        logger.get.warn(Context.inColor(Color.MAGENTA, value))
      } else {
        logger.get.warn(value)
      }
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
    if (logger.isDefined) {
      if (inColor) {
        logger.get.error(Context.inColor(Color.RED, value))
      } else {
        logger.get.error(value)
      }
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
