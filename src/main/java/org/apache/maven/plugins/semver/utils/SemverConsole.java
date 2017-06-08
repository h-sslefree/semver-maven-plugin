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
            System.out.print(message);
            ConsoleEraser consoleEraser = new ConsoleEraser();
            String password = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            consoleEraser.start();
            try {
                password = in.readLine();
            }
            catch (IOException e){
                System.out.println("Error trying to read your password!");
                System.exit(1);
            }
            consoleEraser.halt();
            System.out.print("\b");
            return password;
        }
    }

    private static class ConsoleEraser extends Thread {
        private boolean running = true;
        public void run() {
            while (running) {
                System.out.print("\b ");
                try {
                    Thread.sleep(1);
                }
                catch(InterruptedException e) {
                    break;
                }
            }
        }

        public synchronized void halt() {
            running = false;
        }
    }

    private SemverConsole() {}




}
