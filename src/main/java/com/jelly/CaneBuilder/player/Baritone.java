package com.jelly.CaneBuilder.player;

import com.jelly.CaneBuilder.handlers.KeyBindHandler;
import com.jelly.CaneBuilder.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Baritone {

    static List<BlockPos> blocksToWalk = new ArrayList<>();
    static Minecraft mc = Minecraft.getMinecraft();
    public static boolean walking = false;
    static BlockPos endingBlock;
    static Rotation rotation = new Rotation();

    static int blockStep = 0;
    static int deltaJumpTick = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event){
        if(event.phase == TickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null)
            return;

        if(walking){

            if (!rotation.completed) {
                KeyBindHandler.resetKeybindState();
                return;
            }
            KeyBindHandler.setKeyBindState(KeyBindHandler.keyBindShift, blocksToWalk.size() <= 5 || !BlockUtils.isAStraightLine(blocksToWalk.get(blocksToWalk.size() - 1), blocksToWalk.get(blocksToWalk.size() - 3), blocksToWalk.get(blocksToWalk.size() - 5)));

            if(Math.floor(mc.thePlayer.posX) == blocksToWalk.get(blocksToWalk.size() - 1).getX() && Math.floor(mc.thePlayer.posY) == blocksToWalk.get(blocksToWalk.size() - 1).getY() && Math.floor(mc.thePlayer.posZ) == blocksToWalk.get(blocksToWalk.size() - 1).getZ()){
                LogUtils.addCustomLog("Removed");
                blocksToWalk.remove(blocksToWalk.size() - 1);
            }
            if(blocksToWalk.size() == 0){
                walking = false;
                KeyBindHandler.resetKeybindState();
                BlockRenderer.renderMap.clear();
                return;
            }

            if(AngleUtils.getRelativeYawFromBlockPos(blocksToWalk.get(blocksToWalk.size() - 1)) != -1){
                rotation.lockAngle((int) (AngleUtils.getClosest() + AngleUtils.getRelativeYawFromBlockPos(blocksToWalk.get(blocksToWalk.size() - 1))), 0);
            } else { //block is up or down
                if(deltaJumpTick == 0) {
                    if (blocksToWalk.size() > 2) {
                        rotation.lockAngle((int) (AngleUtils.getClosest() + AngleUtils.getRelativeYawFromBlockPos(blocksToWalk.get(blocksToWalk.size() - 2))), 0);
                        LogUtils.addCustomLog("Blocks to walk > 2");
                    } else {
                        rotation.lockAngle((int) (AngleUtils.getClosest() + AngleUtils.getRelativeYawFromBlockPos(endingBlock)), 0);
                        LogUtils.addCustomLog("Blocks to walk == 1 or 2");
                    }
                }
                if(rotation.completed) {
                    LogUtils.addCustomLog("rotation completed");
                    deltaJumpTick = 3;
                }
            }
            if(deltaJumpTick > 0){
                deltaJumpTick--;
                System.out.println("delta jump tick " + deltaJumpTick);
                KeyBindHandler.setKeyBindState(KeyBindHandler.keyBindJump, true);
            } else KeyBindHandler.setKeyBindState(KeyBindHandler.keyBindJump, false);


            KeyBindHandler.setKeyBindState(KeyBindHandler.keybindW, rotation.completed);
           // KeyBindHandler.setKeyBindState(KeyBindHandler.keyBindShift, BlockUtils.getBlockPosAround(0, 0, 0).equals(endingBlock));


        }



    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event){
        rotation.update();
    }

    public static void walkTo(BlockPos endBlock) throws Exception{
        endingBlock = endBlock;
        calculateBlocksToWalk(endingBlock);
        walking = true;
        for(BlockPos blockPos : blocksToWalk){
            System.out.println(blockPos);
        }
    }
    public static void stopWalk(){
        walking = false;
        BlockRenderer.renderMap.clear();
    }
    public static void calculateBlocksToWalk(BlockPos startingBlock, BlockPos endingBlock) throws Exception{
        blockStep++;
        if(blockStep > 300) {
            throw new Exception();
        }
        if (!startingBlock.equals(endingBlock)) {
            calculateBlocksToWalk(getClosetAdjacentBlock(startingBlock, endingBlock), endingBlock);
            blocksToWalk.add(getClosetAdjacentBlock(startingBlock, endingBlock));
            BlockRenderer.renderMap.put(startingBlock, Color.GREEN);
        }
    }

    public static void calculateBlocksToWalk(BlockPos endingBlock) throws Exception{
        BlockRenderer.renderMap.clear();
        BlockRenderer.renderMap.put(endingBlock, Color.RED);
        clearBlocksToWalk();
        calculateBlocksToWalk(new BlockPos(Math.floor(mc.thePlayer.posX), Math.floor(mc.thePlayer.posY), Math.floor(mc.thePlayer.posZ)), endingBlock);
    }

    private static void clearBlocksToWalk(){
        blocksToWalk.clear();
        blockStep = 0;
    }


    public static BlockPos getClosetAdjacentBlock(BlockPos startingBlock, BlockPos endingBlock) {

        BlockPos[] possibleBlockPos = new BlockPos[6];
        possibleBlockPos[0] = startingBlock.add(-1, 0, 0);
        possibleBlockPos[1] = startingBlock.add(0, 0, -1);
        possibleBlockPos[2] = startingBlock.add(1, 0, 0);
        possibleBlockPos[3] = startingBlock.add(0, 0, 1);
        possibleBlockPos[4] = startingBlock.add(0, 1, 0);
        possibleBlockPos[5] = startingBlock.add(0, -1, 0);
        double min = 99;//MathUtils.getDistanceBetweenTwoBlock(possibleBlockPos[0], endingBlock)
        int value = -1;
        for (int i = 0; i < possibleBlockPos.length; i++) {
            if (!BlockUtils.isWalkable(mc.theWorld.getBlockState(possibleBlockPos[i]).getBlock()) || !BlockUtils.isWalkable(mc.theWorld.getBlockState(possibleBlockPos[i].up()).getBlock()))
                continue;

            if (BlockUtils.getBlockDistanceBetweenTwoBlock(possibleBlockPos[i], endingBlock) < min && !possibleBlockPos[i].equals(startingBlock)) {
                min = BlockUtils.getBlockDistanceBetweenTwoBlock(possibleBlockPos[i], endingBlock);
                value = i;
            }
        }
        if(value != -1)
            return possibleBlockPos[value];
        else return null;
    }
}
