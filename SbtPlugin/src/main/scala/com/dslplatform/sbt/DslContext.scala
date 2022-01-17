package com.dslplatform.sbt

import com.dslplatform.compiler.client.Context
import com.dslplatform.compiler.client.parameters.{DisableColors, DisablePrompt, LogOutput}
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.Color
import sbt.Logger

import java.io.{PrintWriter, StringWriter}

private[sbt] class DslContext(
  logger: Option[Logger],
  verbose: Boolean,
  ansi: Boolean) extends Context {

  if (logger.isEmpty) put(DisablePrompt.INSTANCE, "")
  else {
    put(LogOutput.INSTANCE,"")
    if (!ansi) put(DisableColors.INSTANCE, "")
  }

  override def show(values: String*): Unit =
    if (logger.isDefined) {
      for (v <- values) {
        logger.get.info(v)
      }
    }

  override def log(value: String): Unit =
    if (logger.isDefined && verbose) {
      if (ansi) {
        logger.get.debug(Context.inColor(Color.YELLOW, value))
      } else {
        logger.get.debug(value)
      }
    }

  override def log(value: Array[Char], len: Int): Unit =
    log(new String(value, 0, len))

  override def warning(value: String): Unit =
    if (logger.isDefined) {
      if (ansi) {
        logger.get.warn(Context.inColor(Color.MAGENTA, value))
      } else {
        logger.get.warn(value)
      }
    }

  override def warning(ex: Exception): Unit = {
    warning(ex.getMessage)
    if (logger.isDefined) {
      val sw = new StringWriter
      ex.printStackTrace(new PrintWriter(sw))
      warning(sw.toString)
    }
  }

  private var lastError = ""

  def isParseError: Boolean =
    lastError != null &&
    (lastError.startsWith("Error parsing dsl in script") ||
     lastError.startsWith("Error in") && lastError.contains(" near line ") && lastError.contains(" and column "))

  override def error(value: String): Unit = {
    lastError = value
    if (logger.isDefined) {
      if (ansi) {
        logger.get.error(Context.inColor(Color.RED, value))
      } else {
        logger.get.error(value)
      }
    }
  }

  override def error(ex: Exception): Unit = {
    error(ex.getMessage)
    if (logger.isDefined) {
      val sw = new StringWriter
      ex.printStackTrace(new PrintWriter(sw))
      error(sw.toString)
    }
  }

  private var askedQuestion = false
  def hasInteracted: Boolean = askedQuestion

  private def askSafe(question: String, color: Color): Unit = {
    askedQuestion = true
    if (!ansi) print(question + " ")
    else print(Ansi.ansi.fgBright(color).bold.a(question + " ").boldOff.reset.toString)
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
