package com.jelly.CaneBuilder.utils;
import com.jelly.CaneBuilder.BuilderState;
import net.minecraft.client.Minecraft;

public class AngleUtils extends Utils{
    private static Minecraft mc = Minecraft.getMinecraft();


    public static float get360RotationYaw() {
        return get360RotationYaw(mc.thePlayer.rotationYaw);
    }

    public static float get360RotationYaw(float yaw) {
        return (yaw % 360 + 360) % 360;
    }

    public static float getOppositeAngle(int angle) {
        return (angle < 180) ? angle + 180 : angle - 180;
    }

    public static boolean shouldRotateClockwise(float initialYaw360, float targetYaw360) {
        return clockwiseDifference(initialYaw360, targetYaw360) < 180;
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

    public static void smoothRotateTo(float targetYaw360, float speed) {
        if (shouldRotateClockwise(get360RotationYaw(), targetYaw360)) {
            smoothRotateClockwise(clockwiseDifference(get360RotationYaw(), targetYaw360), speed);
        } else {
            smoothRotateAnticlockwise(antiClockwiseDifference(get360RotationYaw(), targetYaw360), speed);
        }
    }

    public static void smoothRotateClockwise(float rotateAngle) {
        smoothRotateClockwise(rotateAngle, 1);
    }

    public static void smoothRotateClockwise(float rotateAngle, float speed) {
        float targetYaw = (get360RotationYaw() + rotateAngle) % 360;
        while (get360RotationYaw() != targetYaw) {
            if (Math.abs(get360RotationYaw() - targetYaw) < speed) {
                mc.thePlayer.rotationYaw += Math.abs(get360RotationYaw() - targetYaw);
                return;
            }
            mc.thePlayer.rotationYaw += (0.3f + Utils.nextInt(3) / 10.0f) * speed;
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void sineRotateCW(float rotateAngle, float speed) {
        float targetYaw = (get360RotationYaw() + rotateAngle) % 360;
        while (get360RotationYaw() != targetYaw) {
            float difference = Math.abs(get360RotationYaw() - targetYaw);
            if (difference < 0.4f * speed) {
                mc.thePlayer.rotationYaw += difference;
                return;
            }
            mc.thePlayer.rotationYaw += speed * 0.3 * ((difference / rotateAngle) + (Math.PI / 2));
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void smoothRotateAnticlockwise(float rotateAngle) {
        smoothRotateAnticlockwise(rotateAngle, 1);
    }

    public static void smoothRotateAnticlockwise(float rotateAngle, float speed) {
        float targetYaw = get360RotationYaw(get360RotationYaw() - rotateAngle);
        while (get360RotationYaw() != targetYaw) {
            if (Math.abs(get360RotationYaw() - targetYaw) < speed) {
                mc.thePlayer.rotationYaw -= Math.abs(get360RotationYaw() - targetYaw);
                return;
            }
            mc.thePlayer.rotationYaw -= (0.3f + Utils.nextInt(3) / 10.0f) * speed;
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void sineRotateACW(float rotateAngle, float speed) {
        float targetYaw = get360RotationYaw(get360RotationYaw() - rotateAngle);
        while (get360RotationYaw() != targetYaw) {
            float difference = Math.abs(get360RotationYaw() - targetYaw);
            if (difference < 0.4f * speed) {
                mc.thePlayer.rotationYaw -= difference;
                return;
            }
            mc.thePlayer.rotationYaw -= speed * 0.3 * ((difference / rotateAngle) + (Math.PI / 2));
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static float getActualRotationYaw() { //f3
        return getActualRotationYaw(mc.thePlayer.rotationYaw);
    }

    public static float getActualRotationYaw(float yaw) { //f3
        return yaw > 0 ?
                (yaw % 360 > 180 ? -(180 - (yaw % 360 - 180)) : yaw % 360) :
                (-yaw % 360 > 180 ? (180 - (-yaw % 360 - 180)) : -(-yaw % 360));
    }

    public static void hardRotate(float yaw360) {
        while (get360RotationYaw() != yaw360) {
            if (Math.abs(get360RotationYaw() - yaw360) < 0.2f) {
                mc.thePlayer.rotationYaw = yaw360;
                return;
            }
            if (shouldRotateClockwise(get360RotationYaw(), yaw360)) {
                mc.thePlayer.rotationYaw += 0.1f;
            } else {
                mc.thePlayer.rotationYaw -= 0.1f;
            }
        }
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