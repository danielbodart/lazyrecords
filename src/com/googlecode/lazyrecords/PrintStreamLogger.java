package com.googlecode.lazyrecords;

import java.io.PrintStream;
import java.util.Map;

public class PrintStreamLogger implements Logger {
    private final PrintStream printStream;

    public PrintStreamLogger(PrintStream printStream) {
        this.printStream = printStream;
    }

    @Override
    public Logger log(final Map<String, ?> parameters) {
        printStream.println(parameters);
        return this;
    }
}
