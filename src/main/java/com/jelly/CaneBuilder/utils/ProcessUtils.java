package com.jelly.CaneBuilder.utils;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.handlers.KeyBindHandler;
import com.jelly.CaneBuilder.handlers.MacroHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.jelly.CaneBuilder.handlers.KeyBindHandler.*;
import static com.jelly.CaneBuilder.utils.InventoryUtils.clickWindow;

public class ProcessUtils {


    static Minecraft mc = Minecraft.getMinecraft();



    public static void switchLayer(){
        Clock jumpCooldown = new Clock();
        try {
            BuilderState.isSwitchingLayer = true;
            Thread.sleep(2000);
            MacroHandler.playerRotation.easeTo(AngleUtils.getClosest(), 89, 800);
            Thread.sleep(1000);
            mc.thePlayer.inventory.currentItem = 8;
            Thread.sleep(100);
            if(mc.currentScreen == null)
                KeyBindHandler.onTick(keybindUseItem);
            Thread.sleep(1500);
            clickWindow(mc.thePlayer.openContainer.windowId, 22, 0, 0);
            Thread.sleep(1000);
            while (InventoryUtils.getFirstSlotWithSugarcane() != 0) {
                clickWindow(mc.thePlayer.openContainer.windowId, 45 + InventoryUtils.getFirstSlotWithSugarcane(), 0, 0);
                Thread.sleep(500);
            }
            Thread.sleep(500);
            mc.thePlayer.closeScreen();
            Thread.sleep(500);
            InventoryUtils.openInventory();
            Thread.sleep(500);
            clickWindow(mc.thePlayer.openContainer.windowId, InventoryUtils.getFirstSlotWithDirt(), 0, 1);
            Thread.sleep(500);
            mc.thePlayer.closeScreen();
            disableJumpPotion();
            mc.thePlayer.inventory.currentItem = 0;
            while(((int)mc.thePlayer.posY - BuilderState.corner1.getY() < 8)){
                if (jumpCooldown.passed()) {
                    resetKeybindState();
                    setKeyBindState(keyBindJump, true);
                    jumpCooldown.schedule(1000);
                } else {
                    boolean shouldPlace = mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && mc.objectMouseOver.sideHit == EnumFacing.UP && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() > 2.2f;
                    setKeyBindState(keyBindJump, false);
                    updateKeys(false, false, false, false, false, shouldPlace, true);
                }
                Thread.sleep(50);
            }
            KeyBindHandler.resetKeybindState();
            Thread.sleep(1500);
            BuilderState.setCorner1((int) Math.floor(mc.thePlayer.posX), (int) Math.floor(mc.thePlayer.posY - 1), (int) Math.floor(mc.thePlayer.posZ));
            Thread.sleep(500);

        }catch(Exception ignored){ }
    }

    public static void disableJumpPotion(){
        try {
            if (mc.thePlayer.isPotionActive(Potion.jump)) {
                LogUtils.addCustomLog("Setting potion effects");
                Thread.sleep(500);
                mc.thePlayer.inventory.currentItem = 8;
                KeyBindHandler.onTick(keybindUseItem);
                Thread.sleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 51, 0, 0);
                Thread.sleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 32, 0, 0);
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
    public static void setRancherBootsTo400() throws Exception{
        LogUtils.addCustomLog("Current rancher boot's speed = " + InventoryUtils.getRancherBootSpeed());
        if(InventoryUtils.getRancherBootSpeed() == -1){
            LogUtils.addCustomLog("Can't find rancher's boots data!");
            throw new Exception();
        }
        if(InventoryUtils.getRancherBootSpeed() == 400) {
            mc.thePlayer.closeScreen();
            return;
        }

        Thread.sleep(500);
        InventoryUtils.openInventory();
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
        m.invoke(mc.currentScreen, '\b', 14);
        Thread.sleep(250);
        m.invoke(mc.currentScreen, '\b', 14);
        Thread.sleep(450);
        m.invoke(mc.currentScreen, '\b', 14);
        Thread.sleep(300);
        m.invoke(mc.currentScreen, '4', 16);
        Thread.sleep(350);
        m.invoke(mc.currentScreen, '0', 16);
        Thread.sleep(250);
        m.invoke(mc.currentScreen, '0', 16);
        Thread.sleep(400);
        Field f = ((GuiEditSign) mc.currentScreen).getClass().getDeclaredField("field_146848_f");
        f.setAccessible(true);
        ((TileEntitySign)(f.get(mc.currentScreen))).markDirty();
        mc.displayGuiScreen((GuiScreen)null);
        Thread.sleep(500);
        mc.thePlayer.inventory.currentItem = 0;
        KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
        Thread.sleep(500);
    }
    public static void setRancherBootsTo200() throws Exception{
        LogUtils.addCustomLog("Current rancher boot's speed = " + InventoryUtils.getRancherBootSpeed());
        if(InventoryUtils.getRancherBootSpeed() == -1){
            LogUtils.addCustomLog("Can't find rancher's boots data!");
            throw new Exception();
        }
        if(InventoryUtils.getRancherBootSpeed() == 200) {
            mc.thePlayer.closeScreen();
            return;
        }
        Thread.sleep(500);
        InventoryUtils.openInventory();
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
        m.invoke(mc.currentScreen, '\b', 14);
        Thread.sleep(250);
        m.invoke(mc.currentScreen, '\b', 14);
        Thread.sleep(350);
        m.invoke(mc.currentScreen, '\b', 14);
        Thread.sleep(250);
        m.invoke(mc.currentScreen, '2', 16);
        Thread.sleep(350);
        m.invoke(mc.currentScreen, '0', 16);
        Thread.sleep(400);
        m.invoke(mc.currentScreen, '0', 16);
        Thread.sleep(300);
        Field f = ((GuiEditSign) mc.currentScreen).getClass().getDeclaredField("field_146848_f");
        f.setAccessible(true);
        ((TileEntitySign)(f.get(mc.currentScreen))).markDirty();
        mc.displayGuiScreen((GuiScreen)null);
        Thread.sleep(500);
        mc.thePlayer.inventory.currentItem = 0;
        KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
        Thread.sleep(500);
    }

}
