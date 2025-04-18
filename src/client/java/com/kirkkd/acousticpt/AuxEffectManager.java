package com.kirkkd.acousticpt;

import com.kirkkd.RealisticAcoustics;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.alSource3i;
import static org.lwjgl.openal.EXTEfx.*;

public class AuxEffectManager {
    private static final int MAX_AUX_SLOTS = 15;

    private static final int[] auxEffectSlots = new int[MAX_AUX_SLOTS];
    private static final int[] auxEffectSlotSources = new int[MAX_AUX_SLOTS];
    private static int auxEffectSlotCount;

    static {
        ignoreError();
        for (auxEffectSlotCount = 0; auxEffectSlotCount < MAX_AUX_SLOTS; auxEffectSlotCount++) {
            auxEffectSlots[auxEffectSlotCount] = alGenAuxiliaryEffectSlots();
            auxEffectSlotSources[auxEffectSlotCount] = 0;
            if (alGetError() != AL_NO_ERROR) break;
        }
        RealisticAcoustics.LOGGER.info("Created aux effect slots: {}", auxEffectSlotCount);
    }

    public static int bindSource(int source) {
        ignoreError();

        // Tries to bind a source to an aux effect slot, if available. Returns the ID of the aux effect slot.
        for (int i = 0; i < auxEffectSlotCount; i++) {
            if (auxEffectSlotSources[i] == 0) { // available
                auxEffectSlotSources[i] = source;
                alSource3i(source, AL_AUXILIARY_SEND_FILTER, auxEffectSlots[i], 0, 0);
                checkError("Failed to bind source.");

                return auxEffectSlots[i];
            }
        }
        return 0;
    }

    public static void unbindSource(int source) {
        ignoreError();

        for (int i = 0; i < auxEffectSlotCount; i++) {
            if (auxEffectSlotSources[i] == source) {
                auxEffectSlotSources[i] = 0;
                return;
            }
        }
    }

    public static int createEffect() {
        ignoreError();

        int effect = alGenEffects();
        checkError("Failed to create effect.");
        return effect;
    }

    public static void deleteEffect(int effect) {
        ignoreError();

        alDeleteEffects(effect);
        checkErrorSoft("Failed to delete effect.");
    }

    public static void attachEffectToSlot(int slot, int effect) {
        ignoreError();

        alAuxiliaryEffectSloti(slot, AL_EFFECTSLOT_EFFECT, effect);
        checkError("Failed to attach effect to slot.");
    }

    private static void checkError(String msg) {
        int error = alGetError();
        if (error != AL_NO_ERROR) throw new RuntimeException(alGetString(error) + ": " + msg);
    }

    private static void checkErrorSoft(String msg) {
        int error = alGetError();
        if (error != AL_NO_ERROR) RealisticAcoustics.LOGGER.warn("{}: {}", alGetString(error), msg);
    }

    private static void ignoreError() {
        alGetError();
    }
}
