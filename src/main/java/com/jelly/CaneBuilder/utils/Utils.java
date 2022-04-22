package com.jelly.CaneBuilder.utils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
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
    public synchronized static void smoothRotatePitchTo(final int targetPitch, double speed) {
        while (Minecraft.getMinecraft().thePlayer.rotationPitch != targetPitch) {
            if (Math.abs(Minecraft.getMinecraft().thePlayer.rotationPitch  - targetPitch) < 1f * speed) {
                Minecraft.getMinecraft().thePlayer.rotationPitch = targetPitch;
                return;
            }
            if(targetPitch <  Minecraft.getMinecraft().thePlayer.rotationPitch )
                Minecraft.getMinecraft().thePlayer.rotationPitch  -= (0.3f + nextInt(3) / 10.0f) * speed;
            else
                Minecraft.getMinecraft().thePlayer.rotationPitch  += (0.3f + nextInt(3) / 10.0f) * speed;
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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

    public static void sineRotateCW(final float rotationClockwise360, double speed) {
            float targetYaw = (Math.round(get360RotationYaw()) + rotationClockwise360) % 360;
            while (get360RotationYaw() != targetYaw) {
                float difference = Math.abs(get360RotationYaw() - targetYaw);
                if (difference < 0.4f * speed) {
                    Minecraft.getMinecraft().thePlayer.rotationYaw = Math.round(Minecraft.getMinecraft().thePlayer.rotationYaw + difference);
                    return;
                }
                Minecraft.getMinecraft().thePlayer.rotationYaw += speed * 0.3 * ((difference / rotationClockwise360) + (Math.PI / 2));
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

    }
    public static void sineRotateACW(final float rotationAnticlockwise360, double speed) {
        new Thread(() -> {
            float targetYaw = Math.round(get360RotationYaw(get360RotationYaw() - rotationAnticlockwise360));
            while (get360RotationYaw() != targetYaw) {
                float difference = Math.abs(get360RotationYaw() - targetYaw);
                if (difference < 0.4f * speed) {
                    Minecraft.getMinecraft().thePlayer.rotationYaw = Math.round(Minecraft.getMinecraft().thePlayer.rotationYaw - difference);
                    return;
                }
                Minecraft.getMinecraft().thePlayer.rotationYaw -= 0.3f + nextInt(3) / 10.0f;
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public static synchronized void goToBlock(int x, int z) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            double yaw = Utils.get360RotationYaw();
            double xdiff = x + 0.5 - mc.thePlayer.posX;
            double zdiff = z + 0.5 -  mc.thePlayer.posZ;
            double distance = Math.sqrt(Math.pow(xdiff, 2) + Math.pow(zdiff, 2));
            double speed = Math.sqrt((Math.pow(Math.abs(mc.thePlayer.posX - mc.thePlayer.lastTickPosX), 2) + (Math.pow(Math.abs(mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ), 2))));
            double targetYaw = Utils.get360RotationYaw((float) Math.toDegrees(Math.atan2(-xdiff, zdiff)));
            Utils.addCustomLog("Calculated yaw: " + targetYaw);

            if (yaw > targetYaw) {
                if (yaw - targetYaw > 180) {
                    Utils.sineRotateCW(Utils.get360RotationYaw((float) (targetYaw - yaw)), 2);
                } else {
                    Utils.sineRotateACW((float) (yaw - targetYaw), 2);
                }
            } else {
                if (targetYaw - yaw < 180) {
                    Utils.sineRotateCW((float) (targetYaw - yaw), 2);
                } else {
                    Utils.sineRotateACW(Utils.get360RotationYaw((float) (yaw - targetYaw)), 2);
                }
            }
            Thread.sleep(1000);


            while (Math.abs(distance) > 0.2) {
                System.out.println(1.4 * speed >= distance);
                xdiff = x + 0.5 - mc.thePlayer.posX;
                zdiff = z + 0.5 -  mc.thePlayer.posZ;
                targetYaw = Utils.get360RotationYaw((float) Math.toDegrees(Math.atan2(-xdiff, zdiff)));
                Utils.hardRotate((float) targetYaw);
                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), 1.4 * speed >= distance);
                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), !(1.4 * speed >= distance));

                distance = Math.sqrt(Math.pow((x + 0.5 - mc.thePlayer.posX), 2) + Math.pow((z + 0.5 - mc.thePlayer.posZ), 2));
                speed = Math.sqrt((Math.pow(Math.abs(mc.thePlayer.posX - mc.thePlayer.lastTickPosX), 2) + (Math.pow(Math.abs(mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ), 2))));
                Thread.sleep(10);

            }
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), false);
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static synchronized void lookAtPos(float x, float z) {
        Minecraft mc = Minecraft.getMinecraft();
        double yaw = Utils.get360RotationYaw();
        double xdiff = x - mc.thePlayer.posX;
        double zdiff = z - mc.thePlayer.posZ;
        double targetYaw = Utils.get360RotationYaw((float) Math.toDegrees(Math.atan2(-xdiff, zdiff)));
        Utils.addCustomLog("Calculated yaw: " + targetYaw);

        if (yaw > targetYaw) {
            if (yaw - targetYaw > 180) {
                Utils.sineRotateCW(Utils.get360RotationYaw((float) (targetYaw - yaw)), 2);
            } else {
                Utils.sineRotateACW((float) (yaw - targetYaw), 2);
            }
        } else {
            if (targetYaw - yaw < 180) {
                Utils.sineRotateCW((float) (targetYaw - yaw), 2);
            } else {
                Utils.sineRotateACW(Utils.get360RotationYaw((float) (yaw - targetYaw)), 2);
            }
        }

    }
    public static synchronized void align() {
        double x = Minecraft.getMinecraft().thePlayer.posX;
        double z = Minecraft.getMinecraft().thePlayer.posZ;

        lookAtPos((int)x + 0.5f,(int)z + 0.5f);
        while (x % 1 < 0.4f || x % 1 > 0.6f || z % 1 > 0.6f ||  z % 1 < 0.4f){
            x = Minecraft.getMinecraft().thePlayer.posX;
            z = Minecraft.getMinecraft().thePlayer.posZ;


            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), true);
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), true);
        }
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), false);
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);


    }
}
