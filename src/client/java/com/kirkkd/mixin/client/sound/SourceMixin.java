package com.kirkkd.mixin.client.sound;

import com.kirkkd.access.ISourceMixin;
import net.minecraft.client.sound.Source;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Source.class)
public class SourceMixin implements ISourceMixin {
    @Shadow @Final private int pointer;

    @Override
    public int realistic_acoustics_1_21_5$getPointer() {
        return pointer;
    }
}
