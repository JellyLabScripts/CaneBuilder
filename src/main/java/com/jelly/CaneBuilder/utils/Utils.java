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

import java.util.Random;

public class Utils {
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
    public static boolean hasSugarcaneInHotbar(){
        for(int  i = 36; i < 45; i++) {
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
}
