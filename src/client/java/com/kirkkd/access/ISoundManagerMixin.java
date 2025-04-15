package com.kirkkd.access;

import com.kirkkd.acousticpt.AudioReceiver;
import com.kirkkd.acousticpt.ImageSoundInstance;
import net.minecraft.client.sound.SoundSystem;

public interface ISoundManagerMixin {
    void realistic_acoustics_1_21_5$playImage(ImageSoundInstance soundInstance);

    SoundSystem realistic_acoustics_1_21_5$getSoundSystem();

    AudioReceiver realist_acoustics_1_21_5$getAudioReceiver();
}
