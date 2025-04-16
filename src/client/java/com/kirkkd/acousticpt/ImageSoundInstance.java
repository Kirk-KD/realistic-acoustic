package com.kirkkd.acousticpt;

import com.kirkkd.RealisticAcoustics;
import com.kirkkd.RealisticAcousticsClient;
import com.kirkkd.access.ISoundManagerMixin;
import com.kirkkd.access.ISoundSystemMixin;
import com.kirkkd.access.ISourceMixin;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class ImageSoundInstance extends MovingSoundInstance {
    private double energy = 1;

    private final AtomicReference<AudioFilter> audioFilter = new AtomicReference<>();

    protected ImageSoundInstance(SoundInstance original) {
        super(SoundEvent.of(original.getId()), original.getCategory(), SoundInstance.createRandom());
        getSoundSet(RealisticAcousticsClient.SOUND_MANAGER);
    }

    public void applyMuffle(Channel.SourceManager sourceManager) {
        sourceManager.run(source -> {
            ISourceMixin sourceMixin = (ISourceMixin) source;
            int pointer = sourceMixin.realistic_acoustics_1_21_5$getPointer();

            float minGainHF = 0.01f;
            float gain = 1.0f;
            float gainHF = (float) (minGainHF + (gain - minGainHF) * energy);
            audioFilter.set(new AudioFilter(pointer).lowPass(1f, gainHF));

//            RealisticAcoustics.LOGGER.info("low pass {}", energy);
        });
    }

    public void cleanUpAudioFilter() {
        // Currently broken, will fix when necessary.
        if (audioFilter.get() != null) audioFilter.get().destroy();
    }

    @Override
    public void tick() {
        SoundSystem soundSystem = ((ISoundManagerMixin) RealisticAcousticsClient.SOUND_MANAGER).realistic_acoustics_1_21_5$getSoundSystem();
        Channel.SourceManager sourceManager = ((ISoundSystemMixin) soundSystem).realistic_acoustics_1_21_5$getSourceManager(this);

        if (sourceManager == null) RealisticAcoustics.LOGGER.warn("source manager is null");
        else applyMuffle(sourceManager);
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundLoader loader, Identifier id, boolean repeatInstantly) {
        return super.getAudioStream(loader, id, repeatInstantly);
    }

    public void setPositionSmoothly(Vec3d targetPos, double smoothFactor) {
        double newX = this.x + (targetPos.x - this.x) * smoothFactor;
        double newY = this.y + (targetPos.y - this.y) * smoothFactor;
        double newZ = this.z + (targetPos.z - this.z) * smoothFactor;

        setPosition(new Vec3d(newX, newY, newZ));
    }

    public void setPosition(Vec3d position) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
    }

    public void setVolume(double volume) {
        this.volume = (float) volume;
    }

    public void setVolumeSmoothly(double targetVolume, double smoothFactor) {
        double newVolume = this.volume + (targetVolume - this.volume) * smoothFactor;
        setVolume(newVolume);
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public void setEnergySmoothly(double targetEnergy, double smoothFactor) {
        double newEnergy = this.energy + (targetEnergy - this.energy) * smoothFactor;
        setEnergy(newEnergy);
    }
}
