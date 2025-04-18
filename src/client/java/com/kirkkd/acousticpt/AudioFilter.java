package com.kirkkd.acousticpt;

import com.kirkkd.RealisticAcoustics;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.EXTEfx.*;

public class AudioFilter {
    private final int source;
    private boolean destroyed = false;

    private int auxSlot = 0;
    private int reverbEffect = 0;

    public AudioFilter(int source) {
        this.source = source;
    }

    public void reverb(float delay, float percentEcho) {
        if (destroyed) return;

        if (delay <= 0.1f) return;
        delay = Math.clamp(delay, 0f, 20f);

        bindAuxSlotIfNeeded();
        if (auxSlot != 0) {
            createReverbEffectIfNeeded();
            alEffecti(reverbEffect, AL_EFFECT_TYPE, AL_EFFECT_REVERB);
            alEffectf(reverbEffect, AL_REVERB_DECAY_TIME, delay);
            alEffectf(reverbEffect, AL_REVERB_GAIN, percentEcho);
            if (alGetError() != AL_NO_ERROR) throw new RuntimeException("Failed to configure reverb.");

            AuxEffectManager.attachEffectToSlot(auxSlot, reverbEffect);
        } else RealisticAcoustics.LOGGER.warn("No more aux slots");
    }

    public void destroy() {
        if (destroyed) return;

        if (reverbEffect != 0) AuxEffectManager.deleteEffect(reverbEffect);
        if (source != 0) AuxEffectManager.unbindSource(source);

        destroyed = true;
    }

    private void bindAuxSlotIfNeeded() {
        if (auxSlot == 0) auxSlot = AuxEffectManager.bindSource(source);
    }

    private void createReverbEffectIfNeeded() {
        if (reverbEffect == 0) reverbEffect = AuxEffectManager.createEffect();
    }
}