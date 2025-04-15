package com.kirkkd;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import org.lwjgl.system.MemoryStack;

import java.io.Console;

import static org.lwjgl.openal.ALC10.*;

public class RealisticAcousticsClient implements ClientModInitializer {
	public static MinecraftClient MC_CLIENT;
	public static SoundManager SOUND_MANAGER = null;

	@Override
	public void onInitializeClient() {
		MC_CLIENT = MinecraftClient.getInstance();
	}

	public static void onJoinWorld() {
		SOUND_MANAGER = MC_CLIENT.getSoundManager();

		try (MemoryStack stack = MemoryStack.stackPush()) {
			long device = alcGetContextsDevice(alcGetCurrentContext());
			boolean efxSupported = alcIsExtensionPresent(device, "ALC_EXT_EFX");
			if (!efxSupported) {
				throw new IllegalStateException("EFX extension not supported by this OpenAL implementation.");
			}

			RealisticAcoustics.LOGGER.info("EFX is supported!");
		}

	}

	public static void onDisconnect() {

	}
}