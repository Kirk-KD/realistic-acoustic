package com.kirkkd.acousticpt;

import com.kirkkd.RealisticAcousticsClient;
import com.kirkkd.access.ISoundManagerMixin;
import com.kirkkd.util.DebugParticle;
import net.minecraft.particle.*;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ImageAudioSource {
    private final AudioSource originalAudioSource;
    private final ImageSoundInstance imageSoundInstance;

    public ImageAudioSource(AudioSource original, List<AudioReceiver.HitSourceResult> hits) {
        originalAudioSource = original;
        originalAudioSource.getSoundInstance().getSoundSet(RealisticAcousticsClient.SOUND_MANAGER); // initialize .sound
        imageSoundInstance = new ImageSoundInstance(originalAudioSource.getSoundInstance());
        updateImageSoundInstance(hits, false);
    }

    public void updateImageSoundInstance(List<AudioReceiver.HitSourceResult> hits) {
        updateImageSoundInstance(hits, true);
    }

    public void updateImageSoundInstance(List<AudioReceiver.HitSourceResult> hits, boolean smooth) {
        double totalEnergy = hits.stream()
                .mapToDouble(AudioReceiver.HitSourceResult::energy)
                .sum();
        double imageEnergy = Math.clamp(totalEnergy / Config.MAX_ENERGY, 0.0, 1.0);

        List<AudioReceiver.HitSourceResult> echoResults = hits.stream()
                .filter(hit -> hit.lastEcho() != null)
                .toList();
        Vec3d sum = echoResults.stream()
                .map(AudioReceiver.HitSourceResult::lastEcho)
                .reduce(new Vec3d(0.0, 0.0, 0.0), Vec3d::add);
        Vec3d imagePosition = echoResults.isEmpty()
                ? originalAudioSource.getPosition()
                : sum.multiply(1.0 / echoResults.size());

        double imageVolume = originalAudioSource.getSoundInstance().getVolume() * imageEnergy;

        imageSoundInstance.setPositionSmoothly(imagePosition, smooth ? Config.SMOOTH_FACTOR : 1);
        imageSoundInstance.setVolumeSmoothly(imageVolume, smooth ? Config.SMOOTH_FACTOR : 1);
        imageSoundInstance.setEnergySmoothly(imageEnergy, smooth ? Config.SMOOTH_FACTOR : 1);

        echoResults.forEach(result -> DebugParticle.summon(0x00FF00, result.lastEcho()));
        DebugParticle.summon(DustParticleEffect.RED, imagePosition);
    }

    public void play() {
        originalAudioSource.onClaimedByImage(this);
        ((ISoundManagerMixin) RealisticAcousticsClient.SOUND_MANAGER).realistic_acoustics_1_21_5$playImage(imageSoundInstance);
    }

    public void stopOriginalAudioInstance() {
        RealisticAcousticsClient.SOUND_MANAGER.stop(originalAudioSource.getSoundInstance());
    }

    public boolean isPlaying() {
        return RealisticAcousticsClient.SOUND_MANAGER.isPlaying(imageSoundInstance);
    }

    public ImageSoundInstance getImageSoundInstance() {
        return imageSoundInstance;
    }
}
