package com.kirkkd.acousticpt;

import com.kirkkd.RealisticAcousticsClient;
import com.kirkkd.util.DebugMessage;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundListenerTransform;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AudioReceiver {
    private final AudioSourceGrid audioSourceGrid;
    private SoundListenerTransform soundListenerTransform = null;
    private final Map<AudioSource, List<AudioHitResult>> hitSourceResults = new ConcurrentHashMap<>();
    private final List<AudioHitResult> missSourceResults = Collections.synchronizedList(new ArrayList<>());
    private final Map<AudioSource, ImageAudioSource> imageAudioSources = new HashMap<>();

    private static List<Vec3d> DIRECTIONS = null;

    public AudioReceiver() {
        audioSourceGrid = new AudioSourceGrid(this);
    }

    public void addSource(AudioSource source) {
        if (RealisticAcousticsClient.isEnabled())
            audioSourceGrid.add(source);
    }

    public List<AudioSource> getSourceInRadius(Vec3d pos, double radius) {
        return audioSourceGrid.getInRadius(pos, radius);
    }

    public void update() {
        if (DIRECTIONS == null) DIRECTIONS = generateDirections();

        soundListenerTransform = RealisticAcousticsClient.SOUND_MANAGER.getListenerTransform();

        if (!audioSourceGrid.isEmpty()) {
            Ray.count = 0; // DEBUG
            castRays();
        }

        playImageAudioSources();

        DebugMessage.overlay("count: " + audioSourceGrid.count());

        audioSourceGrid.clearNotPlaying();
        hitSourceResults.clear();
        missSourceResults.clear();
    }

    public void onRayHitSource(AudioSource source, AudioHitResult result) {
        if (result.getEnergy() < Config.MIN_ENERGY) return;
        hitSourceResults.computeIfAbsent(source, k -> Collections.synchronizedList(new ArrayList<>())).add(result);
    }

    public void onRayMissSource(AudioHitResult result) {
        missSourceResults.add(result);
    }

    public void onSoundInstanceStopped(SoundInstance sound) {
        for (Map.Entry<AudioSource, ImageAudioSource> entry : imageAudioSources.entrySet()) {
            if (entry.getKey().getSoundInstance().equals(sound)) {
                ImageAudioSource imageAudioSource = entry.getValue();
                RealisticAcousticsClient.SOUND_MANAGER.stop(imageAudioSource.getImageSoundInstance());
            }
        }
    }

    public Vec3d getPosition() {
        return soundListenerTransform.position();
    }

    public void clearAudioSourceGrid() {
        audioSourceGrid.clear();
    }

    private void castRays() {
        if (DIRECTIONS == null) return;

        Vec3d pos = getPosition();
        DIRECTIONS.parallelStream().forEach(direction -> {
            Ray ray = new Ray(this, pos, direction);
            ray.cast();
        });
    }

    private void playImageAudioSources() {
        for (Map.Entry<AudioSource, List<AudioHitResult>> entry : hitSourceResults.entrySet()) {
            AudioSource source = entry.getKey();
            List<AudioHitResult> results = entry.getValue();

            if (imageAudioSources.containsKey(source)) {
                ImageAudioSource imageAudioSource = imageAudioSources.get(source);
                if (imageAudioSource.isPlaying()) imageAudioSource.updateImageSoundInstance(results, missSourceResults);
            } else {
                ImageAudioSource imageAudioSource = new ImageAudioSource(source, results, missSourceResults);
                imageAudioSources.put(source, imageAudioSource);
                imageAudioSource.play();
            }
        }

        Iterator<Map.Entry<AudioSource, ImageAudioSource>> it = imageAudioSources.entrySet().iterator();
        while (it.hasNext()) {
            ImageAudioSource imageAudioSource = it.next().getValue();
            if (!imageAudioSource.isPlaying()) it.remove();
        }
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
