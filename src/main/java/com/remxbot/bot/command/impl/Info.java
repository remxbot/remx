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
    final private RemxBot bot;

    public Info(RemxBot bot) {
        this.bot = bot;
    }

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
        return Mono.zip(m.getChannel(), m.getClient().getGuilds().count(), bot.getInvite())
                .flatMap(x -> x.getT1().createEmbed(embed -> {
                    var chan = x.getT1();
                    var count = x.getT2();
                    var invite = x.getT3();
                    var d4jver = GitProperties.getProperties().getProperty(GitProperties.APPLICATION_VERSION);
                    embed.setDescription("remx: discord music bot with sound processing capabilities");
                    embed.addField("Shard ID", chan.getClient().getConfig().getShardIndex() + "", true);
                    embed.addField("Total shards", chan.getClient().getConfig().getShardCount() + "", true);
                    embed.addField("This shard is serving", String.format("%d guild(s)", count), true);
                    embed.addField("Bot version", RemxBot.getVersion(), true);
                    embed.addField("Discord4J version", d4jver, true);
                    embed.addField("Invite link", String.format("[click](%s)", invite), true);

                    embed.addField("License notice", StringUtil.LICENSE_NOTICE, false);
                })).then();
    }
}
