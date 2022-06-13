package com.jelly.CaneBuilder.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class InventoryUtils {
    static Minecraft mc = Minecraft.getMinecraft();
    public static int getRancherBootSpeed() {
        final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(8).getStack();
        if (stack != null && stack.hasTagCompound()) {
            final NBTTagCompound tag = stack.getTagCompound();
            if (tag.hasKey("ExtraAttributes")) {
                final NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
                if (ea.hasKey("ranchers_speed")){
                    return ea.getInteger("ranchers_speed");
                }
            }
        }
        return -1;
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
}
