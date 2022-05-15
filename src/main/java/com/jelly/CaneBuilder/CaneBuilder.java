package com.jelly.CaneBuilder;

import com.jelly.CaneBuilder.commands.Corner1;
import com.jelly.CaneBuilder.commands.Corner2;
import com.jelly.CaneBuilder.commands.SetDirection;
import com.jelly.CaneBuilder.processes.*;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mod(modid = CaneBuilder.MODID, version = CaneBuilder.VERSION)
public class CaneBuilder {
    public static final String MODID = "canebuilder";
    public static final String NAME = "Cane Builder";
    public static final String VERSION = "1.0";

    public static List<ProcessModule> processes = new ArrayList<>();
    static Minecraft mc = Minecraft.getMinecraft();

    //states
    static boolean diggingPath = false;
    static boolean diggingTrench = false;
    static boolean goLeft = false;
    static boolean inFailsafe;
    static boolean error;
    // for digging trench path
    static boolean slowDig;
    static boolean inDiggingTrench;
    // for digging path part

    //for placing sugarcane

    //autoclicker
    static long initialTime = 0;

    /*
     ** @author JellyLab, Polycrylate
     */

    public volatile static int playerYaw;

    MouseHelper mouseHelper = new MouseHelper();

    @Mod.EventHandler
    public static void init(FMLInitializationEvent event) {
        System.out.println("Registering");
        MinecraftForge.EVENT_BUS.register(new CaneBuilder());
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
    public void onMessageReceived(ClientChatReceivedEvent event) {

        if (event.message.getFormattedText().contains("You were spawned in Limbo") && !inFailsafe && BuilderState.enabled) {
            activateFailsafe();
            ScheduleRunnable(LeaveSBIsand, 8, TimeUnit.SECONDS);
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
            ScheduleRunnable(LeaveSBIsand, 10, TimeUnit.SECONDS);
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
                    Utils.addCustomLog("Completed process");
                    KeyBindHelper.updateKeys(false, false, false, false, false, false, false);
                }
            }
        }
    }

    Runnable LeaveSBIsand = new Runnable() {
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
        Field f = null;
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
        slowDig = false;
        inDiggingTrench = false;
        inFailsafe = false;
        error = false;
        // playerYaw = Math.round(AngleUtils.get360RotationYaw() / 90) < 4 ? Math.round(AngleUtils.get360RotationYaw() / 90) * 90 : 0;
    }

    public static void disableScript() {
        BuilderState.enabled = false;
        diggingTrench = false;
        diggingPath = false;
        Utils.addCustomMessage("Disabling script");
        KeyBindHelper.updateKeys(false, false, false, false, false, false, false);
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

