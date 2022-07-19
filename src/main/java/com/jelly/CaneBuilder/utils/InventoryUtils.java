package com.jelly.CaneBuilder.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.util.EnumChatFormatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jelly.CaneBuilder.handlers.KeyBindHandler.updateKeys;

public class InventoryUtils {
    static Minecraft mc = Minecraft.getMinecraft();
    public static int getRancherBootSpeed() {
        final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(8).getStack();
        int speed = -1;
        if (stack != null && stack.hasTagCompound()) {
            final NBTTagCompound tag = stack.getTagCompound();
<<<<<<< HEAD
            Utils.addCustomMessage(tag.toString());
            if (tag.hasKey("ExtraAttributes")) {
                final NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
                if (ea.hasKey("ranchers_speed")){
                    return ea.getInteger("ranchers_speed");
=======
            final Pattern pattern = Pattern.compile("(Current Speed Cap: §a\\d+)", Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(tag.toString());
            while (matcher.find()) {
                if (matcher.group(0) != null) {
                    speed = Integer.parseInt((matcher.group(0).replaceAll("Current Speed Cap: §a" ,"")));
>>>>>>> 64f5f078b38ac0c844d7afc761ed0e5e400fb22d
                }
            }
        }
        return speed;
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

    public static int countDirtStack() {
        int count = 0;
        for (Slot slot : Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots) {
            if (slot != null) {
                if (slot.getStack() != null) {
                    try {
                        if (slot.getStack().getDisplayName().contains("Dirt"))
                            count++;
                    } catch (Exception ignored) {}
                }
            }

        }
        return count;

    }

    public static int getFirstSlotWithDirt() {
        for (Slot slot : Minecraft.getMinecraft().thePlayer.inventoryContainer.inventorySlots) {
            if (slot != null) {
                if (slot.getStack() != null) {
                    try {
                        if (slot.getStack().getItem().equals(Item.getItemFromBlock(Blocks.dirt)))
                            return slot.slotNumber;
                    } catch (Exception ignored) {}
                }
            }

        }
        return -1;

    }

    public static void openInventory(){
        mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
    }


    public static void clickWindow(int windowID, int slotID, int mouseButtonClicked, int mode) throws Exception {
        if (mc.thePlayer.openContainer instanceof ContainerChest || mc.currentScreen instanceof GuiInventory) {
            mc.playerController.windowClick(windowID, slotID, mouseButtonClicked, mode, mc.thePlayer);
            LogUtils.addCustomLog("Pressing slot : " + slotID);
        } else {
            LogUtils.addCustomMessage(EnumChatFormatting.RED + "Didn't open window! This shouldn't happen and the script has been disabled. Please immediately report to the developer.");
            updateKeys(false, false, false, false, false, false, false);
            throw new Exception();
        }
    }
}
