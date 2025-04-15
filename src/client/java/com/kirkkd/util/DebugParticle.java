package com.kirkkd.util;

import com.kirkkd.RealisticAcousticsClient;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.Vec3d;

public class DebugParticle {
    public static void summon(int c, Vec3d p) {
        if (RealisticAcousticsClient.isDebug() && RealisticAcousticsClient.MC_CLIENT.world != null) {
            RealisticAcousticsClient.MC_CLIENT.world.addParticleClient(
                    new DustParticleEffect(c, 1),
                    p.x, p.y, p.z,
                    0, 0, 0
            );
        }
    }
}
