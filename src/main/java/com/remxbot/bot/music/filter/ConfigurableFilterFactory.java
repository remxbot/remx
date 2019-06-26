package com.remxbot.bot.music.filter;

import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

/**
 * Provides an interface for factories that make configurable filters.<br/>
 * After all modifications to a filter are applied an update will be issued.<br/>
 * It is suggested to keep attribute IDs starting with zero and sequential.<br/>
 * The number and IDs of attributes shall not change during runtime.
 *
 * <p><b>NOTE:</b> Each factory should attempt to keep as few recreations as possible by caching the filters when
 *                 they are not changed, as change to any filter will cause additional calls to
 *                 {@link ConfigurableFilterFactory#buildFilter(AudioTrack, AudioDataFormat, UniversalPcmAudioFilter)}
 */
public interface ConfigurableFilterFactory {
    /**
     * Alters and verifies a floating point attributes value, for example each equalizer band may be an attribute
     * @param id ID of the attribute to change
     * @param value new value of the attribute
     * @return true if the value passed verification
     */
    boolean setFloatAttribute(int id, float value);

    /**
     * Retrieves the value of a floating point attribute
     * @param id argument ID to retrieve
     * @return the value of the attribute
     * @throws IllegalArgumentException in the case that the ID is invalid
     */
    float getFloatAttribute(int id) throws IllegalArgumentException;

    /**
     * @return the current values of all attributes
     */
    float[] getAllAttributes();

    /**
     * Alters all internal attributes to be the provided new one.<br/>
     * The attributes are verified and shall not be altered if at least one fails verification.
     * @param allAttributes New values for all attributes. Expected to be the same size as {@link #getAllAttributes()}
     * @throws IllegalArgumentException if the values do not pass verification
     */
    void setAllAttributes(float[] allAttributes) throws IllegalArgumentException;

    /**
     * Checks to see whether the generated audio filter can work with the given format
     * @param format the format to check against
     * @return true if the audio format can be operated on by filters from this factory
     */
    boolean isCompatible(AudioDataFormat format);

    /**
     * Builds a filter for inside the chain
     * @param track the track to build for
     * @param format the format that the track is in
     * @param output where to output the filter
     * @return newly constructed filter
     */
    AudioFilter buildFilter(AudioTrack track, AudioDataFormat format, UniversalPcmAudioFilter output);
}
