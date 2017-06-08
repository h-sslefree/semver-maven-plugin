package org.apache.maven.plugins.semver.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * <h1>SemverConsole</h1>
 *
 * <p>Implemented {@link java.io.BufferedReader} to avoid nullable Consoles in NON-native console appenders such as Eclipse.</p>
 *
 * @author sido
 */
public class SemverConsole {

    public static String readLine(String message) throws IOException {
        if (System.console() != null) {
            return System.console().readLine(message);
        } else {
            System.out.print(message);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        }
    }

    public static String readLine(String message, Object... args) throws IOException {
        if (System.console() != null) {
            return System.console().readLine(message, args);
        } else {
            System.out.print(String.format(message, args));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        }
    }

    public static String readPassword(String message)  throws IOException {
        if (System.console() != null) {
            return String.valueOf(System.console().readPassword(message));
        } else {
           return readLine(message);
        }
    }

    private SemverConsole() {}



}
