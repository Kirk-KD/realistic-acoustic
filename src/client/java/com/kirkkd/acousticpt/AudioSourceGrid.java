package com.kirkkd.acousticpt;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;

public class AudioSourceGrid {
    public static final int GRID_SIZE = 32;
    public static final int NUM_CELLS = 8;
    public static final int CELL_SIZE = GRID_SIZE / NUM_CELLS;
    public static final int HALF_GRID = GRID_SIZE / 2;

    private boolean empty = true;

    @SuppressWarnings("unchecked")
    private final List<AudioSource>[][][] grid = new ArrayList[NUM_CELLS][NUM_CELLS][NUM_CELLS];
    private final List<AudioSource> outOfRange = new ArrayList<>();

    private final AudioReceiver audioReceiver;

    public AudioSourceGrid(AudioReceiver audioReceiver) {
        this.audioReceiver = audioReceiver;
        initialize();
    }

    public void clearNotPlaying() {
        List<AudioSource> stillPlaying = new ArrayList<>();

        for (int x = 0; x < NUM_CELLS; x++) {
            for (int y = 0; y < NUM_CELLS; y++) {
                for (int z = 0; z < NUM_CELLS; z++) {
                    stillPlaying.addAll(grid[x][y][z].stream().filter(AudioSource::isPlaying).toList());
                    grid[x][y][z].clear();
                }
            }
        }

        stillPlaying.forEach(this::add);
        outOfRange.removeIf(audioSource -> !audioSource.isPlaying());
        outOfRange.forEach(this::add);

        empty = stillPlaying.isEmpty() && outOfRange.isEmpty();
    }

    public void add(AudioSource audioSource) {
        if (isOutOfRange(audioSource.getPosition()) && !outOfRange.contains(audioSource)) outOfRange.add(audioSource);
        else {
            Vec3i cell = posToCellIndex(audioSource.getPosition());
            grid[cell.getX()][cell.getY()][cell.getZ()].add(audioSource);
        }
        empty = false;
    }

    public List<AudioSource> getInRadius(Vec3d pos, double radius) {
        if (isOutOfRange(pos)) return List.of();
        return getCell(pos).stream().filter(source -> source.getPosition().isInRange(pos, radius)).toList();
    }

    public boolean isEmpty() {
        return empty;
    }

    private void initialize() {
        for (int x = 0; x < NUM_CELLS; x++) {
            for (int y = 0; y < NUM_CELLS; y++) {
                for (int z = 0; z < NUM_CELLS; z++) {
                    grid[x][y][z] = new ArrayList<>();
                }
            }
        }
    }

    private List<AudioSource> getCell(Vec3d pos) {
        Vec3i cell = posToCellIndex(pos);
        return grid[cell.getX()][cell.getY()][cell.getZ()];
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
}
