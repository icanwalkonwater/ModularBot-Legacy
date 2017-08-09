package com.jesus_crie.modularbot.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.jesus_crie.modularbot.utils.F.f;

public class MiscUtils {

    public static void notNull(Object o, String name) {
        if (o == null)
            throw new IllegalArgumentException(f("%s may not be null.", name));
    }

    public static String collectStackTrace(Throwable e) {
        final StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        return sw.getBuffer().toString();
    }
}
