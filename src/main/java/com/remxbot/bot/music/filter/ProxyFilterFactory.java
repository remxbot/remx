package com.remxbot.bot.music.filter;

import com.google.common.collect.Lists;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.FilterChainBuilder;
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Collections;
import java.util.List;

public class ProxyFilterFactory implements PcmFilterFactory {
    final private List<ConfigurableFilterFactory> factories;

    ProxyFilterFactory(List<ConfigurableFilterFactory> factories) {
        this.factories = factories;
    }

    @Override
    public List<AudioFilter> buildChain(AudioTrack track, AudioDataFormat format, UniversalPcmAudioFilter output) {
        var reversed = Lists.reverse(factories);
        var builder = new FilterChainBuilder();
        var top = output;
        for (var filter : reversed) {
            if (filter.isCompatible(format)) {
                builder.addFirst(filter.buildFilter(track, format, top));
                top = builder.makeFirstUniversal(2);
            }
        }
        var built = builder.build(null, 2).filters;
        Collections.reverse(built);
        return built;
    }
}
