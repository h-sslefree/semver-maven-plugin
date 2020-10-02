package org.apache.maven.plugins.semver.utils;

import static java.lang.System.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 *
 * <h1>SemverConsole</h1>
 *
 * <p>Implemented {@link java.io.BufferedReader} to avoid nullable Consoles in NON-native console
 * appenders such as Eclipse.
 *
 * @author sido
 */
public class SemverConsole {

  /**
   * Implements the readLine from {@link System#console()}. If the {@link System#console()} = null
   * then the {@link BufferedReader} is implemented.
   *
   * @param message message from goals
   * @return commandline message
   * @throws IOException
   */
  public static String readLine(String message) throws IOException {
    if (console() != null) {
      return console().readLine(message);
    } else {
      System.out.print(message);
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      return reader.readLine();
    }
  }

  public static String readLine(String message, Object... args) throws IOException {
    if (console() != null) {
      return console().readLine(message, args);
    } else {
      System.out.printf(message, args);
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      return reader.readLine();
    }
  }

  public static String readPassword(String message) throws IOException {
    if (console() != null) {
      return String.valueOf(console().readPassword(message));
    } else {
      return readLine(message);
    }
  }

  /** Protect illegal usage. */
  private SemverConsole() {}
}
