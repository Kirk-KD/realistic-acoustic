package com.kirkkd.mixin.client;

import com.kirkkd.RealisticAcousticsClient;
import com.kirkkd.access.ISoundManagerMixin;
import com.kirkkd.acousticpt.AudioReceiver;
import com.kirkkd.acousticpt.AudioSource;
import com.kirkkd.acousticpt.ImageSoundInstance;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SoundManager.class)
public abstract class SoundManagerMixin implements ISoundManagerMixin {
    @Shadow @Final private SoundSystem soundSystem;

    @Unique
    private final List<SoundCategory> modifiedSoundCategories = List.of(SoundCategory.BLOCKS, SoundCategory.RECORDS, SoundCategory.HOSTILE);

    @Unique
    private AudioReceiver audioReceiver = null;

    @Inject(at = @At("HEAD"), method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", cancellable = true)
    public void play(SoundInstance sound, CallbackInfo ci) {
        if (sound instanceof PositionedSoundInstance positionedSound &&
                modifiedSoundCategories.contains(positionedSound.getCategory()) &&
                !positionedSound.getId().equals(Identifier.of("block.trial_spawner.ambient")) // trial spawners annoying as hell
        ) {
            if (audioReceiver != null) {
                audioReceiver.addSource(new AudioSource(sound));
                ci.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "stop(Lnet/minecraft/client/sound/SoundInstance;)V")
    public void stop(SoundInstance sound, CallbackInfo ci) {
        if (audioReceiver != null)
            audioReceiver.onSoundInstanceStopped(sound);
    }

    @Inject(at = @At("HEAD"), method = "tick(Z)V")
    public void tick(boolean paused, CallbackInfo ci) {
        if (RealisticAcousticsClient.SOUND_MANAGER != null && RealisticAcousticsClient.MC_CLIENT.world != null) {
            if (audioReceiver == null) audioReceiver = new AudioReceiver();
            audioReceiver.update();
        }
    }

    @Override
    public void realistic_acoustics_1_21_5$playImage(ImageSoundInstance soundInstance) {
        soundSystem.play(soundInstance);
    }

    @Override
    public SoundSystem realistic_acoustics_1_21_5$getSoundSystem() {
        return soundSystem;
    }

    @Override
    public AudioReceiver realist_acoustics_1_21_5$getAudioReceiver() {
        return audioReceiver;
    }
}
