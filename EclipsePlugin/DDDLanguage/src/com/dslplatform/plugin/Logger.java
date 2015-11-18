package com.dslplatform.plugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
/**
* The logger of convenience for the Favorites plug-in.
*/
public class Logger {
  /**
  * Log the specified information as info with debug flag.
  *
  * @param message a human-readable message,
  *
  localized to the current locale
  */
  public static void debug(String message) {
    // info(String.format("%s DEBUG: %s", new Date(), message));
  }
  /**
  * Log the specified information.
  *
  * @param message a human-readable message,
  *
  localized to the current locale
  */
  public static void info(String message) {
    log(IStatus.INFO, IStatus.OK, message, null);
  }
  /**
  * Log the specified error.
  3.6.
  Logging
  37
  *
  * @param exception a low-level exception
  */
  public static void error(Throwable exception) {
    error("Unexpected Exception", exception);
  }
  /**
  * Log the specified error.
  *
  * @param message a human-readable message,
  *
  localized to the current locale
  * @param exception a low-level exception,
  *
  or <code>null</code> if not applicable
  */
  public static void error(
    String message, Throwable exception) {
    log(IStatus.ERROR, IStatus.OK, message, exception);
  }
  /**
  * Log the specified information.
  *
  * @param severity the severity; one of
  *
  <code>IStatus.OK</code>,
  *
  <code>IStatus.ERROR</code>,
  *
  <code>IStatus.INFO</code>,
  *
  or <code>IStatus.WARNING</code>
  * @param pluginId the unique identifier of the relevant
  * plug-in
  * @param code the plug-in-specific status code, or
  * <code>OK</code>
  * @param message a human-readable message,
  *
  localized to the current locale
  * @param exception a low-level exception,
  *
  or <code>null</code> if not applicable
  */
  public static void log(
    int severity,
    int code,
    String message,
    Throwable exception) {
    log(createStatus(severity, code, message, exception));
  }
  /**
  * Create a status object representing the
  * specified information.
  38
  TChapterT 3 Eclipse Infrastructure
  *
  * @param severity the severity; one of
  *
  <code>IStatus.OK</code>,
  *
  <code>IStatus.ERROR</code>,
  *
  <code>IStatus.INFO</code>,
  *
  or <code>IStatus.WARNING</code>
  * @param pluginId the unique identifier of the
  * relevant plug-in
  * @param code the plug-in-specific status code,
  * or <code>OK</code>
  * @param message a human-readable message,
  *
  localized to the current locale
  * @param exception a low-level exception,
  *
  or <code>null</code> if not applicable
  * @return the status object (not <code>null</code>)
  */
  public static IStatus createStatus(
  int severity,
  int code,
  String message,
  Throwable exception) {
    return new Status(
      severity,
      Activator.getDefault().getBundle().getSymbolicName(),
      code,
      message,
      exception);
  }
  /**
  * Log the given status.
  *
  * @param status the status to log
  */
  public static void log(IStatus status) {
    Activator.getDefault().getLog().log(status);
  }
}

