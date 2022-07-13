package com.jelly.CaneBuilder.features;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.handlers.KeyBindHandler;
import com.jelly.CaneBuilder.handlers.MacroHandler;
import com.jelly.CaneBuilder.utils.Clock;
import com.jelly.CaneBuilder.utils.LogUtils;
import com.jelly.CaneBuilder.utils.ScoreboardUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Failsafe {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Clock tpCooldown = new Clock();


    public static boolean restarting = false;
    public static boolean pauseOnLeave = true;



    @SubscribeEvent
    public void onMessageReceived(ClientChatReceivedEvent event) {
        String message = net.minecraft.util.StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (BuilderState.enabled) {
            if (message.contains("DYNAMIC") || message.contains("Something went wrong trying to send ") || message.contains("don't spam") || message.contains("A disconnect occurred ") || message.contains("An exception occurred ") || message.contains("Couldn't warp ") || message.contains("You are sending commands ") || message.contains("Cannot join ") || message.contains("There was a problem ") || message.contains("You cannot join ") || message.contains("You were kicked while ") || message.contains("You are already playing") || message.contains("You cannot join SkyBlock from here!")) {
                tpCooldown.schedule(10000);
            }
        }
    }

    @SubscribeEvent
    public final void tick(TickEvent.ClientTickEvent event) {
        if (!BuilderState.enabled || event.phase == TickEvent.Phase.END || mc.thePlayer == null || mc.theWorld == null) return;

        if(ScoreboardUtils.getLocation() != ScoreboardUtils.location.ISLAND && !BuilderState.paused && pauseOnLeave){
            MacroHandler.pauseScript();
            KeyBindHandler.resetKeybindState();
        }

        switch (ScoreboardUtils.getLocation()) {
            case TELEPORTING:
                if (tpCooldown.passed()) {
                    mc.thePlayer.sendChatMessage("/lobby");
                    tpCooldown.schedule(15000);
                    LogUtils.addCustomLog("teleporting");
                }
                return;
            case LIMBO:
                if (tpCooldown.passed()) {
                    mc.thePlayer.sendChatMessage("/lobby");
                    tpCooldown.schedule(5000);
                    LogUtils.addCustomLog("limbo");
                }
                return;
            case LOBBY:
                if (tpCooldown.passed()) {
                    mc.thePlayer.sendChatMessage("/skyblock");
                    tpCooldown.schedule(5000);
                    LogUtils.addCustomLog("lobby");
                }
                return;
            case HUB:
                if (tpCooldown.passed()) {
                    mc.thePlayer.sendChatMessage("/is");
                    tpCooldown.schedule(5000);
                    LogUtils.addCustomLog("hub");
                }
                return;
            case ISLAND:
                if (BuilderState.paused && pauseOnLeave && !restarting) {
                    restarting = true;
                    new Thread(() -> {
                        try {
                            mc.inGameHasFocus = true;
                            mc.mouseHelper.grabMouseCursor();
                            mc.displayGuiScreen(null);
                            KeyBindHandler.setKeyBindState(KeyBindHandler.keyBindShift, true);
                            Thread.sleep(1000);
                            KeyBindHandler.setKeyBindState(KeyBindHandler.keyBindShift, false);
                            MacroHandler.continueMacro();
                            restarting = false;

                        }catch (Exception e){e.printStackTrace();}
                    }).start();
                }
        }
    }


}
