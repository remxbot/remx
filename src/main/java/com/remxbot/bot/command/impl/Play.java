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
import discord4j.core.object.entity.GuildMessageChannel;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

import java.util.List;

public class Play implements Command {
    final private RemxBot bot;

    public Play(RemxBot bot) {
        this.bot = bot;
    }

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public void formLongDescription(EmbedCreateSpec embed) {
        embed.setDescription("Adds track(s) to the player queue, at the end of the queue");
        embed.addField("Example", "`r:play \"https://www.youtube.com/watch?v=9sJUDx7iEJw\"`", false);
        embed.addField("Example (search)", "`r:play \"ytsearch: the search\"`", false);
        embed.addField("Example (playlist)", "`r:play \"https://www.youtube.com/watch?v=q6EoRBvdVPQ&list=PLFsQleAWXsj_4yDeebiIADdH5FMayBiJo\"`", false);
    }

    @Override
    public String getDescription() {
        return "Enqueues the chosen tracks";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public Mono<Void> process(Message m, List<String> args) {
        if (args.size() != 2) {
            return m.getChannel().flatMap(c -> c.createMessage(s ->
                    s.setContent("Expected one argument! For more information see `r:help play`.")))
                    .then();
        }
        return m.getChannel()
                .ofType(GuildMessageChannel.class)
                .flatMapMany(gmc -> {
                    var disp = bot.getGuildAudioDispatcher(gmc.getGuildId());
                    return disp.findSongs(args.get(1)).doOnNext(disp::enqueue);
                })
                .count()
                .zipWith(m.getChannel())
                .flatMap(t -> t.getT2().createMessage(String.format("Enqueued **%s** track(s).", t.getT1())))
                .then();

    }
}
