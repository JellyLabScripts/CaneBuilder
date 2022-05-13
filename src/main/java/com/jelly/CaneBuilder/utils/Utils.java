package com.jelly.CaneBuilder.utils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;

import java.util.Random;

public class Utils {

    protected static Minecraft mc = Minecraft.getMinecraft();
    protected static int keybindA = mc.gameSettings.keyBindLeft.getKeyCode();
    protected static int keybindD = mc.gameSettings.keyBindRight.getKeyCode();
    protected static int keybindW = mc.gameSettings.keyBindForward.getKeyCode();
    protected static int keybindS = mc.gameSettings.keyBindBack.getKeyCode();
    protected static int keybindAttack = mc.gameSettings.keyBindAttack.getKeyCode();
    protected static int keybindUseItem = mc.gameSettings.keyBindUseItem.getKeyCode();
    protected static int keyBindShift = mc.gameSettings.keyBindSneak.getKeyCode();
    protected static int keyBindJump = mc.gameSettings.keyBindJump.getKeyCode();
    public static boolean isUngrabbed = false;
    protected static boolean doesGameWantUngrabbed;
    protected static MouseHelper oldMouseHelper;

    public static void addCustomMessage(String msg){
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GREEN +
                "[Cane Builder] : " + EnumChatFormatting.GRAY + msg));

    }
    public static void addCustomMessage(String msg, EnumChatFormatting color){
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GREEN +
                "[Cane Builder] : " + color + msg));

    }
    public static void addCustomLog(String log){
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.BLUE +
                "[Cane Builder Log] : " + EnumChatFormatting.GRAY + log));

    }

    public static int nextInt(int upperbound) {
        Random r = new Random();
        return r.nextInt(upperbound);
    }
    public static boolean hasSugarcaneInInv(){
        for(Slot slot : Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots) {
            if (slot != null) {
                try {
                    if (slot.getStack().getItem().equals(Items.reeds))
                        return true;
                }catch(Exception e){

                }
            }
        }
        return false;
    }
    public static double roundTo2DecimalPlaces(double d){
        return Math.floor(d * 100) / 100;
    }
    public static boolean hasSugarcaneInHotbar() {
        for (int i = 36; i < 45; i++) {
            if (Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots.get(i) != null) {
                try {
                    if (Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots.get(i).getStack().getItem().equals(Items.reeds))
                        return true;
                } catch (Exception e) {

                }
            }
        }
        return false;
    }
    public static boolean hasSugarcaneInMainInv(){
        for(int  i = 9; i < 36; i++) {
            if (Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots.get(i) != null) {
                try {
                    if (Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots.get(i).getStack().getItem().equals(Items.reeds))
                        return true;
                }catch(Exception e){

                }
            }
        }
        return false;
    }
    public static boolean isHotbarFull(){
        try {
            for (int i = 36; i < 45; i++) {
                if (Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots.get(i).getStack().getDisplayName() == null) {

                    return false;
                }
            }
        }catch(Exception e){
            return false;
        }
        return true;

    }

    public static int getFirstSlotWithSugarcane() {
        for (Slot slot : Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots) {
            if (slot != null) {
                if (slot.getStack() != null) {
                    try {
                        if (slot.getStack().getItem().equals(Items.reeds))
                            return slot.slotNumber;
                    }catch(Exception e){

                    }
                }
            }

        }
        return 0;

    }


    public static int getFirstHotbarSlotWithSugarcane() {
        for(int  i = 36; i < 45; i++) {
            if (Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots.get(i) != null) {
                try {
                    if (Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots.get(i).getStack().getItem().equals(Items.reeds))
                        return i;
                }catch(Exception e){

                }
            }
        }
        return 36;

    }
    public static int getFirstSlotWithDirt() {
        for (Slot slot : Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots) {
            if (slot != null) {
                if (slot.getStack() != null) {
                    try {
                        if (slot.getStack().getItem().equals(Item.getItemFromBlock(Blocks.dirt)))
                            return slot.slotNumber;
                    }catch(Exception e){

                    }
                }
            }

        }
        return -1;

    }
    public static boolean isInCenterOfBlockForward(){
        return (Math.round(AngleUtils.get360RotationYaw()) == 180 || Math.round(AngleUtils.get360RotationYaw()) == 0) ?Math.abs(Minecraft.getMinecraft().thePlayer.posZ) % 1 > 0.3f && Math.abs(Minecraft.getMinecraft().thePlayer.posZ) % 1 < 0.7f :
                Math.abs(Minecraft.getMinecraft().thePlayer.posX) % 1 > 0.3f && Math.abs(Minecraft.getMinecraft().thePlayer.posX) % 1 < 0.7f;

    }
    public static boolean isInCenterOfBlockSideways(){
        return (Math.round(AngleUtils.get360RotationYaw()) == 90 || Math.round(AngleUtils.get360RotationYaw()) == 270) ?Math.abs(Minecraft.getMinecraft().thePlayer.posZ) % 1 > 0.3f && Math.abs(Minecraft.getMinecraft().thePlayer.posZ) % 1 < 0.7f :
                Math.abs(Minecraft.getMinecraft().thePlayer.posX) % 1 > 0.3f && Math.abs(Minecraft.getMinecraft().thePlayer.posX) % 1 < 0.7f;

    }
    public static synchronized void goToRelativeBlock(int rightOffset, int frontOffset){
        try {
            setKeyBindState(keyBindShift, true);
            BlockPos targetBlockPos = BlockUtils.getBlockPosAround(0, frontOffset, 0);
            while ((Math.floor(mc.thePlayer.posX) != targetBlockPos.getX() || Math.floor(mc.thePlayer.posZ) != targetBlockPos.getZ())) {
                setKeyBindState(keybindW, frontOffset > 0);
                setKeyBindState(keybindS, !(frontOffset > 0));
                Thread.sleep(1);
            }
            resetKeybindState();
            BlockPos targetBlockPos2 = BlockUtils.getBlockPosAround(rightOffset, 0, 0);
            while ((Math.floor(mc.thePlayer.posX) != targetBlockPos2.getX() || Math.floor(mc.thePlayer.posZ) != targetBlockPos2.getZ())) {
                setKeyBindState(keybindD, rightOffset > 0);
                setKeyBindState(keybindA, !(rightOffset > 0));
                Thread.sleep(1);
            }
            resetKeybindState();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    protected static void setKeyBindState(int keyCode, boolean pressed){
        if(pressed){
            if(mc.currentScreen != null){
                Utils.addCustomLog("In GUI, pausing");
                KeyBinding.setKeyBindState(keyCode, false);
                return;
            }
        }
        KeyBinding.setKeyBindState(keyCode, pressed);
    }
    protected static void resetKeybindState(){
        KeyBinding.setKeyBindState(keybindA, false);
        KeyBinding.setKeyBindState(keybindS, false);
        KeyBinding.setKeyBindState(keybindW, false);
        KeyBinding.setKeyBindState(keybindD, false);
        KeyBinding.setKeyBindState(keyBindShift, false);
        KeyBinding.setKeyBindState(keyBindJump, false);
        KeyBinding.setKeyBindState(keybindAttack, false);
        KeyBinding.setKeyBindState(keybindUseItem, false);
    }
    public static void goToBlock(int x, int z) {
        try {
            double xdiff = x + 0.5 - mc.thePlayer.posX;
            double zdiff = z + 0.5 - mc.thePlayer.posZ;
            double distance = Math.sqrt(Math.pow(xdiff, 2) + Math.pow(zdiff, 2));
            double speed = Math.sqrt((Math.pow(Math.abs(mc.thePlayer.posX - mc.thePlayer.lastTickPosX), 2) + (Math.pow(Math.abs(mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ), 2))));
            double targetYaw = AngleUtils.get360RotationYaw((float) Math.toDegrees(Math.atan2(-xdiff, zdiff)));

            AngleUtils.smoothRotateTo((float) targetYaw, 2);
            Thread.sleep(100);

            while (Math.abs(distance) > 0.2) {
                if (Thread.currentThread().isInterrupted()) throw new Exception("Detected interrupt - stopping");
                xdiff = x + 0.5 - mc.thePlayer.posX;
                zdiff = z + 0.5 - mc.thePlayer.posZ;
                targetYaw = AngleUtils.get360RotationYaw((float) Math.toDegrees(Math.atan2(-xdiff, zdiff)));
                if (1.4 * speed < distance) AngleUtils.hardRotate((float) targetYaw);
                setKeyBindState(keybindW, true);
                setKeyBindState(keyBindShift, 1.4 * speed >= distance);
                distance = Math.sqrt(Math.pow((x + 0.5 - mc.thePlayer.posX), 2) + Math.pow((z + 0.5 - mc.thePlayer.posZ), 2));
                speed = Math.sqrt((Math.pow(Math.abs(mc.thePlayer.posX - mc.thePlayer.lastTickPosX), 2) + (Math.pow(Math.abs(mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ), 2))));
                Thread.sleep(20);
            }
            resetKeybindState();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
   /* public static void ungrabMouse() {
        Minecraft m = Minecraft.getMinecraft();
        if (isUngrabbed) return;
        m.gameSettings.pauseOnLostFocus = false;
        if (oldMouseHelper == null) oldMouseHelper = m.mouseHelper;
        doesGameWantUngrabbed = !Mouse.isGrabbed();
        oldMouseHelper.ungrabMouseCursor();
        m.inGameHasFocus = true;
        m.mouseHelper = new MouseHelper() {
            @Override
            public void mouseXYChange() {
            }
            @Override
            public void grabMouseCursor() {
                doesGameWantUngrabbed = false;
            }
            @Override
            public void ungrabMouseCursor() {
                doesGameWantUngrabbed = true;
            }
        };
        isUngrabbed = true;
    }

    public static void regrabMouse() {
        if (!isUngrabbed) return;
        Minecraft m = Minecraft.getMinecraft();
        m.mouseHelper = oldMouseHelper;
        if (!doesGameWantUngrabbed) m.mouseHelper.grabMouseCursor();
        oldMouseHelper = null;
        isUngrabbed = false;
    }*/

}
