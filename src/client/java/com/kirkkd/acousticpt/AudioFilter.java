package com.kirkkd.acousticpt;

import org.lwjgl.openal.ALC10;

import java.nio.ByteBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.EXTEfx.*;


public class AudioFilter {
    static {
        // Open the default OpenAL device.
        long device = ALC10.alcOpenDevice((ByteBuffer) null);
        if (device == 0L) {
            throw new IllegalStateException("Failed to open the default device.");
        }

        if (!ALC10.alcIsExtensionPresent(device, "ALC_EXT_EFX")) {
            throw new IllegalStateException("EFX extension not supported!");
        }
    }

    private final int sourceId;
    private int filterId;
    private boolean isFilterCreated = false;

    public AudioFilter(int sourceId) {
        this.sourceId = sourceId;
    }

    private void createFilterIfNeeded() {
        if (!isFilterCreated) {
            filterId = alGenFilters();
            isFilterCreated = true;
        }
    }

    public AudioFilter lowPass(float gain, float gainHF) {
        createFilterIfNeeded();
        alFilteri(filterId, AL_FILTER_TYPE, AL_FILTER_LOWPASS);
        alFilterf(filterId, AL_LOWPASS_GAIN, gain);
        alFilterf(filterId, AL_LOWPASS_GAINHF, gainHF);
        attachToSource();

        return this;
    }

    private void attachToSource() {
        alSourcei(sourceId, AL_DIRECT_FILTER, filterId);
    }

    public void destroy() {
        if (isFilterCreated) {
            alDeleteFilters(filterId);
            alSourcei(sourceId, AL_DIRECT_FILTER, AL_FILTER_NULL);
            isFilterCreated = false;
        }
    }

    private static void checkALError() {
        int err = alGetError();
        if (err != AL_NO_ERROR) {
            throw new RuntimeException("OpenAL error: " + alGetString(err));
        }
    }
}