package com.jelly.CaneBuilder;

import com.jelly.CaneBuilder.config.Config;
import com.jelly.CaneBuilder.structures.Coord;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

public class BuilderState {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean enabled = false;
    public static Coord corner1 = null;
    public static Coord corner2 = null;
    public static int direction = -1;
    public static int layer = 0;
    public static boolean isSwitchingLayer = false;

    public static void setCorner1(int x, int y, int z) {
        corner1 = new Coord(x, y, z);
        Utils.addCustomMessage("Set corner 1 to: " + corner1, EnumChatFormatting.GREEN);
        Config.writeConfig();
    }

    public static void setCorner2(int x, int y, int z) {
        corner2 = new Coord(x, y, z);
        Utils.addCustomMessage("Set corner 2 to: " + corner2, EnumChatFormatting.GREEN);
        Config.writeConfig();
    }


    public static int lookingAtParallel() {
        if (direction == 0) {
            return mc.objectMouseOver.getBlockPos().getZ();
        } else if (direction == 1) {
            return mc.objectMouseOver.getBlockPos().getX();
        }
        return -1;
    }

    public static int lookingAtPerpendicular() {
        if (direction == 0) {
            return mc.objectMouseOver.getBlockPos().getX();
        } else if (direction == 1) {
            return mc.objectMouseOver.getBlockPos().getZ();
        }
        return -1;
    }

    public static int onParallel() {
        if (direction == 0) {
            return (int) Math.floor(mc.thePlayer.posZ);
        } else if (direction == 1) {
            return (int) Math.floor(mc.thePlayer.posX);
        }
        return -1;
    }

    public static int onPerpendicular() {
        if (direction == 0) {
            return (int) Math.floor(mc.thePlayer.posX);
        } else if (direction == 1) {
            return (int) Math.floor(mc.thePlayer.posZ);
        }
        return -1;
    }
}
