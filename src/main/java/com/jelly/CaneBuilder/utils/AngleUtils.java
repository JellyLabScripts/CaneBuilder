package com.jelly.CaneBuilder.utils;
import com.jelly.CaneBuilder.BuilderState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

public class AngleUtils extends LogUtils {
    private static Minecraft mc = Minecraft.getMinecraft();


    public static float get360RotationYaw() {
        return get360RotationYaw(mc.thePlayer.rotationYaw);
    }

    public static float get360RotationYaw(float yaw) {
        return (yaw % 360 + 360) % 360;
    }

    public static float clockwiseDifference(float initialYaw360, float targetYaw360) {
        return get360RotationYaw(targetYaw360 - initialYaw360);
    }

    public static float antiClockwiseDifference(float initialYaw360, float targetYaw360) {
        return get360RotationYaw(initialYaw360 - targetYaw360);
    }

    public static float smallestAngleDifference(float initialYaw360, float targetYaw360) {
        return Math.min(clockwiseDifference(initialYaw360, targetYaw360), antiClockwiseDifference(initialYaw360, targetYaw360));
    }


    public static int getRelativeYawFromBlockPos(BlockPos facingBlockPos) {
        if (BlockUtils.getBlockPosAround(1, 0, 0).equals(facingBlockPos) || BlockUtils.getBlockPosAround(1, 0, 1).equals(facingBlockPos)) {
            LogUtils.addCustomLog("Right");
            return 90;
        } else if (BlockUtils.getBlockPosAround(-1, 0, 0).equals(facingBlockPos) || BlockUtils.getBlockPosAround(-1, 0, 1).equals(facingBlockPos)) {
            LogUtils.addCustomLog("Left");
            return -90;
        } else if (BlockUtils.getBlockPosAround(0, 1, 0).equals(facingBlockPos)  || BlockUtils.getBlockPosAround(0, 0, 0).equals(facingBlockPos)
        || BlockUtils.getBlockPosAround(0, 1, 1).equals(facingBlockPos)) {
            LogUtils.addCustomLog("Forward");
            return 0;
        } else if (BlockUtils.getBlockPosAround(0, -1, 0).equals(facingBlockPos) || BlockUtils.getBlockPosAround(0, -1, 1).equals(facingBlockPos)) {
            LogUtils.addCustomLog("Backward " + facingBlockPos);
            return 180;
        }
        LogUtils.addCustomLog("Can't find " + facingBlockPos);
        return -1;

    }

    public static float getClosest() {
        if (get360RotationYaw() < 45 || get360RotationYaw() > 315) {
            return 0f;
        } else if (get360RotationYaw() < 135) {
            return 90f;
        } else if (get360RotationYaw() < 225) {
            return 180f;
        } else {
            return 270f;
        }
    }

    public static float parallelToC2() {
        if (BuilderState.direction == 0) {
            if (BuilderState.corner2.getZ() > BuilderState.corner1.getZ()) {
                return 0;
            } else {
                return 180;
            }
        } else if (BuilderState.direction == 1) {
            if (BuilderState.corner2.getX() > BuilderState.corner1.getX()) {
                return 270;
            } else {
                return 90;
            }
        }
        return -1;
    }

    public static float perpendicularToC2() {
        if (BuilderState.direction == 0) {
            if (BuilderState.corner2.getX() > BuilderState.corner1.getX()) {
                return 270;
            } else {
                return 90;
            }
        } else if (BuilderState.direction == 1) {
            if (BuilderState.corner2.getZ() > BuilderState.corner1.getZ()) {
                return 0;
            } else {
                return 180;
            }
        }
        return -1;
    }

    public static float parallelToC1() {
        if (BuilderState.direction == 0) {
            if (BuilderState.corner2.getZ() > BuilderState.corner1.getZ()) {
                return 180;
            } else {
                return 0;
            }
        } else if (BuilderState.direction == 1) {
            if (BuilderState.corner2.getX() > BuilderState.corner1.getX()) {
                return 90;
            } else {
                return 270;
            }
        }
        return -1;
    }

    public static float perpendicularToC1() {
        if (BuilderState.direction == 0) {
            if (BuilderState.corner2.getX() > BuilderState.corner1.getX()) {
                return 90;
            } else {
                return 270;
            }
        } else if (BuilderState.direction == 1) {
            if (BuilderState.corner2.getZ() > BuilderState.corner1.getZ()) {
                return 180;
            } else {
                return 0;
            }
        }
        return -1;
    }
}