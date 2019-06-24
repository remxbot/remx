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

package com.remxbot.bot.command.impl;

import com.remxbot.bot.command.Command;
import com.remxbot.bot.command.CommandCategory;
import com.remxbot.bot.command.CommandRunner;
import com.remxbot.bot.util.StringUtil;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.util.List;

public class Help implements Command {
    private final CommandRunner runner;

    public Help(CommandRunner runner) {
        this.runner = runner;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Gets a list of commands and their description";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }

    @Override
    public Mono<Void> process(Message m, List<String> args) {
        return m.getChannel().flatMap(c -> c.createEmbed(e -> {
            var help = new StringBuilder();
            var cmds = runner.getCommandMap().values();
            int maxLen = cmds.stream()
                    .map(Command::getName)
                    .mapToInt(String::length)
                    .max().orElseThrow(() -> new RuntimeException("failed to find the longest function name"));
            for (var cat : CommandCategory.values()) {
                help.append(String.format("**%s**:\n", cat.getPrettyName()));
                cmds.stream()
                        .filter(x -> cat.equals(x.getCategory()))
                        .map(x -> String.format("`%s:` %s\n", StringUtil.pad(maxLen, x.getName(), '.'), x.getDescription()))
                        .forEach(help::append);
            }
            e.setDescription(help.toString());
        })).then();
    }
}
