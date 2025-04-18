package com.kirkkd.acousticpt;

import com.kirkkd.RealisticAcoustics;
import org.lwjgl.openal.ALC10;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.alSource3i;
import static org.lwjgl.openal.EXTEfx.*;

public class AudioFilter  {
    static {
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

    private int effectId;
    private int effectSlotId;
    private boolean isReverbCreated = false;

    public static int effectSlotCount = 0;
    private static final int MAX_EFFECT_SLOT_COUNT = 15; // Any higher and the audio will become choppy :(

    public AudioFilter(int sourceId) {
        this.sourceId = sourceId;
    }

    private void createFilterIfNeeded() {
        ignoreALError();

        if (!isFilterCreated) {
            filterId = alGenFilters();
            checkALError();

            isFilterCreated = true;
        }
    }

    private void createReverbIfNeeded() {
        ignoreALError();
        if (!isReverbCreated && effectSlotCount < MAX_EFFECT_SLOT_COUNT) {
            effectId = alGenEffects();
            checkALError();

            effectSlotId = alGenAuxiliaryEffectSlots();
            checkALError();

            alSource3i(sourceId, AL_AUXILIARY_SEND_FILTER, effectSlotId, 0, AL_FILTER_NULL);
            checkALError();

            isReverbCreated = true;

            effectSlotCount++;
        }
    }

    public void lowPass(float gain, float gainHF) {
        createFilterIfNeeded();

        alFilteri(filterId, AL_FILTER_TYPE, AL_FILTER_LOWPASS);
        checkALError();

        alFilterf(filterId, AL_LOWPASS_GAIN, gain);
        checkALError();

        alFilterf(filterId, AL_LOWPASS_GAINHF, gainHF);
        checkALError();

        alSourcei(sourceId, AL_DIRECT_FILTER, filterId);
        checkALError();
    }

    public void reverb(float delay, float percentEcho) {
        if (delay <= 0.1f) return;
        delay = Math.clamp(delay, 0f, 20f);

        createReverbIfNeeded();
        if (isReverbCreated) {
            alEffecti(effectId, AL_EFFECT_TYPE, AL_EFFECT_REVERB);
            alEffectf(effectId, AL_REVERB_DECAY_TIME, delay);
            alEffectf(effectId, AL_REVERB_GAIN, percentEcho);
            checkALError();

            alAuxiliaryEffectSloti(effectSlotId, AL_EFFECTSLOT_EFFECT, effectId);
            catchALError(AL_INVALID_NAME, () -> RealisticAcoustics.LOGGER.error("Invalid Name for effectSlotId"));
        }
    }

    public void destroy() {
        destroy(false);
    }

    public void destroy(boolean instantly) {
        ignoreALError();

        if (isFilterCreated) {
            alDeleteFilters(filterId);
            checkALError();

            isFilterCreated = false;
        }
        if (isReverbCreated) {
            float decayTime = instantly ? 0 : 2000;
            isReverbCreated = false;

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    alDeleteAuxiliaryEffectSlots(effectSlotId);
                    alDeleteEffects(effectId);

                    if (alGetError() == AL_NO_ERROR) effectSlotCount--;
                    else isReverbCreated = true;
                }
            }, (long) decayTime);
        }
    }

    private static void ignoreALError() {
        alGetError();
    }

    private static void checkALError() {
        int err = alGetError();
        if (err != AL_NO_ERROR) {
            throw new RuntimeException("OpenAL error: " + alGetString(err));
        }
    }

    private static void catchALError(int error, Runnable cb) {
        int e = alGetError();
        if (e != AL_NO_ERROR) {
            if (e == error) cb.run();
            else throw new RuntimeException("OpenAL error: " + alGetString(e));
        }
    }
}
