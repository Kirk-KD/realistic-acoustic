package com.kirkkd.acousticpt;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import oshi.util.tuples.Pair;

import java.util.Map;

public class Coefficient {
    // (reflection, transmission)
    private static final Map<TagKey<Block>, Pair<Double, Double>> VALUES = Map.of(
            BlockTags.LEAVES, new Pair<>(0.10, 0.70),

            BlockTags.PICKAXE_MINEABLE, new Pair<>(0.9, 0.05),
            BlockTags.AXE_MINEABLE, new Pair<>(0.55, 0.2),
            BlockTags.SHOVEL_MINEABLE, new Pair<>(0.25, 0.4),
            BlockTags.HOE_MINEABLE, new Pair<>(0.2, 0.6)
    );

    public static double ofReflection(BlockState blockState) {
        for (Map.Entry<TagKey<Block>, Pair<Double, Double>> entry : VALUES.entrySet()) {
            if (blockState.isIn(entry.getKey())) return entry.getValue().getA();
        }
        return 0.6;
    }

    public static double ofTransmission(BlockState blockState) {
        for (Map.Entry<TagKey<Block>, Pair<Double, Double>> entry : VALUES.entrySet()) {
            if (blockState.isIn(entry.getKey())) return entry.getValue().getB();
        }
        return 0.3;
    }
}
