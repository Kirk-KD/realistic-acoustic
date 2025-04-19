package com.kirkkd.acousticpt;

import com.kirkkd.RealisticAcousticsClient;
import com.kirkkd.access.ISoundManagerMixin;
import com.kirkkd.util.DebugParticle;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ImageAudioSource {
    private final AudioSource originalAudioSource;
    private final ImageSoundInstance imageSoundInstance;

    public ImageAudioSource(AudioSource original, List<AudioHitResult> hits, List<AudioHitResult> misses) {
        originalAudioSource = original;
        originalAudioSource.getSoundInstance().getSoundSet(RealisticAcousticsClient.SOUND_MANAGER); // initialize .sound
        imageSoundInstance = new ImageSoundInstance(originalAudioSource.getSoundInstance());
        updateImageSoundInstance(hits, misses, false);
    }

    public void updateImageSoundInstance(List<AudioHitResult> hits, List<AudioHitResult> misses) {
        updateImageSoundInstance(hits, misses, true);
    }

    public void updateImageSoundInstance(List<AudioHitResult> hits, List<AudioHitResult> misses, boolean smooth) {
        double imageEnergy = 0;
        Vec3d imagePosition = new Vec3d(0, 0, 0);
        double reverbDelay = 0;

        // Used to calculate the rough "room size"
        int countDirectHits = 0;
        int countEchoHits = 0;
        int countEchoMisses = 0;

        for (AudioHitResult hit : hits) {
            imageEnergy += hit.getEnergy();
            if (hit.getResultType() == AudioHitResult.ResultType.DIRECT) {
                imagePosition = imagePosition.add(originalAudioSource.getPosition());
                reverbDelay += hit.getDistance();
                countDirectHits++;
            } else {
                imagePosition = imagePosition.add(hit.getLastEchoPosition());
                reverbDelay += hit.getLastEchoDistance();
                countEchoHits++;
            }
        }
        for (AudioHitResult miss : misses) {
            if (miss.getResultType() == AudioHitResult.ResultType.DECAYED) {
                reverbDelay += miss.getLastEchoDistance();
                countEchoMisses++;
            }
        }

        if (countEchoHits != 0) {
            reverbDelay = reverbDelay / (countEchoHits + countEchoMisses + countDirectHits) / Config.SPEED_OF_SOUND;
            double percentEcho = (double) (countEchoHits + countEchoMisses) / (hits.size() + misses.size()) - 0.9;
            percentEcho *= 6;
            percentEcho = Math.clamp(percentEcho, 0.0, 1.0);

            imageSoundInstance.setReverbDelay(reverbDelay);
            imageSoundInstance.setReverbGain(percentEcho);
        }

        imageEnergy = Math.clamp(imageEnergy / Config.MAX_ENERGY, 0.0, 1.0);
        imagePosition = hits.isEmpty() ? originalAudioSource.getPosition() : imagePosition.multiply(1.0 / hits.size());
        double imageVolume = originalAudioSource.getSoundInstance().getVolume() * imageEnergy;

        imageSoundInstance.setEnergySmoothly(imageEnergy, smooth ? Config.SMOOTH_FACTOR : 1);
        imageSoundInstance.setPositionSmoothly(imagePosition, smooth ? Config.SMOOTH_FACTOR : 1);
        imageSoundInstance.setVolumeSmoothly(imageVolume, smooth ? Config.SMOOTH_FACTOR : 1);

        hits.forEach(result -> {
            if (result.getResultType() == AudioHitResult.ResultType.ECHO)
                DebugParticle.summon(0x00FF00, result.getLastEchoPosition());
        });
        misses.forEach(result -> {
            if (result.getResultType() == AudioHitResult.ResultType.DECAYED)
                DebugParticle.summon(0x009900, result.getLastEchoPosition());
        });
        DebugParticle.summon(DustParticleEffect.RED, imagePosition);
    }

    public void play() {
        originalAudioSource.onClaimedByImage(this);
        ((ISoundManagerMixin) RealisticAcousticsClient.SOUND_MANAGER).realistic_acoustics_1_21_5$playImage(imageSoundInstance);
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
