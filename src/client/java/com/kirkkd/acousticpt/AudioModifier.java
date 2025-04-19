package com.kirkkd.acousticpt;

import com.kirkkd.RealisticAcoustics;

import static org.lwjgl.openal.AL10.AL_NO_ERROR;
import static org.lwjgl.openal.AL10.alGetError;
import static org.lwjgl.openal.EXTEfx.*;

public class AudioModifier {
    private final int source;
    private boolean destroyed = false;

    private int auxSlot = 0;
    private int reverbEffect = 0;
    private int lowPassFilter = 0;

    public AudioModifier(int source) {
        this.source = source;
    }

    public void lowPass(float gainHF) {
        if (destroyed) return;

        createLowPassFilterIfNeeded();

        alFilteri(lowPassFilter, AL_FILTER_TYPE, AL_FILTER_LOWPASS);
        alFilterf(lowPassFilter, AL_LOWPASS_GAIN, 1);
        alFilterf(lowPassFilter, AL_LOWPASS_GAINHF, gainHF);
        if (alGetError() != AL_NO_ERROR) throw new RuntimeException("Failed to configure low pass.");

        AuxEffectManager.attachFilterToSource(source, auxSlot, lowPassFilter);
    }

    public void reverb(float delay, float percentEcho) {
        if (destroyed) return;

        if (delay <= 0.1f) return;
        delay = Math.clamp(delay, 0f, 20f);

        if (bindAuxSlotIfNeeded()) {
            createReverbEffectIfNeeded();

            alEffecti(reverbEffect, AL_EFFECT_TYPE, AL_EFFECT_REVERB);
            alEffectf(reverbEffect, AL_REVERB_DECAY_TIME, delay);
            alEffectf(reverbEffect, AL_REVERB_GAIN, percentEcho);
            if (alGetError() != AL_NO_ERROR) throw new RuntimeException("Failed to configure reverb.");

            AuxEffectManager.attachEffectToSlot(auxSlot, reverbEffect);
        }
    }

    public void destroy() {
        if (destroyed) return;

        if (reverbEffect != 0) AuxEffectManager.deleteEffect(reverbEffect);
        if (lowPassFilter != 0) AuxEffectManager.deleteFilter(lowPassFilter);
        if (source != 0) AuxEffectManager.unbindSource(source);

        destroyed = true;
    }

    private boolean bindAuxSlotIfNeeded() {
        if (auxSlot == 0) auxSlot = AuxEffectManager.bindSource(source);
        if (auxSlot == 0) {
            RealisticAcoustics.LOGGER.warn("No more aux slots");
            return false;
        }
        return true;
    }

    private void createReverbEffectIfNeeded() {
        if (reverbEffect == 0) reverbEffect = AuxEffectManager.createEffect();
    }

    private void createLowPassFilterIfNeeded() {
        if (lowPassFilter == 0) lowPassFilter = AuxEffectManager.createFilter();
    }
}