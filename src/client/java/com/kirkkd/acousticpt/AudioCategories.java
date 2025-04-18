package com.kirkkd.acousticpt;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

import java.util.List;

public class AudioCategories {
    private static final List<SoundCategory> IGNORE = List.of(
            SoundCategory.MASTER,
            SoundCategory.MUSIC,
            SoundCategory.VOICE,
            SoundCategory.AMBIENT
    );
    private static final List<String> IGNORE_IDS = List.of(
            "block.sand.idle"
    );
    private static final List<SoundCategory> APPLY_REVERB = List.of(
            SoundCategory.BLOCKS,
            SoundCategory.NEUTRAL,
            SoundCategory.PLAYERS,
            SoundCategory.HOSTILE,
            SoundCategory.RECORDS
    );

    public static boolean shouldIgnore(SoundInstance soundInstance) {
        return IGNORE.contains(soundInstance.getCategory()) || soundInstance.getId().toString().endsWith(".ambient") || IGNORE_IDS.contains(soundInstance.getId().toString());
    }

    public static boolean shouldApplyReverb(SoundInstance soundInstance) {
        return !shouldIgnore(soundInstance) && APPLY_REVERB.contains(soundInstance.getCategory());
    }
}
