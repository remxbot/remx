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
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.util.List;

public class Join implements Command {
    final private RemxBot bot;

    public Join(RemxBot bot) {
        this.bot = bot;
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Joins the callers current voice channel";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public Mono<Void> process(Message msg, List<String> args) {
        return msg.getAuthorAsMember()
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                .flatMap(c -> bot.getGuildAudioDispatcher(c.getGuildId()).connect(c))
                .then();
    }
}
