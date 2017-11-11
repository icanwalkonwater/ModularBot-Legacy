package com.jesus_crie.modularbot.utils;

import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    public static void notEmpty(Map<?, ?> map, String name) {
        Checks.notNull(map, name);
        if (map.size() <= 0)
            throw new IllegalArgumentException(name + " may not be empty");
    }

    public static void checkEncodableUTF8(String unicode) {
        Checks.notNull(unicode, "unicode");
        try {
            MiscUtil.encodeUTF8(unicode);
        } catch (Exception e) {
            throw new IllegalArgumentException(unicode + " is not a valid UTF-8 character");
        }
    }

    public static String collectStackTrace(Throwable e) {
        final StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        return sw.getBuffer().toString();
    }

    public static String stringifyUser(User u) {
        return u.getName() + "#" + u.getDiscriminator();
    }

    public static String stringifyEmote(MessageReaction.ReactionEmote emote) {
        return emote.isEmote() ? emote.getId() : emote.getName();
    }

    public static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.toLowerCase().substring(1);
    }

    /**
     * Easily convert an amount of time into milliseconds.
     * @param time the amount if time in <code>unit</code>.
     * @param unit the unit of time.
     * @return the amount if time converted into milliseconds.
     */
    public static long convertTime(long time, TimeUnit unit) {
        return unit.toMillis(time);
    }

    /**
     * Format the timestamp to correspond the pattern.
     * The pattern can use:
     *      {day} -> day of the month in 2 digits.
     *      {month} -> month of the year in 2 digits.
     *      {year} -> year in 4 digits.
     *      {hour} -> hour of the day (24 hour clock) in 2 digits.
     *      {minutes} -> minutes of the hour in 2 digits.
     *      {seconds} -> seconds if the minute in 2 digits.
     *      {nano} -> nanoseconds of the minute in 9 digits.
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
