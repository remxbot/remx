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

import com.remxbot.bot.RemxBot;
import com.remxbot.bot.command.Command;
import com.remxbot.bot.command.CommandCategory;
import com.remxbot.bot.util.StringUtil;
import discord4j.common.GitProperties;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.util.List;

public class Info implements Command {
    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "Prints general information about the bot";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }

    @Override
    public Mono<Void> process(Message m, List<String> args) {
        return m.getChannel()
                .zipWhen(x -> x.getClient().getGuilds().count())
                .flatMap(x -> x.getT1().createEmbed(embed -> {
            embed.setDescription("remx: discord music bot with sound processing capabilities");
            embed.addField("Shard ID", x.getT1().getClient().getConfig().getShardIndex() + "", true);
            embed.addField("This shard is serving", String.format("%d guild(s)", x.getT2()), true);
            embed.addField("Bot version", RemxBot.getVersion(), true);
            embed.addField("Discord4J version", GitProperties.getProperties().getProperty(GitProperties.APPLICATION_VERSION), true);
            embed.addField("License notice", StringUtil.LICENSE_NOTICE, false);
        })).then();
    }
}
