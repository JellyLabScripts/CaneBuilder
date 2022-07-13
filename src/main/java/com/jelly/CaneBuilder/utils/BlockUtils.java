package com.jelly.CaneBuilder.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;


public class BlockUtils {
    static Minecraft mc = Minecraft.getMinecraft();

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

    public static boolean isAStraightLine(BlockPos b1, BlockPos b2, BlockPos b3){
        if((b1.getX() - b2.getX()) == 0 || (b2.getX() - b3.getX()) == 0 || (b1.getX() - b3.getX()) == 0)
            return (b1.getX() - b2.getX()) == 0 && (b2.getX() - b3.getX()) == 0 && (b1.getX() - b3.getX()) == 0 && b1.getY() == b2.getY() && b2.getY()== b3.getY();
        return ((b1.getZ() - b2.getZ())/(b1.getX() - b2.getX()) == (b2.getZ() - b3.getZ())/(b2.getX() - b3.getX()) &&
                (b1.getZ() - b2.getZ())/(b1.getX() - b2.getX()) == (b1.getZ() - b3.getZ())/(b1.getX() - b3.getX())) && b1.getY() == b2.getY() && b2.getY()== b3.getY();

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
    public static Block getBlockAround(int rightOffset, int frontOffset) {
        return getBlockAround(rightOffset, frontOffset, 0);
    }

    public static Block getBlockAround(int rightOffset, int frontOffset, int upOffset) {
        int X = (int) Math.round(Math.floor(mc.thePlayer.posX));
        int Y = (int) Math.round(Math.floor(mc.thePlayer.posY));
        int Z = (int) Math.round(Math.floor(mc.thePlayer.posZ));
        return (mc.theWorld.getBlockState(
          new BlockPos(getUnitZ() * -1 * rightOffset + getUnitX() * frontOffset + X, Y + upOffset,
            getUnitX() * rightOffset + getUnitZ() * frontOffset + Z)).getBlock());
    }

    public static Block getBlockAroundFrom(BlockPos from, int rightOffset, int frontOffset, int upOffset) {
        int X = from.getX();
        int Y = from.getY();
        int Z = from.getZ();
        return (mc.theWorld.getBlockState(
          new BlockPos(getUnitZ() * -1 * rightOffset + getUnitX() * frontOffset + X, Y + upOffset,
            getUnitX() * rightOffset + getUnitZ() * frontOffset + Z)).getBlock());
    }

    public static BlockPos getBlockPosAround(int rightOffset, int frontOffset, int upOffset) {
        int X = (int) Math.round(Math.floor(mc.thePlayer.posX));
        int Y = (int) Math.round(Math.floor(mc.thePlayer.posY));
        int Z = (int) Math.round(Math.floor(mc.thePlayer.posZ));
        return new BlockPos(getUnitZ() * -1 * rightOffset + getUnitX() * frontOffset + X, Y + upOffset,
          getUnitX() * rightOffset + getUnitZ() * frontOffset + Z);

    }

    public static Block getFrontBlock() {
        return getBlockAround(0, 1, 0);
    }

    public static Block getBackBlock() {
        return getBlockAround(0, -1, 0);
    }

    public static Block getRightBlock() {
        return getBlockAround(1, 0, 0);
    }

    public static Block getLeftBlock() {
        return getBlockAround(-1, 0, 0);
    }

    public static int countCarpet() {
        int r = 2;
        int count = 0;
        BlockPos playerPos = mc.thePlayer.getPosition();
        playerPos.add(0, 1, 0);
        Vec3i vec3i = new Vec3i(r, r, r);
        Vec3i vec3i2 = new Vec3i(r, r, r);
        for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i2))) {
            IBlockState blockState = mc.theWorld.getBlockState(blockPos);
            if (blockState.getBlock() == Blocks.carpet && blockState.getValue(BlockCarpet.COLOR) == EnumDyeColor.BROWN) {
                count++;
            }
        }
        return count;
    }


    public static double getDistanceBetweenTwoBlock(BlockPos b1, BlockPos b2){
        return Math.sqrt((b1.getX() - b2.getX()) * (b1.getX() - b2.getX())
                + (b1.getY() - b2.getY()) * (b1.getY() - b2.getY())
                + (b1.getZ() - b2.getZ()) * (b1.getZ() - b2.getZ()));
    }
    public static int getBlockDistanceBetweenTwoBlock(BlockPos b1, BlockPos b2){
        return Math.abs(b1.getX() - b2.getX())
                + Math.abs((b1.getY() - b2.getY()))
                + Math.abs((b1.getZ() - b2.getZ()));
    }
}
