package com.kirkkd.acousticpt;

import com.kirkkd.RealisticAcousticsClient;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.CompletableFuture;

public class ImageSoundInstance extends MovingSoundInstance {
    private double energy = 1;

    protected ImageSoundInstance(SoundInstance original) {
        super(SoundEvent.of(original.getId()), original.getCategory(), SoundInstance.createRandom());
        getSoundSet(RealisticAcousticsClient.SOUND_MANAGER);
    }

    @Override
    public void tick() {

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
