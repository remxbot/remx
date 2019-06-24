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

package com.remxbot.bot;

import com.remxbot.bot.command.CommandRunner;
import com.remxbot.bot.music.GuildAudioDispatcher;
import com.remxbot.bot.command.impl.*;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RemxBot {
    final private Logger LOGGER = LoggerFactory.getLogger(RemxBot.class);
    final private DiscordClient client;
    final private CommandRunner runner = new CommandRunner();
    final private Map<Snowflake, GuildAudioDispatcher> dispatchers = new ConcurrentHashMap<>();
    private AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    public RemxBot(String token) {
        client = new DiscordClientBuilder(token).build();
        client.getEventDispatcher().on(ReadyEvent.class)
                .doOnNext(e -> LOGGER.info("Logged in as {}", e.getSelf().getUsername()))
                .flatMap(e -> getInvite())
                .subscribe(LOGGER::info);

        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.getConfiguration().setFilterHotSwapEnabled(true);

        runner.setPrefix("r:");
        runner.addCommand(new Info());
        runner.addCommand(new Help(runner));

        runner.addCommand(new Play(this));
        runner.addCommand(new Join(this));
        runner.addCommand(new Next(this));
        runner.addCommand(new Prev(this));
        runner.addCommand(new Playlist(this));

        runner.addCommand(new Exit());

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .flatMap(runner::processCommand).subscribe();
    }

    public void run() {
        client.login().block();
    }

    /**
     * Blocking logout method intended for use by shutdown hooks
     */
    public void logout() {
        client.logout().block();
    }

    private Mono<String> inviteMonoCache = null;
    public Mono<String> getInvite() {
        if (inviteMonoCache == null) {
            inviteMonoCache = client.getApplicationInfo()
                    .map(ApplicationInfo::getId)
                    .map(Snowflake::asString)
                    .map(id -> String.format("https://discordapp.com/oauth2/authorize?client_id=%s&scope=bot", id))
                    .cache();
        }
        return inviteMonoCache;
    }

    public GuildAudioDispatcher getGuildAudioDispatcher(Snowflake id) {
        return dispatchers.computeIfAbsent(id, gid -> new GuildAudioDispatcher(gid, playerManager));
    }
}
