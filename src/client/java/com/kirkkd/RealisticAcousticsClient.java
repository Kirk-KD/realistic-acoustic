package com.kirkkd;

import com.kirkkd.access.ISoundManagerMixin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class RealisticAcousticsClient implements ClientModInitializer {
	public static MinecraftClient MC_CLIENT;
	public static SoundManager SOUND_MANAGER = null;

	private static final KeyBinding keyEnabled = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.realistic-acoustics.enable",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_RIGHT_ALT,
			"category.realistic-acoustics.debug"
	));
	private static boolean enabled = true;

	private static final KeyBinding keyDebug = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.realistic-acoustics.debug",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_RIGHT_CONTROL,
			"category.realistic-acoustics.debug"
	));
	private static boolean debug = false;

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isDebug() {
        return debug;
    }

    @Override
	public void onInitializeClient() {
		MC_CLIENT = MinecraftClient.getInstance();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyEnabled.wasPressed()) {
				enabled	= !enabled;
				if (client.player != null)
					client.player.sendMessage(Text.literal((enabled ? "Enabled" : "Disabled") + " Realistic Acoustics"), false);
			}
			while (keyDebug.wasPressed()) {
				debug = !debug;
				if (client.player != null)
					client.player.sendMessage(Text.literal("debug = " + debug), false);
			}
		});
	}

	public static void onJoinWorld() {
		SOUND_MANAGER = MC_CLIENT.getSoundManager();
	}

	public static void onDisconnect() {
		if (SOUND_MANAGER != null) ((ISoundManagerMixin) SOUND_MANAGER).realist_acoustics_1_21_5$getAudioReceiver().clearAudioSourceGrid();
	}
}