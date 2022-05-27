package com.jelly.CaneBuilder.features;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.utils.BlockUtils;
import com.jelly.CaneBuilder.utils.Clock;
import com.jelly.CaneBuilder.utils.ScoreboardUtils;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Failsafe {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Clock cooldown = new Clock();
    private static boolean teleporting;


    @SubscribeEvent
    public void onMessageReceived(ClientChatReceivedEvent event) {
        String message = net.minecraft.util.StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (BuilderState.enabled) {
            if (message.contains("DYNAMIC") || message.contains("Something went wrong trying to send ") || message.contains("don't spam") || message.contains("A disconnect occurred ") || message.contains("An exception occurred ") || message.contains("Couldn't warp ") || message.contains("You are sending commands ") || message.contains("Cannot join ") || message.contains("There was a problem ") || message.contains("You cannot join ") || message.contains("You were kicked while ") || message.contains("You are already playing") || message.contains("You cannot join SkyBlock from here!")) {
                Utils.addCustomLog("Failed teleport - waiting");
                teleporting = false;
                cooldown.schedule(10000);
            }
        }
    }


    @SubscribeEvent
    public final void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END || mc.thePlayer == null || mc.theWorld == null) return;
        if(!(BuilderState.enabled || BuilderState.isSwitchingLayer)) return;

        switch (Utils.getLocation()) {
            case TELEPORTING:
                teleporting = false;
                return;
            case LIMBO:
                if (cooldown.passed()) {
                    mc.thePlayer.sendChatMessage("/lobby");
                    cooldown.schedule(5000);
                    teleporting = true;
                }
                return;
            case LOBBY:
                if (cooldown.passed()) {
                    mc.thePlayer.sendChatMessage("/skyblock");
                    cooldown.schedule(5000);
                    teleporting = true;
                }
                return;
            case HUB:
                Utils.addCustomMessage("Detected Hub");
                if (cooldown.passed()) {
                    mc.thePlayer.sendChatMessage("/is");
                    cooldown.schedule(5000);
                    teleporting = true;
                }
        }
    }


}
