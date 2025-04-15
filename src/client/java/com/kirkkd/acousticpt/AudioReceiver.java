package com.kirkkd.acousticpt;

import com.kirkkd.RealisticAcousticsClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundListenerTransform;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AudioReceiver {
    public record HitSourceResult(double energy, double distance, Vec3d lastEcho) {}

    private final AudioSourceGrid audioSourceGrid;
    private SoundListenerTransform soundListenerTransform = null;
    private final Map<AudioSource, List<HitSourceResult>> hitSourceResults = new ConcurrentHashMap<>();
    private final Map<AudioSource, ImageAudioSource> imageAudioSources = new HashMap<>();

    private static final List<Vec3d> DIRECTIONS = generateDirections();

    public AudioReceiver() {
        audioSourceGrid = new AudioSourceGrid(this);
    }

    public void addSource(AudioSource source) {
        audioSourceGrid.add(source);
    }

    public List<AudioSource> getSourceInRadius(Vec3d pos, double radius) {
        return audioSourceGrid.getInRadius(pos, radius);
    }

    public void update() {
        soundListenerTransform = RealisticAcousticsClient.SOUND_MANAGER.getListenerTransform();

        if (audioSourceGrid.isEmpty()) return;

        Ray.count = 0; // DEBUG

        castRays();
        playImageAudioSources();

        audioSourceGrid.clearNotPlaying();
        hitSourceResults.clear();
    }

    public void onRayHitSource(AudioSource source, HitSourceResult result) {
        if (result.energy < Config.MIN_ENERGY) return;
        hitSourceResults.computeIfAbsent(source, k -> Collections.synchronizedList(new ArrayList<>())).add(result);
    }

    public void onSoundInstanceStopped(SoundInstance sound) {
        for (Map.Entry<AudioSource, ImageAudioSource> entry : imageAudioSources.entrySet()) {
            if (entry.getKey().getSoundInstance().equals(sound))
                RealisticAcousticsClient.SOUND_MANAGER.stop(entry.getValue().getImageSoundInstance());
        }
    }

    public Vec3d getPosition() {
        return soundListenerTransform.position();
    }

    private void castRays() {
        Vec3d pos = getPosition();
        DIRECTIONS.parallelStream().forEach(direction -> {
            Ray ray = new Ray(this, pos, direction);
            ray.cast();
        });
    }

    private void playImageAudioSources() {
        for (Map.Entry<AudioSource, List<HitSourceResult>> entry : hitSourceResults.entrySet()) {
            AudioSource source = entry.getKey();
            List<HitSourceResult> results = entry.getValue();

            if (imageAudioSources.containsKey(source)) {
                ImageAudioSource imageAudioSource = imageAudioSources.get(source);
                if (!imageAudioSource.isPlaying()) imageAudioSource.stopOriginalAudioInstance();
                imageAudioSource.updateImageSoundInstance(results);
            } else {
                ImageAudioSource imageAudioSource = new ImageAudioSource(source, results);
                imageAudioSources.put(source, imageAudioSource);
                imageAudioSource.play();
            }
        }

        imageAudioSources.entrySet().removeIf(entry -> !entry.getValue().isPlaying());
    }

    private static List<Vec3d> generateDirections() {
        List<Vec3d> directions = new ArrayList<>(Config.NUM_RAYS);
        double goldenAngle = Math.PI * (3 - Math.sqrt(5));

        for (int i = 0; i < Config.NUM_RAYS; i++) {
            double y = 1 - (i / (double)(Config.NUM_RAYS - 1)) * 2;
            double radius = Math.sqrt(1 - y * y);

            double theta = goldenAngle * i;
            double x = Math.cos(theta) * radius;
            double z = Math.sin(theta) * radius;

            directions.add(new Vec3d(x, y, z));
        }
        return directions;
    }
}
