package com.jelly.CaneBuilder;

import com.jelly.CaneBuilder.commands.Corner1;
import com.jelly.CaneBuilder.commands.Corner2;
import com.jelly.CaneBuilder.commands.SetDirection;
import com.jelly.CaneBuilder.config.Config;
import com.jelly.CaneBuilder.processes.*;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
 ** @author JellyLab, Polycrylate
 */

@Mod(modid = CaneBuilder.MODID, version = CaneBuilder.VERSION)
public class CaneBuilder {
    public static final String MODID = "canebuilder";
    public static final String NAME = "Cane Builder";
    public static final String VERSION = "2.0";

    public static List<ProcessModule> processes = new ArrayList<>();
    public static Minecraft mc = Minecraft.getMinecraft();

    static boolean inFailsafe;
    static boolean error;

    MouseHelper mouseHelper = new MouseHelper();

    @Mod.EventHandler
    public static void init(FMLInitializationEvent event) {
        try {
            Config.readConfig();
        } catch (Exception e) {
            System.out.println("Error reading config file");
        }
        System.out.println("Registering");
        MinecraftForge.EVENT_BUS.register(new CaneBuilder());
        FMLCommonHandler.instance().bus().register(new CaneBuilder());
        ClientCommandHandler.instance.registerCommand(new SetDirection());
        ClientCommandHandler.instance.registerCommand(new Corner1());
        ClientCommandHandler.instance.registerCommand(new Corner2());
        KeyBindHelper.initializeCustomKeybindings();

        processes.add(new PlaceDirt1());
        processes.add(new PlaceDirt2());
        processes.add(new PlaceDirt3());
        processes.add(new PlaceDirt4());
        processes.add(new DigTrench());
        processes.add(new FillTrench());
        processes.add(new PlaceDirt5());
        processes.add(new PlaceDirt6());
        processes.add(new DigPath1());
        processes.add(new DigPath2());
        processes.add(new PlaceSC());
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        KeyBindHelper.onKeyPress(event);
    }

    @SubscribeEvent
    public void onTickPlayer(TickEvent.ClientTickEvent tickEvent) {
        if (!BuilderState.enabled || mc.thePlayer == null || mc.theWorld == null || tickEvent.phase != TickEvent.Phase.START)
            return;

        for (ProcessModule process : processes) {
            if (process.isEnabled()) {
                process.onTick();
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(final RenderWorldLastEvent event) {
        if (!BuilderState.enabled || mc.thePlayer == null || mc.theWorld == null) return;
        for (ProcessModule process : processes) {
            if (process.isEnabled()) {
                process.onRenderWorld();
            }
        }
    }

    @SubscribeEvent
    public void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        System.out.println("Detecting leave, disabling");
        System.out.println("Detecting leave, disabling");
        for (ProcessModule process : processes) {
            if (process.isEnabled()) {
                process.toggle();
                process.onDisable();
            }
        }
        disableScript();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void render(RenderGameOverlayEvent event) {
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            int topPad = 0;
            if (mc.gameSettings.showDebugInfo) {
                topPad = (new ScaledResolution(mc).getScaledHeight() / 2) - 50;
            }
            Utils.drawStringWithShadow(
              EnumChatFormatting.DARK_GREEN + "-- " + EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.BOLD + "CANE BUILDER" + EnumChatFormatting.DARK_GREEN + " --", 4, topPad + 25, 1f, -1);
            Utils.drawStringWithShadow(
              EnumChatFormatting.WHITE + "" + "Corner 1: " + (BuilderState.corner1 != null ? EnumChatFormatting.GREEN + BuilderState.corner1.toString() : EnumChatFormatting.RED + "Not set"), 4, topPad + 40, 1f, -1);
            Utils.drawStringWithShadow(
              EnumChatFormatting.WHITE + "" + "Corner 2: " + (BuilderState.corner2 != null ? EnumChatFormatting.GREEN + BuilderState.corner2.toString() : EnumChatFormatting.RED + "Not set"), 4, topPad + 50, 1f, -1);
            Utils.drawStringWithShadow(
              EnumChatFormatting.WHITE + "" + "Direction: " + (BuilderState.direction != -1 ? EnumChatFormatting.GREEN + (BuilderState.direction == 0 ? "North / South" : "East / West") : EnumChatFormatting.RED + "Not set"), 4, topPad + 60, 1f, -1);
        }
    }

    @SubscribeEvent
    public void onMessageReceived(ClientChatReceivedEvent event) {
        if (event.message.getFormattedText().contains("You were spawned in Limbo") && !inFailsafe && BuilderState.enabled) {
            activateFailsafe();
            ScheduleRunnable(LeaveSBIsland, 8, TimeUnit.SECONDS);
        }
        if ((event.message.getFormattedText().contains("Sending to server") && !inFailsafe && BuilderState.enabled)) {
            activateFailsafe();
            ScheduleRunnable(WarpHome, 10, TimeUnit.SECONDS);
        }
        if ((event.message.getFormattedText().contains("DYNAMIC") || (event.message.getFormattedText().contains("Couldn't warp you")) && inFailsafe)) {
            error = true;
        }
        if ((event.message.getFormattedText().contains("SkyBlock Lobby") && !inFailsafe && BuilderState.enabled)) {
            activateFailsafe();
            ScheduleRunnable(LeaveSBIsland, 10, TimeUnit.SECONDS);
        }
    }

    public static void switchToNextProcess(ProcessModule currentModule) {
        Utils.addCustomLog("Switching processes");
        KeyBindHelper.updateKeys(false, false, false, false, false, false, false);
        for (int i = 0; i < processes.size(); i++) {
            if (processes.get(i).equals(currentModule)) {
                processes.get(i).toggle();
                processes.get(i).onDisable();
                if (i < processes.size() - 1) {
                    processes.get(i + 1).onEnable();
                    processes.get(i + 1).toggle();
                } else {
                    Utils.addCustomMessage("Completed Layer!", EnumChatFormatting.GREEN);
                    disableScript();
                }
            }
        }
    }

    Runnable LeaveSBIsland = new Runnable() {
        @Override
        public void run() {
            mc.thePlayer.sendChatMessage("/l");
            ScheduleRunnable(Rejoin, 5, TimeUnit.SECONDS);
        }
    };

    Runnable WarpHub = new Runnable() {
        @Override
        public void run() {
            mc.thePlayer.sendChatMessage("/warp hub");
            ScheduleRunnable(WarpHome, 5, TimeUnit.SECONDS);
        }
    };

    Runnable Rejoin = new Runnable() {
        @Override
        public void run() {
            mc.thePlayer.sendChatMessage("/play sb");
            ScheduleRunnable(WarpHome, 5, TimeUnit.SECONDS);
        }
    };

    Runnable WarpHome = new Runnable() {
        @Override
        public void run() {
            mc.thePlayer.sendChatMessage("/warp home");
            ScheduleRunnable(afterRejoin1, 3, TimeUnit.SECONDS);
        }
    };


    Runnable afterRejoin1 = new Runnable() {
        @Override
        public void run() {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            if (!error) {
                ScheduleRunnable(afterRejoin2, 1, TimeUnit.SECONDS);
            } else {
                Utils.addCustomLog("Error detected. Waiting 20 seconds");
                ScheduleRunnable(WarpHome, 20, TimeUnit.SECONDS);
                error = false;
            }
        }
    };

    Runnable afterRejoin2 = () -> {
        KeyBinding.setKeyBindState(KeyBindHelper.keyBindShift, false);
        mc.inGameHasFocus = true;
        mouseHelper.grabMouseCursor();
        mc.displayGuiScreen(null);
        Field f;
        f = FieldUtils.getDeclaredField(mc.getClass(), "leftClickCounter", true);
        try {
            f.set(mc, 10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initVar();
        inFailsafe = false;
        BuilderState.enabled = true;
    };


    void ScheduleRunnable(Runnable r, int delay, TimeUnit tu) {
        ScheduledExecutorService eTemp = Executors.newScheduledThreadPool(1);
        eTemp.schedule(r, delay, tu);
        eTemp.shutdown();
    }

    void initVar() {
        inFailsafe = false;
    }

    public static void disableScript() {
        BuilderState.enabled = false;
        Utils.addCustomMessage("Disabling script", EnumChatFormatting.RED);
        KeyBindHelper.resetKeybindState();
        for (ProcessModule process : processes) {
            if (process.isEnabled()) {
                process.onDisable();
                process.toggle();
            }
        }
    }

    public static void activateFailsafe() {
        inFailsafe = true;
        BuilderState.enabled = false;
        KeyBindHelper.updateKeys(false, false, false, false, false, false, false);
    }
}

