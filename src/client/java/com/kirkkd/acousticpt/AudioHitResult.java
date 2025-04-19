package com.kirkkd.acousticpt;

import net.minecraft.util.math.Vec3d;

public class AudioHitResult {
    public enum ResultType {
        MISSED,
        DECAYED,
        DIRECT,
        ECHO
    }

    private final ResultType resultType;
    private final double energy;
    private final double distance;
    private final Vec3d lastEchoPosition;
    private final double lastEchoDistance;

    public AudioHitResult(ResultType resultType, double energy, double distance, Vec3d lastEchoPosition, double lastEchoDistance) {
        this.resultType = resultType;
        this.energy = energy;
        this.distance = distance;
        this.lastEchoPosition = lastEchoPosition;
        this.lastEchoDistance = lastEchoDistance;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public double getEnergy() {
        return energy;
    }

    public double getDistance() {
        return distance;
    }

    public Vec3d getLastEchoPosition() {
        return lastEchoPosition;
    }

    public double getLastEchoDistance() {
        return lastEchoDistance;
    }
}
