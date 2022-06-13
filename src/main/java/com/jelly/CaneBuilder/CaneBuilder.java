package com.jelly.CaneBuilder;

import com.jelly.CaneBuilder.commands.Corner1;
import com.jelly.CaneBuilder.commands.Corner2;
import com.jelly.CaneBuilder.commands.SetDirection;
import com.jelly.CaneBuilder.commands.SetLayer;
import com.jelly.CaneBuilder.config.Config;
import com.jelly.CaneBuilder.features.Failsafe;
import com.jelly.CaneBuilder.processes.*;
import com.jelly.CaneBuilder.utils.*;
import javafx.util.Builder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
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
import org.junit.internal.runners.statements.RunAfters;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.jelly.CaneBuilder.KeyBindHelper.*;
import static com.jelly.CaneBuilder.utils.Utils.*;

/*
 ** @author JellyLab, Polycrylate
 */

@Mod(modid = CaneBuilder.MODID, version = CaneBuilder.VERSION)
public class CaneBuilder {
    public static final String MODID = "canebuilder";
    public static final String NAME = "Cane Builder";
    public static final String VERSION = "2.0";

    public static Rotation rotation = new Rotation();

    public static List<ProcessModule> processes = new ArrayList<>();
    public static Minecraft mc = Minecraft.getMinecraft();

    public static String[] requiredSlotsName = new String[]{"Builder", "Infini", "Shovel", "Prisma", "Magical", "Pickaxe", "Aspect", "Dirt"};

    public static boolean isFastBreakOn = false;
    public static int layerCount = 0;


    static Clock hubCooldown = new Clock();
    static Clock jumpCooldown = new Clock();

    @Mod.EventHandler
    public static void init(FMLInitializationEvent event) {
        try {
            Config.readConfig();
        } catch (Exception e) {
            System.out.println("Error reading config file");
        }
        System.out.println("Registering");
        MinecraftForge.EVENT_BUS.register(new CaneBuilder());
        MinecraftForge.EVENT_BUS.register(new Failsafe());
        ClientCommandHandler.instance.registerCommand(new SetDirection());
        ClientCommandHandler.instance.registerCommand(new Corner1());
        ClientCommandHandler.instance.registerCommand(new Corner2());
        ClientCommandHandler.instance.registerCommand(new SetLayer());
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
            Utils.drawStringWithShadow(
                    EnumChatFormatting.WHITE + "" + "Layer count: " + (BuilderState.layer != 0 ? EnumChatFormatting.GREEN + "" + (BuilderState.layer) : EnumChatFormatting.RED + "Not set"), 4, topPad + 70, 1f, -1);
        }
    }

    public static void switchToNextProcess(ProcessModule currentModule) {
        Utils.addCustomLog("Switching processes");
        ThreadManager.stopExistingThreads();
        KeyBindHelper.updateKeys(false, false, false, false, false, false, false);
        for (int i = 0; i < processes.size(); i++) {
            if (processes.get(i).equals(currentModule)) {
                processes.get(i).toggle();
                if (i < processes.size() - 1) {
                    processes.get(i + 1).toggle();
                } else {
                    layerCount ++;

                    if(layerCount < BuilderState.layer){
                        disableScript();
                        ThreadManager.executeThread(new Thread(switchLayer));
                    } else {
                        disableScript();
                        Utils.addCustomMessage("Completed Layer!", EnumChatFormatting.GREEN);
                    }
                }
            }
        }
    }

    public static void startScript(ProcessModule processModule){
        isFastBreakOn = false;
        if(BuilderState.layer == 0){
            Utils.addCustomLog("Bozo set layer count");
            return;
        }
        if(BuilderState.direction == 0){
            if ((Math.abs(BuilderState.corner1.getZ() - BuilderState.corner2.getZ()) + 1) % 3 != 0){
                Utils.addCustomLog("Bozo read #how-to-use and set the corners correctly");
                return;
            }
        } else {
            if ((Math.abs(BuilderState.corner1.getX() - BuilderState.corner2.getX()) + 1) % 3 != 0){
                Utils.addCustomLog("Bozo read #how-to-use and set the corners correctly");
                return;
            }
        }
        ThreadManager.executeThread(new Thread(() -> {
            try {
                if(!(processModule instanceof PlaceSC)) {
                    disableJumpPotion();
                    Utils.addCustomLog("Setting Rancher's boot's speed");
                    setRancherBootsTo400();

                    Thread.sleep(500);
                    Utils.addCustomLog("Preparing inventory");

                    mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
                    Thread.sleep(500);
                    for (int i = 0; i < requiredSlotsName.length; i++) {
                        Utils.addCustomLog("Slot for " + requiredSlotsName[i] + " : " + InventoryUtils.getSlotNumberByDisplayName(requiredSlotsName[i]));

                        if(mc.thePlayer.inventoryContainer.getSlot(i + 36).getHasStack()) {
                            if (mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack().getDisplayName().contains(requiredSlotsName[i]))
                                continue;

                            clickWindow(mc.thePlayer.openContainer.windowId, 36 + i, 0, 1);
                            Thread.sleep(500);
                        }
                        if(InventoryUtils.getSlotNumberByDisplayName(requiredSlotsName[i]) >= 36){
                            clickWindow(mc.thePlayer.openContainer.windowId, InventoryUtils.getSlotNumberByDisplayName(requiredSlotsName[i]), 0, 1);
                            Thread.sleep(500);
                        }

                        clickWindow(mc.thePlayer.openContainer.windowId, InventoryUtils.getSlotNumberByDisplayName(requiredSlotsName[i]), 0, 1);
                        Thread.sleep(500);
                    }
                    mc.thePlayer.closeScreen();
                    Thread.sleep(500);
                }
                processModule.toggle();
                BuilderState.isSwitchingLayer = false;
                BuilderState.enabled = true;
            } catch (Exception e) {
                e.printStackTrace();
                disableScript();
            }
        }));


    }




    public static void disableScript() {
        BuilderState.enabled = false;
        BuilderState.isSwitchingLayer = false;
        Utils.addCustomMessage("Disabling script", EnumChatFormatting.RED);
        KeyBindHelper.resetKeybindState();
        ThreadManager.stopExistingThreads();
        for (ProcessModule process : processes) {
            if (process.isEnabled()) {
                process.toggle();
            }
        }
    }
    public static Thread switchLayer = new Thread(() -> {
        try {
            BuilderState.isSwitchingLayer = true;
            mc.thePlayer.sendChatMessage("/hub");
            hubCooldown.schedule(2000);
            while (!(hubCooldown.passed()) || !(Utils.getLocation() == Utils.location.ISLAND))
                Thread.sleep(1);
            Thread.sleep(2000);
            setKeyBindState(KeyBindHelper.keyBindShift, true);
            Thread.sleep(200);
            setKeyBindState(KeyBindHelper.keyBindShift, false);
            Thread.sleep(500);
            disableJumpPotion();
            mc.thePlayer.inventory.currentItem = 6;
            Thread.sleep(100);
            if(mc.currentScreen == null)
                KeyBinding.onTick(keybindUseItem);
            Thread.sleep(500);
            mc.thePlayer.inventory.currentItem = 8;
            Thread.sleep(100);
            if(mc.currentScreen == null)
                KeyBinding.onTick(keybindUseItem);
            Thread.sleep(1500);
            Utils.clickWindow(mc.thePlayer.openContainer.windowId, 22, 0, 0);
            Thread.sleep(1000);
            while (InventoryUtils.getFirstSlotWithSugarcane() != 0) {
                Utils.clickWindow(mc.thePlayer.openContainer.windowId, 45 + InventoryUtils.getFirstSlotWithSugarcane(), 0, 0);
                Thread.sleep(500);
            }
            Thread.sleep(500);
            mc.thePlayer.closeScreen();
            Thread.sleep(500);
            mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
            Thread.sleep(500);
            clickWindow(mc.thePlayer.openContainer.windowId, InventoryUtils.getFirstSlotWithDirt(), 0, 1);
            Thread.sleep(500);
            mc.thePlayer.closeScreen();
            mc.thePlayer.inventory.currentItem = 0;
            while(((int)mc.thePlayer.posY - BuilderState.corner1.getY() < 8)){
               if (jumpCooldown.passed()) {
                    resetKeybindState();
                    setKeyBindState(keyBindJump, true);
                    jumpCooldown.schedule(1000);
               } else {
                    boolean shouldPlace = mc.objectMouseOver != null && mc.objectMouseOver.sideHit == EnumFacing.UP && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() > 2.2f;
                    setKeyBindState(keyBindJump, false);
                    updateKeys(false, false, false, false, false, shouldPlace, true);
               }
               Thread.sleep(50);
            }
            KeyBindHelper.resetKeybindState();
            Thread.sleep(1500);
            BuilderState.setCorner1((int) Math.floor(mc.thePlayer.posX), (int) Math.floor(mc.thePlayer.posY - 1), (int) Math.floor(mc.thePlayer.posZ));
            Thread.sleep(500);
            for (ProcessModule process : processes) {
                if (process instanceof PlaceDirt1) {
                    CaneBuilder.startScript(process);
                }
            }

        }catch(Exception e){ disableScript(); }
    });

    public static void disableJumpPotion(){
        try {
            if (mc.thePlayer.isPotionActive(Potion.jump)) {
                Utils.addCustomLog("Setting potion effects");
                Thread.sleep(500);
                mc.thePlayer.inventory.currentItem = 8;
                KeyBinding.onTick(keybindUseItem);
                Thread.sleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 49, 0, 0);
                Thread.sleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 30, 0, 0);
                Thread.sleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 31, 0, 0);
                Thread.sleep(500);
                if (mc.thePlayer.isPotionActive(Potion.jump))
                    clickWindow(mc.thePlayer.openContainer.windowId, 31, 0, 0);
                Thread.sleep(500);
                mc.thePlayer.closeScreen();
                Thread.sleep(500);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    static void setRancherBootsTo400() throws Exception{
        Utils.addCustomLog("Current rancher boot's speed = " + InventoryUtils.getRancherBootSpeed());
        if(InventoryUtils.getRancherBootSpeed() == -1){
            Utils.addCustomLog("Can't find rancher's boots data!");
            throw new Exception();
        }
        if(InventoryUtils.getRancherBootSpeed() == 400) {
            mc.thePlayer.closeScreen();
            return;
        }

        Thread.sleep(500);
        mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
        Thread.sleep(500);
        clickWindow(mc.thePlayer.openContainer.windowId, 36, 0, 1);
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
        Method m = ((GuiEditSign) mc.currentScreen).getClass().getDeclaredMethod("func_73869_a", char.class, int.class);
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
        mc.thePlayer.closeScreen();
        Thread.sleep(500);
        mc.thePlayer.inventory.currentItem = 0;
        KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
        Thread.sleep(500);

    }
}

