package com.kirkkd.util;

import com.kirkkd.RealisticAcousticsClient;
import net.minecraft.text.Text;

public class DebugMessage {
    public static void overlay(String t) {
        if (RealisticAcousticsClient.MC_CLIENT.player != null)
            RealisticAcousticsClient.MC_CLIENT.player.sendMessage(Text.literal(t), true);
    }
}
