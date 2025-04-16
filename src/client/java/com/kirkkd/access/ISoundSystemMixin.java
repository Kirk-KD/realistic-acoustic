package com.kirkkd.access;

import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;

public interface ISoundSystemMixin {
    Channel.SourceManager realistic_acoustics_1_21_5$getSourceManager(SoundInstance soundInstance);
}
