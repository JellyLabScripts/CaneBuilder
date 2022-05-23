package com.jelly.CaneBuilder.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import static com.jelly.CaneBuilder.KeyBindHelper.*;

import java.util.Random;

import static com.jelly.CaneBuilder.KeyBindHelper.updateKeys;

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


    public enum location {
        ISLAND,
        HUB,
        LOBBY,
        LIMBO,
        TELEPORTING
    }

    public static void drawString(String text, int x, int y, float size, int color) {
        GlStateManager.scale(size, size, size);
        float mSize = (float) Math.pow(size, -1);
        Minecraft.getMinecraft().fontRendererObj.drawString(text, Math.round(x / size), Math.round(y / size), color);
        GlStateManager.scale(mSize, mSize, mSize);
    }

    public static void drawStringWithShadow(String text, int x, int y, float size, int color) {
        GlStateManager.scale(size,size,size);
        float mSize = (float)Math.pow(size,-1);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text,Math.round(x / size),Math.round(y / size),color);
        GlStateManager.scale(mSize,mSize,mSize);
    }

    public static void addCustomMessage(String msg) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GREEN +
          "[Cane Builder] : " + EnumChatFormatting.GRAY + msg));
    }

    public static void addCustomMessage(String msg, EnumChatFormatting color) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GREEN +
          "[Cane Builder] : " + color + msg));
    }

    public static void addCustomLog(String log) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.BLUE +
          "[Log] : " + EnumChatFormatting.GRAY + log));
    }

    public static int nextInt(int upperbound) {
        Random r = new Random();
        return r.nextInt(upperbound);
    }

    public static boolean hasSugarcaneInInv() {
        for (Slot slot : Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots) {
            if (slot != null) {
                try {
                    if (slot.getStack().getItem().equals(Items.reeds))
                        return true;
                } catch (Exception e) {

                }
            }
        }
        return false;
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
    public static int getSlotNumberByDisplayName(String displayName) {
        for (int i = 9; i < 45; i++) {
            if (Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots.get(i) != null) {
                try {
                    if (Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots.get(i).getStack().getDisplayName().contains(displayName))
                        return i;
                } catch (Exception e) {

                }
            }
        }
        return 36;
    }

    public static boolean hasSugarcaneInMainInv() {
        for (int i = 9; i < 36; i++) {
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

    public static boolean isHotbarFull() {
        try {
            for (int i = 36; i < 45; i++) {
                if (Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots.get(i).getStack().getDisplayName() == null) {

                    return false;
                }
            }
        } catch (Exception e) {
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
                    } catch (Exception e) {

                    }
                }
            }

        }
        return 0;

    }


    public static int getFirstHotbarSlotWithSugarcane() {
        for (int i = 36; i < 45; i++) {
            if (Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots.get(i) != null) {
                try {
                    if (Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots.get(i).getStack().getItem().equals(Items.reeds))
                        return i;
                } catch (Exception e) {

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
                    } catch (Exception e) {

                    }
                }
            }

        }
        return -1;

    }
    public static synchronized void goToRelativeBlock(int rightOffset, int frontOffset) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clickWindow(int windowID, int slotID, int mouseButtonClicked, int mode) throws Exception {
        if (mc.thePlayer.openContainer instanceof ContainerChest || mc.currentScreen instanceof GuiInventory) {
            mc.playerController.windowClick(windowID, slotID, mouseButtonClicked, mode, mc.thePlayer);
            Utils.addCustomLog("Pressing slot : " + slotID);
        } else {
            Utils.addCustomMessage(EnumChatFormatting.RED + "Didn't open window! This shouldn't happen and the script has been disabled. Please immediately report to the developer.");
            updateKeys(false, false, false, false, false, false, false);
            throw new Exception();
        }
    }

    public static location getLocation() {
        if (ScoreboardUtils.getScoreboardLines().size() == 0) {
            if (BlockUtils.countCarpet() > 0) {
                return location.LIMBO;
            }
            return location.TELEPORTING;
        }

        for (String line : ScoreboardUtils.getScoreboardLines()) {
            String cleanedLine = ScoreboardUtils.cleanSB(line);
            if (cleanedLine.contains("Village")) {
                return location.HUB;
            } else if (cleanedLine.contains("Island")) {
                return location.ISLAND;
            }
        }

        if (ScoreboardUtils.getScoreboardDisplayName(1).contains("SKYBLOCK")) {
            return location.TELEPORTING;
        } else {
            return location.LOBBY;
        }
    }

}
