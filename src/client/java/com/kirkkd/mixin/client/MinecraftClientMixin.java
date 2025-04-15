package com.kirkkd.mixin.client;

import com.kirkkd.RealisticAcousticsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Inject(at = @At("HEAD"), method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/gui/screen/DownloadingTerrainScreen$WorldEntryReason;)V")
	public void joinWorld(ClientWorld world, DownloadingTerrainScreen.WorldEntryReason worldEntryReason, CallbackInfo ci) {
		RealisticAcousticsClient.onJoinWorld();
	}

	@Inject(at = @At("HEAD"), method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V")
	public void disconnect(Screen disconnectionScreen, boolean transferring, CallbackInfo ci) {
		RealisticAcousticsClient.onDisconnect();
	}
}