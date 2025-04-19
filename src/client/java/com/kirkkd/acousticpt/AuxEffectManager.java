package com.kirkkd.acousticpt;

import com.kirkkd.RealisticAcoustics;
import com.kirkkd.RealisticAcousticsClient;
import com.kirkkd.access.ISoundEngineMixin;
import com.kirkkd.access.ISoundManagerMixin;
import com.kirkkd.access.ISoundSystemMixin;
import net.minecraft.client.sound.SoundEngine;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.alSource3i;
import static org.lwjgl.openal.ALC10.alcGetIntegerv;
import static org.lwjgl.openal.EXTEfx.*;

public class AuxEffectManager {
    private static final int MAX_AUX_SLOTS = 12;

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
        SoundEngine soundEngine = ((ISoundSystemMixin) ((ISoundManagerMixin) RealisticAcousticsClient.SOUND_MANAGER).realistic_acoustics_1_21_5$getSoundSystem()).realistic_acoustics_1_21_5$getSoundEngine();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer intBuffer = stack.mallocInt(1);
            long device = ((ISoundEngineMixin) soundEngine).realistic_acoustics_1_21_5$getDevicePointer();
            alcGetIntegerv(device, ALC_MAX_AUXILIARY_SENDS, intBuffer);
            RealisticAcoustics.LOGGER.info("Max aux sends per source: {}", intBuffer.get(0));
        }
    }

    // SOURCE

    public static int bindSource(int source) {
        ignoreError();

        // Tries to bind a source to an aux effect slot, if available. Returns the ID of the aux effect slot.
        for (int i = 0; i < auxEffectSlotCount; i++) {
            if (auxEffectSlotSources[i] == 0) { // available
                auxEffectSlotSources[i] = source;
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

    // EFFECT

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

    // FILTER

    public static int createFilter() {
        ignoreError();

        int filter = alGenFilters();
        checkError("Failed to create filter.");
        return filter;
    }

    public static void deleteFilter(int filter) {
        ignoreError();

        alDeleteFilters(filter);
        checkError("Failed to delete aux filter.");
    }

    public static void attachFilterToSource(int source, int slot, int filter) {
        ignoreError();

        alSourcei(source, AL_DIRECT_FILTER, filter);
        alSource3i(source, AL_AUXILIARY_SEND_FILTER, slot, 0, filter);
        checkError("Failed to attach aux filter.");
    }

    // ERROR

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
