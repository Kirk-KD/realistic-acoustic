package com.kirkkd.acousticpt;

import com.kirkkd.RealisticAcoustics;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AudioSourceGrid {
    public static final int GRID_SIZE = 32;
    public static final int NUM_CELLS = 8;
    public static final int NUM_CELLS_CUBED = NUM_CELLS * NUM_CELLS * NUM_CELLS;
    public static final int CELL_SIZE = GRID_SIZE / NUM_CELLS;
    public static final int HALF_GRID = GRID_SIZE / 2;

    @SuppressWarnings("unchecked")
    private final List<AudioSource>[] grid = new ArrayList[NUM_CELLS_CUBED];
    private final List<AudioSource> outOfRange = new ArrayList<>();

    private final AudioReceiver audioReceiver;

    public AudioSourceGrid(AudioReceiver audioReceiver) {
        this.audioReceiver = audioReceiver;
        initialize();
    }

    public void clearNotPlaying() {
        List<AudioSource> validSources = new ArrayList<>();
        for (int i = 0; i < NUM_CELLS_CUBED; i++) {
            for (AudioSource source : grid[i]) {
                if (source.isPlaying()) validSources.add(source);
                else source.cleanUpAudioFilter();
            }
            grid[i].clear();
        }

        validSources.forEach(this::add);

        Iterator<AudioSource> it = outOfRange.iterator();
        while (it.hasNext()) {
            AudioSource audioSource = it.next();
            if (audioSource.isPlaying()) add(audioSource);
            else {
                audioSource.cleanUpAudioFilter();
                it.remove();
            }
        }
    }

    public void add(AudioSource audioSource) {
        if (isOutOfRange(audioSource.getPosition()) && !outOfRange.contains(audioSource)) outOfRange.add(audioSource);
        else {
            Vec3i cell = posToCellIndex(audioSource.getPosition());
            grid[idx(cell.getX(), cell.getY(), cell.getZ())].add(audioSource);
        }
    }

    public List<AudioSource> getInRadius(Vec3d pos, double radius) {
        if (isOutOfRange(pos)) return List.of();
        return getCell(pos).stream().filter(source -> source.getPosition().isInRange(pos, radius)).toList();
    }

    public int count() {
        int c = 0;
        for (List<AudioSource> cell : grid) c += cell.size();
        return c + outOfRange.size();
    }

    public boolean isEmpty() {
        return count() == 0;
    }

    private void initialize() {
        for (int i = 0; i < NUM_CELLS_CUBED; i++) grid[i] = new ArrayList<>(5);
    }

    private List<AudioSource> getCell(Vec3d pos) {
        Vec3i cell = posToCellIndex(pos);
        return grid[idx(cell.getX(), cell.getY(), cell.getZ())];
    }

    private Vec3i posToCellIndex(Vec3d pos) {
        Vec3d relPos = pos.subtract(audioReceiver.getPosition());

        int cx = (int) Math.floor((relPos.x + HALF_GRID) / CELL_SIZE);
        int cy = (int) Math.floor((relPos.y + HALF_GRID) / CELL_SIZE);
        int cz = (int) Math.floor((relPos.z + HALF_GRID) / CELL_SIZE);

        cx = Math.clamp(cx, 0, NUM_CELLS - 1);
        cy = Math.clamp(cy, 0, NUM_CELLS - 1);
        cz = Math.clamp(cz, 0, NUM_CELLS - 1);

        return new Vec3i(cx, cy, cz);
    }

    private boolean isOutOfRange(Vec3d pos) {
        Vec3d relPos = pos.subtract(audioReceiver.getPosition());
        return Math.abs(relPos.x) > HALF_GRID || Math.abs(relPos.y) > HALF_GRID || Math.abs(relPos.z) > HALF_GRID;
    }

    private static int idx(int x, int y, int z) {
        return x * NUM_CELLS * NUM_CELLS + y * NUM_CELLS + z;
    }
}
