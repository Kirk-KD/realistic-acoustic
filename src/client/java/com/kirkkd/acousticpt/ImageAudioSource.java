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

        Vec3d sum = hits.stream()
                .map(AudioReceiver.HitSourceResult::lastEcho)
                .reduce(new Vec3d(0.0, 0.0, 0.0), Vec3d::add);
        Vec3d imagePosition = hits.isEmpty()
                ? originalAudioSource.getPosition()
                : sum.multiply(1.0 / hits.size());

        double imageVolume = originalAudioSource.getSoundInstance().getVolume() * imageEnergy;

        List<AudioReceiver.HitSourceResult> nonDirectHits = hits.stream()
                .filter(hitSourceResult -> !hitSourceResult.isDirectHit())
                .toList();

        if (!nonDirectHits.isEmpty()) {
            double reverbDelay = nonDirectHits.stream()
                    .mapToDouble(AudioReceiver.HitSourceResult::lastEchoDistance)
                    .sum() / nonDirectHits.size() / Config.SPEED_OF_SOUND;
            double percentEcho = (double) nonDirectHits.size() / hits.size();

            imageSoundInstance.setReverbDelay(reverbDelay);
            imageSoundInstance.setReverbGain(percentEcho);
        }

        imageSoundInstance.setPositionSmoothly(imagePosition, smooth ? Config.SMOOTH_FACTOR : 1);
        imageSoundInstance.setVolumeSmoothly(imageVolume, smooth ? Config.SMOOTH_FACTOR : 1);
        imageSoundInstance.setEnergySmoothly(imageEnergy, smooth ? Config.SMOOTH_FACTOR : 1);

        hits.forEach(result -> DebugParticle.summon(0x00FF00, result.lastEcho()));
        DebugParticle.summon(DustParticleEffect.RED, imagePosition);
    }

    public void play() {
        originalAudioSource.onClaimedByImage(this);
        ((ISoundManagerMixin) RealisticAcousticsClient.SOUND_MANAGER).realistic_acoustics_1_21_5$playImage(imageSoundInstance);
    }

    public void stopOriginalAudioInstance() {
//        cleanUpAudioFilter();
        RealisticAcousticsClient.SOUND_MANAGER.stop(originalAudioSource.getSoundInstance());
    }

    public void cleanUpAudioFilter() {
        imageSoundInstance.cleanUpAudioFilter();
    }

    public boolean isPlaying() {
        return RealisticAcousticsClient.SOUND_MANAGER.isPlaying(imageSoundInstance);
    }

    public ImageSoundInstance getImageSoundInstance() {
        return imageSoundInstance;
    }
}
