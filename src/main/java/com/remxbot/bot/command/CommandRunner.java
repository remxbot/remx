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

import com.remxbot.bot.RemxBot;
import com.remxbot.bot.util.StringUtil;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.*;

public class CommandRunner {
    private final RemxBot bot;
    private final Logger LOGGER = LoggerFactory.getLogger(CommandRunner.class);
    private Map<String, Command> commandMap = new HashMap<>();

    private String prefix;

    public CommandRunner(RemxBot bot) {
        this.bot = bot;
    }

    public Mono<Void> processCommand(Message msg) {
        if (msg.getAuthor().map(User::isBot).orElse(true)) {
            return Mono.empty();
        }
        var arguments = StringUtil.splitArgumentString(msg.getContent().orElse(""));
        if (arguments.isEmpty() || !arguments.get(0).startsWith(prefix)) {
            return Mono.empty();
        }
        LOGGER.debug("Processing command with arguments {}", arguments);
        return Mono.justOrEmpty(commandMap.get(arguments.get(0).substring(prefix.length())))
                .flatMap(c -> c.process(msg, arguments))
                .onErrorResume(e ->
                        Mono.fromRunnable(() ->
                                LOGGER.error("Command processing failed for " + arguments.get(0), e))
                                .then(msg.getChannel()
                                        .flatMap(c -> c.createMessage(s ->
                                                s.setContent("There was an error while processing your command.")))
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
