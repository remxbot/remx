/*
 * This file is part of remx.
 *
 * remx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * remx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with remx.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * This file is part of remx.
 *
 * remx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * remx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with remx.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.remxbot.bot.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Various utilities for dealing with strings
 */
public class StringUtil {
    public static final String BAR = "\u25ac";
    public static final String LICENSE_NOTICE = "remx is free software, for more information see:\n" +
            "- [Our Git repository](https://github.com/remxbot/remx)\n" +
            "- [COPYING](https://github.com/remxbot/remx/blob/master/COPYING) in said repository\n" +
            "- The [Free Software Foundation](https://www.fsf.org/)";

    /**
     * Splits the given string as arguments, so that it handles quoted parts of strings.
     *
     * @param str string to split
     * @return string after splitting
     */
    public static List<String> splitArgumentString(String str) {
        var temp = new StringBuilder();
        ArrayList<String> result = new ArrayList<>();
        boolean add = true;
        for (int i = 0; i < str.length(); i++) {
            // each time a space is encountered add temp to the list
            // if a " is encountered toggle that behavior
            char c = str.charAt(i);
            if (c == '"') {
                add ^= true;
            } else if (add && c == ' ') {
                if (temp.length() == 0) {
                    continue;
                }
                result.add(temp.toString());
                temp.setLength(0);
            } else {
                temp.append(c);
            }
        }
        if (temp.length() != 0) {
            result.add(temp.toString());
        }
        return result;
    }

    /**
     * @param min length to pad to
     * @param str string to pad
     * @return left-aligned padded string, at least as long as min
     */
    public static String pad(int min, String str) {
        return pad(min, str, ' ');
    }

    /**
     * @param min length to pad to
     * @param str string to pad
     * @param pad padding character
     * @return left-aligned padded string, at least as long as min
     */
    public static String pad(int min, String str, char pad) {
        var sb = new StringBuilder();
        sb.append(str);
        if (min > str.length()) {
            sb.append(String.valueOf(pad).repeat(min - str.length()));
        }
        return sb.toString();
    }

    /**
     * @param identifier YouTube video identifier
     * @return the URL on which the thumbnail can be found
     */
    public static String youtubeThumb(String identifier) {
        return String.format("https://img.youtube.com/vi/%s/hqdefault.jpg", identifier);
    }

    /**
     * Generates a nice looking progress bar for use in embeds
     * @param progress percentage [0, 1]
     * @param uri URI to use in the highlighted part of the bar, if null default exists
     * @return nicely formatted progress bar
     */
    public static String generateProgress(float progress, String uri) {
        int sections = (int) (progress * 10);
        if (uri == null) {
            // if you get the reference you deserve a veteran discount
            uri = "http://a/%%30%30";
        }
        if (sections == 0) {
            return BAR.repeat(10);
        } else {
            return String.format("[[%s](%s)%s]", BAR.repeat(sections), uri, BAR.repeat(10 - sections));
        }
    }

    /**
     * @param duration duration to format in millis
     * @return duration formatted as <code>(dd:hh:)mm:ss</code>. The part in parenthesis is optional.
     */
    public static String formatLength(long duration) {
        var builder = new StringBuilder();
        var days = duration / (24 * 3600 * 1000);
        duration %= 24 * 3600 * 1000;
        var hours = duration / (3600 * 1000);
        duration %= 3600 * 1000;
        var minutes = duration / (60 * 1000);
        duration %= 60 * 1000;
        var seconds = duration / 1000;
        if (days > 0) {
            builder.append(String.format("%02d", days)).append(':');
        }
        if (hours > 0) {
            builder.append(String.format("%02d", hours)).append(':');
        }
        return builder.append(String.format("%02d", minutes)).append(':')
                      .append(String.format("%02d", seconds)).toString();
    }
}

