package com.kirkkd.mixin.client.sound;

import com.kirkkd.access.ISoundEngineMixin;
import net.minecraft.client.sound.SoundEngine;
import org.lwjgl.openal.ALC10;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.IntBuffer;

import static org.lwjgl.openal.EXTEfx.ALC_MAX_AUXILIARY_SENDS;

@Mixin(SoundEngine.class)
public class SoundEngineMixin implements ISoundEngineMixin {
    @Shadow private long devicePointer;

    @Override
    public long realistic_acoustics_1_21_5$getDevicePointer() {
        return devicePointer;
    }

    @Inject(at = @At("HEAD"), method = "createAttributes(Lorg/lwjgl/system/MemoryStack;Z)Ljava/nio/IntBuffer;", cancellable = true)
    private void createAttributes(MemoryStack stack, boolean directionalAudio, CallbackInfoReturnable<IntBuffer> cir) {
        IntBuffer intBuffer = stack.callocInt(11);
        int j = ALC10.alcGetInteger(this.devicePointer, 6548);
        if (j > 0) {
            intBuffer.put(6546).put(directionalAudio ? 1 : 0);
            intBuffer.put(6550).put(0);
        }

        intBuffer.put(6554).put(1);

        intBuffer.put(ALC_MAX_AUXILIARY_SENDS).put(16);

        cir.setReturnValue(intBuffer.put(0).flip());

        cir.cancel();
    }
}
