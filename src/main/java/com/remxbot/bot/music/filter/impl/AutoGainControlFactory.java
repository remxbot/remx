package com.remxbot.bot.music.filter.impl;

import com.remxbot.bot.music.filter.ConfigurableFilterFactory;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

// This class implements both the filter and the factory as it is trivial
public class AutoGainControlFactory implements ConfigurableFilterFactory, FloatPcmAudioFilter {
    float gain = 1f;

    @Override
    public boolean setFloatAttribute(int id, float value) throws IllegalArgumentException {
        throw new IllegalArgumentException("this filter has no attributes");
    }

    @Override
    public float getFloatAttribute(int id) throws IllegalArgumentException {
        throw new IllegalArgumentException("this filter has no attributes");
    }

    @Override
    public float[] getAllAttributes() {
        return new float[0];
    }

    @Override
    public void setAllAttributes(float[] allAttributes) throws IllegalArgumentException {
        throw new IllegalArgumentException("this filter has no attributes");
    }

    @Override
    public boolean isCompatible(AudioDataFormat format) {
        return true;
    }

    @Override
    public AudioFilter buildFilter(AudioTrack track, AudioDataFormat format, UniversalPcmAudioFilter output) {
        return this;
    }

    @Override
    public void process(float[][] input, int offset, int length) {
        float rms = 0;
        for (int channel = 0; channel < input.length; channel++) {
            var channelSize = input[channel].length;
            for (int i = 0; i < channelSize; i++) {
                var current = (input[channel][i] *= gain);
                rms += current * current / input.length * channelSize;
            }
        }
        if (rms > 0.75 * 0.75) {
            gain *= 0.7;
        } else if (rms < 0.25 * 0.25) {
            gain *= 1.1;
        }
    }

    @Override
    public void seekPerformed(long requestedTime, long providedTime) {
        // nothing to do
    }

    @Override
    public void flush() {
        // nothing to do
    }

    @Override
    public void close() {
        // nothing to do
    }
}
