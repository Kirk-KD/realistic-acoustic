package com.kirkkd.acousticpt;

import com.kirkkd.RealisticAcoustics;
import com.kirkkd.RealisticAcousticsClient;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;

public class Ray {
    public static int count = 0;

    private final AudioReceiver audioReceiver;
    private final ClientWorld world;

    private final Vec3d startDirection;
    private final Vec3d startPosition;

    private double energy;
    private final double cumulativeDistance;
    private int interactions;
    private Vec3d lastEcho;
    private final List<AudioSource> hitSources;

    public Ray(AudioReceiver audioReceiver,
               Vec3d startPosition,
               Vec3d startDirection,
               double energy,
               double cumulativeDistance,
               int interactions,
               Vec3d lastEcho,
               List<AudioSource> hitSources) {
        world = RealisticAcousticsClient.MC_CLIENT.world;
        this.audioReceiver = audioReceiver;
        this.startPosition = startPosition;
        this.startDirection = startDirection;
        this.energy = energy;
        this.cumulativeDistance = cumulativeDistance;
        this.interactions = interactions;
        this.lastEcho = lastEcho;
        this.hitSources = hitSources;

        count++;
    }

    public Ray(AudioReceiver audioReceiver, Vec3d startPosition, Vec3d startDirection) {
        this(audioReceiver, startPosition, startDirection, Config.DEFAULT_RAY_ENERGY, 0, 0, null, new ArrayList<>());
    }

    public void cast() {
        if (world == null) {
            RealisticAcoustics.LOGGER.warn("World is null");
            return;
        }

        boolean wasInAir = false;

        Vec3d origin = startPosition;
        Vec3d direction = startDirection.normalize();

        int x = (int)Math.floor(origin.x);
        int y = (int)Math.floor(origin.y);
        int z = (int)Math.floor(origin.z);

        int stepX = direction.x < 0 ? -1 : 1;
        int stepY = direction.y < 0 ? -1 : 1;
        int stepZ = direction.z < 0 ? -1 : 1;

        double nextX = stepX > 0 ? Math.floor(origin.x) + 1 : Math.floor(origin.x);
        double nextY = stepY > 0 ? Math.floor(origin.y) + 1 : Math.floor(origin.y);
        double nextZ = stepZ > 0 ? Math.floor(origin.z) + 1 : Math.floor(origin.z);

        double tMaxX = direction.x != 0 ? (nextX - origin.x) / direction.x : Double.POSITIVE_INFINITY;
        double tMaxY = direction.y != 0 ? (nextY - origin.y) / direction.y : Double.POSITIVE_INFINITY;
        double tMaxZ = direction.z != 0 ? (nextZ - origin.z) / direction.z : Double.POSITIVE_INFINITY;

        double tDeltaX = direction.x != 0 ? 1.0 / Math.abs(direction.x) : Double.POSITIVE_INFINITY;
        double tDeltaY = direction.y != 0 ? 1.0 / Math.abs(direction.y) : Double.POSITIVE_INFINITY;
        double tDeltaZ = direction.z != 0 ? 1.0 / Math.abs(direction.z) : Double.POSITIVE_INFINITY;

        double t = 0;

        while (energy > Config.MIN_ENERGY && interactions < Config.MAX_INTERACTIONS && cumulativeDistance + t < Config.MAX_DISTANCE) {
            // advance ray
            Direction hitFace;
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    hitFace = stepX > 0 ? Direction.WEST : Direction.EAST;
                    x += stepX;
                    t = tMaxX;
                    tMaxX += tDeltaX;
                } else {
                    hitFace = stepZ > 0 ? Direction.NORTH : Direction.SOUTH;
                    z += stepZ;
                    t = tMaxZ;
                    tMaxZ += tDeltaZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    hitFace = stepY > 0 ? Direction.DOWN : Direction.UP;
                    y += stepY;
                    t = tMaxY;
                    tMaxY += tDeltaY;
                } else {
                    hitFace = stepZ > 0 ? Direction.NORTH : Direction.SOUTH;
                    z += stepZ;
                    t = tMaxZ;
                    tMaxZ += tDeltaZ;
                }
            }

            // current pos is `t` distance in the `direction`
            Vec3d currentPos = startPosition.add(direction.multiply(t));
            List<AudioSource> audioSourcesInRange = audioReceiver.getSourceInRadius(currentPos, 1);
            if (!audioSourcesInRange.isEmpty()) {
                for (AudioSource source : audioSourcesInRange) {
                    if (hitSources.contains(source)) continue;

                    hitSources.add(source);
                    lastEcho = lastEcho == null ? source.getPosition() : lastEcho;
                    audioReceiver.onRayHitSource(
                            source,
                            new AudioReceiver.HitSourceResult(
                                    energy,
                                    cumulativeDistance + t,
                                    lastEcho,
                                    lastEcho == null ? -1 : lastEcho.distanceTo(audioReceiver.getPosition()),
                                    interactions == 0
                            )
                    );
                }
            }

            BlockPos blockPos = new BlockPos(x, y, z);
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.isSolidBlock(world, blockPos)) {
                interactions++;

                if (wasInAir) {
                    Vec3d newDirection = reflect(direction, hitFace);
                    Vec3d echoPosition = currentPos.add(newDirection.multiply(0.01));

                    // check direct line-of-sight for last echo
                    BlockHitResult echoRaycastResult = world.raycast(
                            new RaycastContext(echoPosition, audioReceiver.getPosition(), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, ShapeContext.absent()));
                    if (echoRaycastResult.getType() != HitResult.Type.BLOCK) lastEcho = echoPosition;

                    // reflect
                    new Ray(audioReceiver, echoPosition, newDirection,
                            energy * Coefficient.ofReflection(blockState),
                            cumulativeDistance + t, interactions, lastEcho, hitSources).cast();

                    wasInAir = false;
                }

                // transmit through the block
                energy *= Coefficient.ofTransmission(blockState);
            } else wasInAir = true;
        }
    }

    private Vec3d reflect(Vec3d in, Direction face) {
        return switch (face) {
            case EAST, WEST -> new Vec3d(-in.x, in.y, in.z);
            case UP, DOWN -> new Vec3d(in.x, -in.y, in.z);
            case NORTH, SOUTH -> new Vec3d(in.x, in.y, -in.z);
        };
    }
}
