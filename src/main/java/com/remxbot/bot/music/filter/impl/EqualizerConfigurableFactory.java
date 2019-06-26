package com.remxbot.bot.music.filter.impl;

import com.remxbot.bot.music.filter.ConfigurableFilterFactory;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

/**
 * Creates configured equalizers.
 */
public class EqualizerConfigurableFactory implements ConfigurableFilterFactory {
    private Equalizer cache = null;
    private float[] bandMultipliers = new float[Equalizer.BAND_COUNT];

    @Override
    public synchronized boolean setFloatAttribute(int id, float value) {
        if (value >= -0.25 && value <= 1
                && id >= 0 && id < Equalizer.BAND_COUNT) {
            setFloatAttributeDirect(id, value);
            return true;
        }
        return false;
    }

    @Override
    public synchronized float getFloatAttribute(int id) throws IllegalArgumentException {
        if (id >= 0 && id < Equalizer.BAND_COUNT) {
            return bandMultipliers[id];
        }
        throw new IllegalArgumentException("id out of range");
    }

    @Override
    public synchronized float[] getAllAttributes() {
        return bandMultipliers.clone();
    }

    @Override
    public synchronized void setAllAttributes(float[] allAttributes) throws IllegalArgumentException {
        if (allAttributes.length != Equalizer.BAND_COUNT) {
            throw new IllegalArgumentException("band count wrong");
        }
        for (float f : allAttributes) {
            if (f < -0.25f || f > 1f) {
                throw new IllegalArgumentException("band out of range");
            }
        }

        for (int i = 0; i < allAttributes.length; i++) {
            setFloatAttributeDirect(i, allAttributes[i]);
        }
    }

    @Override
    public boolean isCompatible(AudioDataFormat format) {
        return Equalizer.isCompatible(format);
    }

    @Override
    public synchronized AudioFilter buildFilter(AudioTrack track, AudioDataFormat format, UniversalPcmAudioFilter output) {
        return cache = new Equalizer(format.channelCount, output, bandMultipliers);
    }

    private void setFloatAttributeDirect(int id, float value) {
        bandMultipliers[id] = value;
        if (cache != null) {
            cache.setGain(id, value);
        }
    }
}
