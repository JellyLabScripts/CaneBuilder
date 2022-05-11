package com.jelly.CaneBuilder.utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;


public class BlockUtils {
    static  Minecraft mc = Minecraft.getMinecraft();
    public static int getUnitX() {
        double modYaw = (mc.thePlayer.rotationYaw % 360 + 360) % 360;
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
        double modYaw = (mc.thePlayer.rotationYaw % 360 + 360) % 360;
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
    public static boolean isWalkable(Block block) {
        return block == Blocks.air || block == Blocks.water || block == Blocks.flowing_water || block == Blocks.reeds;
    }

    // 0, 0 = initial block
    public static Block getBlockAround(int rightOffset, int frontOffset){
        return getBlockAround(rightOffset, frontOffset, 0);
    }
    public static Block getBlockAround(int rightOffset, int frontOffset, int upOffset){
        Minecraft mc = Minecraft.getMinecraft();
        int X = (int)Math.round(Math.floor(mc.thePlayer.posX));
        int Y = (int)Math.round(Math.floor(mc.thePlayer.posY));
        int Z = (int)Math.round(Math.floor(mc.thePlayer.posZ));
        return (mc.theWorld.getBlockState(
                new BlockPos(getUnitZ() * -1 * rightOffset + getUnitX() * frontOffset + X, Y + upOffset,//
                        getUnitX() * rightOffset + getUnitZ() * frontOffset + Z)).getBlock());

    }
    public static BlockPos getBlockPosAround(int rightOffset, int frontOffset, int upOffset){
        Minecraft mc = Minecraft.getMinecraft();
        int X = (int)Math.round(Math.floor(mc.thePlayer.posX));
        int Y = (int)Math.round(Math.floor(mc.thePlayer.posY));
        int Z = (int)Math.round(Math.floor(mc.thePlayer.posZ));
        return new BlockPos(getUnitZ() * -1 * rightOffset + getUnitX()* frontOffset + X, Y + upOffset,
                getUnitX() * rightOffset + getUnitZ() * frontOffset + Z);

    }
    public static Block getFrontBlock(){
       return getBlockAround(0, 1, 0);
    }
    public static Block getBackBlock(){
        return getBlockAround(0, -1, 0);
    }
    public static Block getRightBlock(){
        return getBlockAround(1, 0, 0);
    }

    public static Block getLeftBlock(){
        return getBlockAround(-1, 0, 0);
    }


}
