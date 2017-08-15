package com.jesus_crie.modularbot.utils;

import net.dv8tion.jda.core.utils.Checks;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.jesus_crie.modularbot.utils.F.f;

public class MiscUtils {

    public static void requireBetween(int min, int max, int test, String message) {
        if (test < min && test > max)
            throw new IllegalArgumentException(message);
    }

    public static void notEmpty(Object[] array, String name) {
        Checks.notNull(array, name);
        if (array.length <= 0)
            throw new IllegalArgumentException(name + " may not be null");
    }

    public static String collectStackTrace(Throwable e) {
        final StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        return sw.getBuffer().toString();
    }

    public static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.toLowerCase().substring(1);
    }

    /**
     * Format the timestamp to correspond the pattern.
     * The pattern can use:
     *      %day% -> day of the month in 2 digits.
     *      %month% -> month of the year in 2 digits.
     *      %year% -> year in 4 digits.
     *      %hour% -> hour of the day (24 hour clock) in 2 digits.
     *      %minutes% -> minutes of the hour in 2 digits.
     *      %seconds% -> seconds if the minute in 2 digits.
     *      %nano% -> nanoseconds of the minute in 9 digits.
     * @param timestamp the timestamp to format.
     * @param pattern the pattern to use.
     * @return a String of the formatted timestamp that as given.
     */
    public static String properTimestamp(long timestamp, String pattern) {
        return f(pattern.replaceAll("\\{day}", "%1\\$td")
            .replaceAll("\\{month}", "%1\\$tm")
            .replaceAll("\\{year}", "%1\\$tY")
            .replaceAll("\\{hour}", "%1\\$tH")
            .replaceAll("\\{minutes}", "%1\\$tM")
            .replaceAll("\\{seconds}", "%1\\$tS")
            .replaceAll("\\{nano}", "%1\\$tN"), timestamp);
    }
}
