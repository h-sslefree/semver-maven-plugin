package org.apache.maven.plugins.semver.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <h1>SemverConsole</h1>
 *
 * <p>Implemented {@link java.io.BufferedReader} to avoid nullable Consoles in NON-native console appenders such as Eclipse.</p>
 *
 * @author sido
 */
public class SemverConsole {

    /**
     *
     *
     * <p>Implements the readLine from {@link System#console()}. If the {@link System#console()} = null then the {@link BufferedReader} is implemented.</p>
     *
     * @param message
     * @return
     * @throws IOException
     */
    public static String readLine(String message) throws IOException {
        if (System.console() != null) {
            return System.console().readLine(message);
        } else {
            System.out.print(message);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        }
    }

    /**
     *
     * @param message
     * @param args
     * @return
     * @throws IOException
     */
    public static String readLine(String message, Object... args) throws IOException {
        if (System.console() != null) {
            return System.console().readLine(message, args);
        } else {
            System.out.print(String.format(message, args));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        }
    }

    /**
     *
     * @param message
     * @return
     * @throws IOException
     */
    public static String readPassword(String message)  throws IOException {
        if (System.console() != null) {
            return String.valueOf(System.console().readPassword(message));
        } else {
           return readLine(message);
        }
    }

    /**
     * <p>Protect illegal usage.</p>
     */
    private SemverConsole() {}



}
