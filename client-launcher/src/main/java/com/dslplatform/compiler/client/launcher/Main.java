package com.dslplatform.compiler.client.launcher;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class Main {
    public static void main(final String[] args) {
        final int argc = args.length;

        if (argc == 0) {
            System.out.println("This program runs a specific version of the DSL Platform CommandLine Client");
            System.out.println("Usage: java -jar dsl-clc.jar [version] [options]");
        }
        else {
            final String version = args[0];

            try {
                new URLClassLoader(new URL[] {
                        new URL(String.format("http://api.dsl-platform.com/dsl-clc-%s.jar", version)) } )
                    .loadClass("com.dslplatform.compiler.client.Main")
                    .getMethod("main", new Class<?>[] { String[].class })
                    .invoke(null, new Object[] { Arrays.copyOfRange(args, 1, argc) });
            } catch (final Exception e) {
                if (e instanceof ClassNotFoundException) {
                    System.out.println("Could not launch client version: " + version);
                }
                else {
                    System.out.println("Error: " + e.getMessage());
                }
                System.exit(-1);
            }
        }
    }
}
