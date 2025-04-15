package com.kirkkd;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;

public class RealisticAcousticsClient implements ClientModInitializer {
	public static MinecraftClient MC_CLIENT;
	public static SoundManager SOUND_MANAGER = null;

	@Override
	public void onInitializeClient() {
		MC_CLIENT = MinecraftClient.getInstance();
	}

	public static void onJoinWorld() {
		SOUND_MANAGER = MC_CLIENT.getSoundManager();
	}

	public static void onDisconnect() {

	}
}