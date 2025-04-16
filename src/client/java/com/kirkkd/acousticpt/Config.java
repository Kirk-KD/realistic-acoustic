package com.kirkkd.acousticpt;

public class Config {
    public static final int NUM_RAYS = 2000;
    public static final double MAX_ENERGY_PER_RAY = 0.004;
    public static final double MIN_ENERGY = 0.001;
    public static final int MAX_INTERACTIONS = 10;
    public static final double MAX_DISTANCE = 200;
    public static final double DEFAULT_RAY_ENERGY = 1;
    public static final double MAX_ENERGY = MAX_ENERGY_PER_RAY * NUM_RAYS;
    public static final double SMOOTH_FACTOR = 0.5;
}
