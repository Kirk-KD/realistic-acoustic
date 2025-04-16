package com.kirkkd.mixin.client.sound;

import com.kirkkd.access.ISoundSystemMixin;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(SoundSystem.class)
public class SoundSystemMixin implements ISoundSystemMixin {
    @Shadow @Final private Map<SoundInstance, Channel.SourceManager> sources;

    @Override
    public Channel.SourceManager realistic_acoustics_1_21_5$getSourceManager(SoundInstance soundInstance) {
        return sources.get(soundInstance);
    }
}
