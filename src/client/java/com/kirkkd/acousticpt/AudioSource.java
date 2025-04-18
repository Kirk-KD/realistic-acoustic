package com.kirkkd.acousticpt;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.math.Vec3d;

public class AudioSource {
    private final SoundInstance soundInstance;
    private final Vec3d position;
    private ImageAudioSource owner = null;

    public AudioSource(SoundInstance soundInstance) {
        this.soundInstance = soundInstance;
        position = new Vec3d(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
    }

    public void onClaimedByImage(ImageAudioSource owner) {
        this.owner = owner;
    }

    public boolean isPlaying() {
        return owner == null || owner.isPlaying();
    }

    public SoundInstance getSoundInstance() {
        return soundInstance;
    }

    public Vec3d getPosition() {
        return position;
    }

    public void cleanUpAudioFilter() {
        if (owner != null) owner.cleanUpAudioFilter();
    }
}
