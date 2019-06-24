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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class StringUtilTest {

    @Test
    public void splitArgumentString() {
        String string = "foo bar fizz\" \"buzz \"buzz fizz\"";
        List<String> expectedResult = Arrays.asList("foo", "bar", "fizz buzz", "buzz fizz");
        assertThat(StringUtil.splitArgumentString(string), is(expectedResult));
        string = "\"\" \"\"";
        assertThat(StringUtil.splitArgumentString(string), is(Collections.emptyList()));
    }

    @Test
    public void pad() {
        assertEquals("test....", StringUtil.pad(8, "test", '.'));
        assertEquals("test", StringUtil.pad(3, "test", '.'));
        assertEquals("test", StringUtil.pad(-1, "test", '.'));
        assertEquals("test", StringUtil.pad(0, "test", '.'));
        assertEquals("", StringUtil.pad(0, "", '.'));

        assertEquals("test    ", StringUtil.pad(8, "test"));
        assertEquals("test", StringUtil.pad(3, "test"));
        assertEquals("test", StringUtil.pad(-1, "test"));
        assertEquals("test", StringUtil.pad(0, "test"));
        assertEquals("", StringUtil.pad(0, ""));
    }
}