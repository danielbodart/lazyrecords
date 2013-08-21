package com.googlecode.lazyrecords;

import java.io.PrintStream;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Debug.inDebug;
import static com.googlecode.totallylazy.Streams.nullPrintStream;
import static com.googlecode.totallylazy.Streams.streams;
import static java.lang.System.out;

public class DebugLoggerActivator implements Callable<Logger> {
    @Override
    public PrintStreamLogger call() throws Exception {
        return new PrintStreamLogger(inDebug() ? new PrintStream(streams(out)) : nullPrintStream());
    }
}
