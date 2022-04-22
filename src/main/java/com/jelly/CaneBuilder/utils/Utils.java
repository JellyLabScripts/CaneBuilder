package com.jelly.CaneBuilder.utils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
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
    public static void hardRotate(float yaw) {
        Minecraft mc = Minecraft.getMinecraft();
        if (Math.abs(mc.thePlayer.rotationYaw - yaw) < 0.2f) {
            mc.thePlayer.rotationYaw = yaw;
            return;
        }
        while (mc.thePlayer.rotationYaw > yaw) {
            mc.thePlayer.rotationYaw -= 0.1f;
        }
        while (mc.thePlayer.rotationYaw < yaw) {
            mc.thePlayer.rotationYaw += 0.1f;

        }
    }
    public static int getUnitX() {
        double modYaw = (Minecraft.getMinecraft().thePlayer.rotationYaw % 360 + 360) % 360;
        if (modYaw < 45 || modYaw > 315) {
            return 0;
        } else if (modYaw < 135) {
            return -1;
        } else if (modYaw < 225) {
            return 0;
        } else {
            return 1;
        }
    }

    public static int getUnitZ() {
        double modYaw = (Minecraft.getMinecraft().thePlayer.rotationYaw % 360 + 360) % 360;
        if (modYaw < 45 || modYaw > 315) {
            return 1;
        } else if (modYaw < 135) {
            return 0;
        } else if (modYaw < 225) {
            return -1;
        } else {
            return 0;
        }
    }
    public static float get360RotationYaw() {
        return Minecraft.getMinecraft().thePlayer.rotationYaw > 0 ?
                (Minecraft.getMinecraft().thePlayer.rotationYaw % 360) :
                (Minecraft.getMinecraft().thePlayer.rotationYaw < 360f ? 360 - (-Minecraft.getMinecraft().thePlayer.rotationYaw % 360) : 360 + Minecraft.getMinecraft().thePlayer.rotationYaw);
    }
    public static Block getFrontBlock(){
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.theWorld.getBlockState(
                new BlockPos(getUnitX() + mc.thePlayer.posX, mc.thePlayer.posY,
                        getUnitZ() + mc.thePlayer.posZ)).getBlock());
    }
    public static Block getFrontDownBlock(){
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.theWorld.getBlockState(
                new BlockPos(getUnitX() + mc.thePlayer.posX, mc.thePlayer.posY - 1,
                        getUnitZ() + mc.thePlayer.posZ)).getBlock());
    }
    public static Block getBackBlock(){
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.theWorld.getBlockState(
                new BlockPos(getUnitX() * -1 + mc.thePlayer.posX, mc.thePlayer.posY,
                        getUnitZ() * -1 + mc.thePlayer.posZ)).getBlock());
    }
    public static Block getRightBlock(){
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.theWorld.getBlockState(
                new BlockPos(getUnitZ() * -1 + mc.thePlayer.posX, mc.thePlayer.posY,
                        getUnitX() + mc.thePlayer.posZ)).getBlock());
    }

    public static Block getLeftBlock(){
        Minecraft mc = Minecraft.getMinecraft();
        return (mc.theWorld.getBlockState(
                new BlockPos(getUnitZ() + mc.thePlayer.posX, mc.thePlayer.posY,
                        getUnitX() * -1 + mc.thePlayer.posZ)).getBlock());
    }

    public static double roundTo2DecimalPlaces(double d){
        return Math.floor(d * 100) / 100;
    }

    public static boolean isWalkable(Block block) {
        return block == Blocks.air || block == Blocks.water || block == Blocks.flowing_water || block == Blocks.reeds;
    }

    // 0, 0 = initial block
    public static Block getBlockAround(int rightOffset, int frontOffset){
        Minecraft mc = Minecraft.getMinecraft();
        double X = mc.thePlayer.posX;
        double Y = mc.thePlayer.posY;
        double Z = mc.thePlayer.posZ;

        return (mc.theWorld.getBlockState(
                new BlockPos(getUnitZ() * -1 * rightOffset + getUnitX() * frontOffset + X, Y,
                        getUnitX() * rightOffset + getUnitZ() * frontOffset + Z)).getBlock());

    }
    public static Block getBlockAround(int rightOffset, int frontOffset, int upOffset){
        Minecraft mc = Minecraft.getMinecraft();
        double X = mc.thePlayer.posX;
        double Y = mc.thePlayer.posY;
        double Z = mc.thePlayer.posZ;

        return (mc.theWorld.getBlockState(
                new BlockPos(getUnitZ() * -1 * rightOffset + getUnitX() * frontOffset + X, Y + upOffset,
                        getUnitX() * rightOffset + getUnitZ() * frontOffset + Z)).getBlock());

    }
    public static void smoothRotateClockwise(final int rotationClockwise360, double speed) {
        new Thread(() -> {
            int targetYaw = (Math.round(get360RotationYaw()) + rotationClockwise360) % 360;
            while (get360RotationYaw() != targetYaw) {
                if (Math.abs(get360RotationYaw() - targetYaw) < 1f * speed) {
                    Minecraft.getMinecraft().thePlayer.rotationYaw = Math.round(Minecraft.getMinecraft().thePlayer.rotationYaw + Math.abs(get360RotationYaw() - targetYaw));
                    return;
                }
                Minecraft.getMinecraft().thePlayer.rotationYaw += (0.3f + nextInt(3) / 10.0f) * speed;
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }
    public static void smoothRotateAnticlockwise(final int rotationAnticlockwise360, double speed) {
        new Thread(() -> {
            int targetYaw = Math.round(get360RotationYaw(get360RotationYaw() - rotationAnticlockwise360));
            while (get360RotationYaw() != targetYaw) {
                if (Math.abs(get360RotationYaw() - targetYaw) < 1f * speed) {
                    Minecraft.getMinecraft().thePlayer.rotationYaw = Math.round(Minecraft.getMinecraft().thePlayer.rotationYaw - Math.abs(get360RotationYaw() - targetYaw));
                    return;
                }
                Minecraft.getMinecraft().thePlayer.rotationYaw -= (0.3f + nextInt(3) / 10.0f) * speed;
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public static int nextInt(int upperbound) {
        Random r = new Random();
        return r.nextInt(upperbound);
    }
    public static float get360RotationYaw(float yaw) {
        return yaw > 0 ?
                (yaw % 360) :
                (yaw < 360f ? 360 - (-yaw % 360) : 360 + yaw);
    }
}
