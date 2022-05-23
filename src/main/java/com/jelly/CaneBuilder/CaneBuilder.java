package com.jelly.CaneBuilder;

import com.jelly.CaneBuilder.commands.Corner1;
import com.jelly.CaneBuilder.commands.Corner2;
import com.jelly.CaneBuilder.commands.SetDirection;
import com.jelly.CaneBuilder.config.Config;
import com.jelly.CaneBuilder.processes.*;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import static com.jelly.CaneBuilder.utils.Utils.*;

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

    public static String[] requiredSlotsName = new String[]{"Builder", "Infini", "Shovel", "Prisma", "Magical", "Pickaxe", "Aspect", "Dirt"};

    @Mod.EventHandler
    public static void init(FMLInitializationEvent event) {
        try {
            Config.readConfig();
        } catch (Exception e) {
            System.out.println("Error reading config file");
        }
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

    public static void switchToNextProcess(ProcessModule currentModule) {
        Utils.addCustomLog("Switching processes");
        ThreadManager.stopExistingThreads();
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

    public static void startScript(ProcessModule processModule){

        ThreadManager.executeThread(new Thread(() -> {
            try {
                Utils.addCustomLog("Setting Rancher's boot's speed");
                Thread.sleep(500);
                mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
                Thread.sleep(500);
                for(int i = 36; i < 44; i ++){
                    clickWindow(mc.thePlayer.openContainer.windowId, i, 0, 1);
                    Thread.sleep(500);
                }
                Thread.sleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 8, 0, 0);
                Thread.sleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 36, 0, 0);
                Thread.sleep(250);
                mc.thePlayer.closeScreen();
                Thread.sleep(250);
                mc.thePlayer.inventory.currentItem = 0;
                Thread.sleep(250);
                KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
                Thread.sleep(1000);
                Method m = ((GuiEditSign)mc.currentScreen).getClass().getDeclaredMethod("func_73869_a", char.class, int.class);
                Utils.addCustomLog(m.toString());
                m.setAccessible(true);
                m.invoke(mc.currentScreen, '\r', 14);
                Thread.sleep(500);
                m.invoke(mc.currentScreen, '\r', 14);
                Thread.sleep(500);
                m.invoke(mc.currentScreen, '\r', 14);
                Thread.sleep(500);
                m.invoke(mc.currentScreen, '4', 16);
                Thread.sleep(500);
                m.invoke(mc.currentScreen, '0', 16);
                Thread.sleep(500);
                m.invoke(mc.currentScreen, '0', 16);
                Thread.sleep(500);
                Thread.sleep(500);
                mc.thePlayer.closeScreen();
                Thread.sleep(500);
                KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                Thread.sleep(500);
                Utils.addCustomLog("Preparing inventory");
                mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
                Thread.sleep(500);

                for(String s : requiredSlotsName) {
                    clickWindow(mc.thePlayer.openContainer.windowId, Utils.getSlotNumberByDisplayName(s), 0, 1);
                    Thread.sleep(500);
                }
                mc.thePlayer.closeScreen();
                Thread.sleep(500);

                processModule.toggle();
                processModule.onEnable();
                BuilderState.enabled = true;
            } catch(Exception e){
                e.printStackTrace();
                disableScript();
            }
        }));

    }


    public static void disableScript() {
        BuilderState.enabled = false;
        Utils.addCustomMessage("Disabling script", EnumChatFormatting.RED);
        KeyBindHelper.resetKeybindState();
        ThreadManager.stopExistingThreads();
        for (ProcessModule process : processes) {
            if (process.isEnabled()) {
                process.onDisable();
                process.toggle();
            }
        }
    }
}

