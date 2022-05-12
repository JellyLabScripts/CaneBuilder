package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.BlockUtils;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import scala.collection.immutable.ListSet;

import java.util.ArrayList;

public class DigPath2 extends ProcessModule{
    boolean walkingForward;
    double initialX = 0;
    double initialZ = 0;
    boolean pushedOff;
    boolean AOTEing = false;
    public BlockPos targetBlockPos= new BlockPos(10000, 10000, 10000);

    int playerYaw;
    direction currentDirection;
    direction lastLaneDirection;
    @Override
    public void onTick(){
        if(AOTEing){
            resetKeybindState();
            return;
        }
        double dx = Math.abs(mc.thePlayer.posX - mc.thePlayer.lastTickPosX);
        double dy = Math.abs(mc.thePlayer.posY - mc.thePlayer.lastTickPosY);
        double dz = Math.abs(mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ);
        Block blockStandingOn = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)).getBlock();
        Block blockIn = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)).getBlock();
        BlockPos blockInPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);

        if(((!BlockUtils.isWalkable(BlockUtils.getBlockAround(1, 1, 0)) && !BlockUtils.isWalkable(BlockUtils.getBlockAround(0, 1, 0))) ||
                        (!BlockUtils.isWalkable(BlockUtils.getBlockAround(-1, 1, 0)) && !BlockUtils.isWalkable(BlockUtils.getBlockAround(0, 1, 0))))){
            walkingForward = false;
            Utils.addCustomMessage("Ended process");
            CaneBuilder.switchToNextProcess(this);
            updateKeys(false, false, false, false, false, false, false);
            return;
        }
        mc.gameSettings.pauseOnLostFocus = false;
        mc.gameSettings.gammaSetting = 100;
        //angles (locked)
        mc.thePlayer.rotationPitch = 11;
        mc.thePlayer.inventory.currentItem = 2;
        AngleUtils.hardRotate(playerYaw);


        //states
        if (dy == 0) {
            if (!walkingForward) { //normal

                setKeyBindState(keyBindShift, false);
                if (currentDirection.equals(direction.RIGHT))
                    setKeyBindState(keybindD, true);
                else if (currentDirection.equals(direction.LEFT))
                    setKeyBindState(keybindA, true);
                else
                    walkingForward = true;
            } else { // walking forward

                //hole drop fix (prevent sneaking at the hole)
                setKeyBindState(keyBindShift, !BlockUtils.isWalkable(blockStandingOn));

                //unleash keys
                if (lastLaneDirection.equals(direction.LEFT))
                    updateKeys(mc.gameSettings.keyBindForward.isKeyDown(), mc.gameSettings.keyBindBack.isKeyDown(), mc.gameSettings.keyBindLeft.isKeyDown(), false, false);
                else
                    updateKeys(mc.gameSettings.keyBindForward.isKeyDown(), mc.gameSettings.keyBindBack.isKeyDown(), false, mc.gameSettings.keyBindRight.isKeyDown(), false);

                //push keys so the next tick it will unleash
                while (!pushedOff && !lastLaneDirection.equals(direction.NONE)) {
                    if (lastLaneDirection.equals(direction.LEFT)) {
                        Utils.addCustomLog("Bouncing to the right");
                        updateKeys(mc.gameSettings.keyBindForward.isKeyDown(), mc.gameSettings.keyBindBack.isKeyDown(), mc.gameSettings.keyBindLeft.isKeyDown(), true, false);
                    } else {
                        Utils.addCustomLog("Bouncing to the left");
                        updateKeys(mc.gameSettings.keyBindForward.isKeyDown(), mc.gameSettings.keyBindBack.isKeyDown(), true, mc.gameSettings.keyBindRight.isKeyDown(), false);
                    }
                    pushedOff = true;
                }
                setKeyBindState(keybindW, true);
            }
        }


        //change to walk forward
        if (Utils.roundTo2DecimalPlaces(dx) == 0 && Utils.roundTo2DecimalPlaces(dz) == 0) {
            if (shouldWalkForward() && !walkingForward && ((int) initialX != (int) mc.thePlayer.posX || (int) initialZ != (int) mc.thePlayer.posZ)) {
                updateKeys(true, false, false, false, false);
                walkingForward = true;
                targetBlockPos = calculateTargetBlockPos();
                Utils.addCustomLog("Target block : " + targetBlockPos.toString());
                pushedOff = false;
                initialX = mc.thePlayer.posX;
                initialZ = mc.thePlayer.posZ;
            }
        }

        //chagnge back to left/right
        if (blockInPos.getX() == targetBlockPos.getX() && blockInPos.getZ() == targetBlockPos.getZ() && walkingForward ) {//&& BlockUtils.isInCenterOfBlock()
            new Thread(() -> {
                AOTEing = true;
                AOTE();
                AngleUtils.smoothRotatePitchTo(11, 1.2f);
                threadSleep(500);
                mc.thePlayer.sendChatMessage("/setspawn");
                updateKeys(false, false, false, false, true);

                initialX = mc.thePlayer.posX;
                initialZ = mc.thePlayer.posZ;

                if (!BlockUtils.isWalkable(BlockUtils.getLeftBlock()) || !BlockUtils.isWalkable(BlockUtils.getBlockAround(-2, 0))) {
                    //set last lane dir
                    currentDirection = direction.RIGHT;
                    lastLaneDirection = direction.RIGHT;
                } else if (!BlockUtils.isWalkable(BlockUtils.getRightBlock()) || !BlockUtils.isWalkable(BlockUtils.getBlockAround(2, 0))) {
                    currentDirection = direction.LEFT;
                    lastLaneDirection = direction.LEFT;
                }
                walkingForward = false;
                AOTEing = false;
            }).start();

        }
        setKeyBindState(keybindAttack, !walkingForward && !shouldEndDigging());
    }

    @Override
    public void onEnable(){

        initialX = mc.thePlayer.posX;
        initialZ = mc.thePlayer.posZ;
        pushedOff = false;
        AOTEing = true;
        walkingForward = false;

        new Thread(() -> {

            AngleUtils.smoothRotateClockwise(180);
            threadSleep(500);
            currentDirection = calculateDirection();
            lastLaneDirection = calculateDirection();
            AOTE();
            threadSleep(500);
            AngleUtils.smoothRotatePitchTo(11, 1.2f);
            threadSleep(500);
            playerYaw = Math.round(AngleUtils.get360RotationYaw() / 90) < 4 ? Math.round(AngleUtils.get360RotationYaw() / 90) * 90 : 0;
            AOTEing = false;
        }).start();

    }
    boolean shouldEndDigging(){
        for(int i = 0; i < 5; i++) {
            if(BlockUtils.getBlockAround(0, i, 0).equals(Blocks.air) &&
                    BlockUtils.getBlockAround(0, i + 1, 0).equals(Blocks.air) &&
                    BlockUtils.getBlockAround(0, i + 2, 0).equals(Blocks.air))
                return true;
        }
        return false;

    }
    boolean shouldWalkForward() {
       return (BlockUtils.isWalkable(BlockUtils.getBackBlock()) && BlockUtils.isWalkable(BlockUtils.getFrontBlock())) ||
                       (!BlockUtils.isWalkable(BlockUtils.getBackBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())) ||
                       (!BlockUtils.isWalkable(BlockUtils.getBackBlock()) && !BlockUtils.isWalkable(BlockUtils.getRightBlock())) ||
                       (!BlockUtils.isWalkable(BlockUtils.getFrontBlock()) && !BlockUtils.isWalkable(BlockUtils.getRightBlock())) ||
                       (!BlockUtils.isWalkable(BlockUtils.getFrontBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())) ||
                       (!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock()));

    }
    BlockPos calculateTargetBlockPos(){
        if(!BlockUtils.isWalkable(BlockUtils.getRightBlock()) || !BlockUtils.isWalkable(BlockUtils.getLeftBlock())){
            if(!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())){
                return BlockUtils.getBlockPosAround(0, 1, 0);
            } else {
                if(!BlockUtils.isWalkable(BlockUtils.getBlockAround(-1, 1, 0)) && !BlockUtils.isWalkable(BlockUtils.getBlockAround(1, 1, 0))) {
                    Utils.addCustomLog("Detected one block off");
                    return BlockUtils.getBlockPosAround(0, 2, 0);
                }
                else {
                    return BlockUtils.getBlockPosAround(0, 3, 0);
                }

            }
        }

        Utils.addCustomLog("can't calculate target block!");
        return new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);


    }
    direction calculateDirection() {
        ArrayList<Integer> unwalkableBlocks = new ArrayList<>();
        if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)).getBlock().equals(Blocks.end_portal_frame)) {
            for (int i = -3; i < 3; i++) {
                if (!BlockUtils.isWalkable(BlockUtils.getBlockAround(i, 0, 1))) {
                    unwalkableBlocks.add(i);
                }
            }
        } else {
            for (int i = -3; i < 3; i++) {
                if (!BlockUtils.isWalkable(BlockUtils.getBlockAround(i, 0))) {
                    unwalkableBlocks.add(i);
                }
            }
        }

        if (unwalkableBlocks.size() == 0)
            return direction.RIGHT;
        else if (unwalkableBlocks.size() > 1 && hasPosAndNeg(unwalkableBlocks)) {
            return direction.NONE;
        } else if (unwalkableBlocks.get(0) > 0)
            return direction.LEFT;
        else
            return direction.RIGHT;
    }


    boolean hasPosAndNeg(ArrayList<Integer> ar) {
        boolean hasPos = false;
        boolean hasNeg = false;
        for (Integer integer : ar) {
            if (integer < 0)
                hasNeg = true;
            else
                hasPos = true;
        }
        return hasPos && hasNeg;

    }


}
