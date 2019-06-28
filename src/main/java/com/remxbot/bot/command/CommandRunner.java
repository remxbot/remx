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

package com.remxbot.bot.command;

import com.remxbot.bot.util.StringUtil;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandRunner {
    private final List<Snowflake> ADMINS = Stream.of(115076505549144067L, 481143738887045121L, 130510525770629121L)
            .map(Snowflake::of)
            .collect(Collectors.toList());
    private final Logger LOGGER = LoggerFactory.getLogger(CommandRunner.class);
    private Map<String, Command> commandMap = new HashMap<>();
    private String prefix;

    public Mono<Void> processCommand(Message msg) {
        if (msg.getAuthor().map(User::isBot).orElse(true)) {
            return Mono.empty();
        }
        var arguments = StringUtil.splitArgumentString(msg.getContent().orElse(""));
        if (arguments.isEmpty() || !arguments.get(0).startsWith(prefix)) {
            return Mono.empty();
        }
        LOGGER.debug("Processing command with arguments {}", arguments);
        var cmd = arguments.get(0);
        return Mono.justOrEmpty(commandMap.get(cmd.substring(prefix.length())))
                .filter(c -> c.getCategory() != CommandCategory.ADMIN
                        || msg.getAuthor().flatMap(x -> Optional.of(ADMINS.contains(x.getId()))).orElse(false))
                .flatMap(c -> c.process(msg, arguments))
                .onErrorResume(e -> Mono.fromRunnable(() -> LOGGER.error("Command processing failed for " + cmd, e))
                        .then(msg.getChannel()
                                .flatMap(c ->
                                        c.createMessage("There was an error while processing your command.\n"
                                                + e.getMessage()))
                                .then()));
    }

    public void addCommand(Command cmd) {
        commandMap.put(cmd.getName(), cmd);
    }

    public Map<String, Command> getCommandMap() {
        return Collections.unmodifiableMap(commandMap);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
