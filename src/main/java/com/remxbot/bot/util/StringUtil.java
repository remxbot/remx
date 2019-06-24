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
}

