package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.BlockUtils;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import scala.tools.nsc.Global;

import java.util.concurrent.TimeUnit;


public class DigTrench extends ProcessModule{

    boolean inDiggingTrench = false;
    boolean slowDig = false;
    boolean goLeft = false;
    int playerYaw = 0;

    @Override
    public void onTick() {
        if(inDiggingTrench){
            if(!slowDig) {
                if(getBorderBlock() != null) {

                    //getting close to border
                    if (Math.abs(getBorderBlock().getX() - mc.thePlayer.posX) < 7 && Math.abs(getBorderBlock().getZ() - mc.thePlayer.posZ) < 7) {
                        Utils.addCustomLog("Slow digging, border block = " + getBorderBlock());
                        slowDig = true;
                        ExecuteRunnable(SlowDig);
                        setKeyBindState(keybindAttack, false);
                        return;
                    }
                }
                AngleUtils.hardRotate(playerYaw);
                mc.thePlayer.rotationPitch = 20;
                setKeyBindState(keybindW, true);
                setKeyBindState(keybindAttack, true);
            }
        }

    }
    @Override
    public void onEnable(){
        goLeft = onRightSideOfFarmInitial();
        new Thread(() -> {
            while(BlockUtils.isWalkable(BlockUtils.getBlockAround(0, -1, -1))){
                setKeyBindState(keyBindShift, true);
                setKeyBindState(keybindW, true);
                threadSleep(1);
            }
            resetKeybindState();
            threadSleep(100);
            playerYaw = Math.round(AngleUtils.get360RotationYaw() / 90) < 4 ? Math.round(AngleUtils.get360RotationYaw() / 90) * 90 : 0;
            ExecuteRunnable(GoToNextTrench);

        }).start();

    }
    @Override
    public void onDisable(){
        inDiggingTrench = false;
        slowDig = false;
        goLeft = false;
        playerYaw = 0;
    }

    Runnable SlowDig = new Runnable() {
        @Override
        public void run() {
            if(!enabled)
                return;
            try {
                mc.thePlayer.inventory.currentItem = 2;
                inDiggingTrench = true;
                setKeyBindState(keybindAttack, false);
                Thread.sleep(1000);
                while((Math.abs(getBorderBlock().getX() - Math.floor(mc.thePlayer.posX)) > 2 || Math.abs(getBorderBlock().getZ() - Math.floor(mc.thePlayer.posZ)) > 2) && enabled){
                    mc.thePlayer.rotationPitch = 60;
                    Utils.addCustomLog("Digging a block");
                    onTick(keybindAttack);
                    Thread.sleep(1000);
                }
                ExecuteRunnable(JumpUpTrench);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    };
    Runnable JumpUpTrench = new Runnable() {
        @Override
        public void run() {
            try {
                if(!enabled)
                    return;

                mc.thePlayer.inventory.currentItem = 2;
                setKeyBindState(keybindW, true);
                setKeyBindState(keyBindJump, true);
                Thread.sleep(200);
                setKeyBindState(keyBindJump, false);
                Thread.sleep(200);
                resetKeybindState();
                Thread.sleep(500);


                if (goLeft)
                    Utils.addCustomLog("Going left");
                else
                    Utils.addCustomLog("Going Right");

                if (mc.thePlayer.rotationYaw < 180)
                    AngleUtils.smoothRotateClockwise(180, 2);
                else
                    AngleUtils.smoothRotateAnticlockwise(180, 2);
                Thread.sleep(1000);
                if(shouldEndDiggingTrench()){
                    Utils.addCustomLog("Dig trench completed");
                    CaneBuilder.switchToNextProcess(DigTrench.this);
                } else {
                    ExecuteRunnable(GoToNextTrench);
                }
            } catch(Exception e){
                e.printStackTrace();
            }

        }
    };
    Runnable GoToNextTrench = new Runnable() {
        @Override
        public void run() {
            try {
                BlockPos targetBlockPos;
                mc.thePlayer.inventory.currentItem = 2;
                if (goLeft)
                    targetBlockPos = new BlockPos(BlockUtils.getUnitZ() * -1 * -3 + mc.thePlayer.posX, mc.thePlayer.posY,
                            BlockUtils.getUnitX() * -3 + mc.thePlayer.posZ);
                else
                    targetBlockPos = new BlockPos(BlockUtils.getUnitZ() * -1 * 3 + mc.thePlayer.posX, mc.thePlayer.posY,
                            BlockUtils.getUnitX() * 3 + mc.thePlayer.posZ);
                resetKeybindState();
                Utils.addCustomLog("target block : " + targetBlockPos);
                while ((Math.floor(mc.thePlayer.posX) != targetBlockPos.getX() || Math.floor(mc.thePlayer.posZ) != targetBlockPos.getZ()) && enabled) {
                    setKeyBindState(keyBindShift, true);
                    setKeyBindState(keybindA, goLeft);
                    setKeyBindState(keybindD, !goLeft);
                    Thread.sleep(1);
                }
                resetKeybindState();
                Thread.sleep(50);
                setKeyBindState(keyBindShift, true);
                // NEED ALIGNMENT
                // Utils.align();
                Thread.sleep(500);
                Utils.addCustomLog("Starting new row");
                goLeft = !goLeft;

                if (playerYaw < 180)
                    playerYaw += 180;
                else
                    playerYaw -= 180;
                resetKeybindState();
                AOTE();
                mc.thePlayer.inventory.currentItem = 2;
                ScheduleRunnable(InitializeDig, 1, TimeUnit.SECONDS);
                return;

            } catch(Exception e){
                e.printStackTrace();
            }

        }
    };
    Runnable InitializeDig = () -> {
        try {
            if(!enabled)
                return;
            mc.thePlayer.inventory.currentItem = 2;
            playerYaw = Math.round(AngleUtils.get360RotationYaw() / 90) < 4 ? Math.round(AngleUtils.get360RotationYaw() / 90) * 90 : 0;
            Utils.addCustomLog("Initialize digging");
            resetKeybindState();
            Utils.addCustomLog("Player Yaw : " + playerYaw);
            AngleUtils.hardRotate(playerYaw);
            AngleUtils.smoothRotatePitchTo(60, 1);
            onTick(keybindAttack);
            Thread.sleep(500);
            AngleUtils.smoothRotatePitchTo(50, 1);
            onTick(keybindAttack);
            Thread.sleep(500);
            AngleUtils.smoothRotatePitchTo(30, 1);
            onTick(keybindAttack);
            Thread.sleep(500);
            AngleUtils.smoothRotatePitchTo(25, 1);
            onTick(keybindAttack);
            Thread.sleep(300);
            setKeyBindState(keybindW, true);
            Thread.sleep(300);
            setKeyBindState(keybindW, false);
            Thread.sleep(100);
            Utils.addCustomLog("Pressing S");
            setKeyBindState(keybindS, true);
            Thread.sleep(300);
            setKeyBindState(keybindS, false);
            Thread.sleep(300);
            if(!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())) {
                inDiggingTrench = true;
                slowDig = false;
                return;
            }
            else {
                Utils.addCustomLog("Wrong location");
            }


        }catch(Exception e) {
            e.printStackTrace();
        }
    };
    boolean shouldEndDiggingTrench(){
        if(onRightSideOfFarm()) {
            Utils.addCustomLog("On right side of farm");
            if(BlockUtils.getBlockAround(-3, 1, -1).equals(Blocks.air) && !BlockUtils.getBlockAround(-4, 1, -1).equals(Blocks.air)) {
                for (int i = 0; i < 5; i++) {
                    if (BlockUtils.getBlockAround(i, 0, -1).equals(Blocks.air))
                        return true;
                }
            }
        } else {
            Utils.addCustomLog("On left side of farm");
            if(BlockUtils.getBlockAround(3, 1, -1).equals(Blocks.air) && !BlockUtils.getBlockAround(4, 1, -1).equals(Blocks.air)) {
                for (int i = 0; i > -5; i--) {
                    if (BlockUtils.getBlockAround(i, 0, -1).equals(Blocks.air))
                        return true;
                }
            }
        }
        return false;

    }
    boolean onRightSideOfFarm(){
        for (int i = 0; i < 10; i++) {
            if (BlockUtils.isWalkable(BlockUtils.getBlockAround(i, -1, -1))) {
                return true;
            }
        }
        return false;
    }
    boolean onRightSideOfFarmInitial(){
        for (int i = 0; i < 10; i++) {
            if (BlockUtils.isWalkable(BlockUtils.getBlockAround(i, 0, -1))) {
                return true;
            }
        }
        return false;
    }
    BlockPos getBorderBlock(){
        double X = mc.thePlayer.posX;
        double Y = mc.thePlayer.posY;
        double Z = mc.thePlayer.posZ;
        for(int i = 0; i < 10; i++){
            if(BlockUtils.getBlockAround(0, i, 0) != Blocks.air) {
                if (BlockUtils.getBlockAround(0, i + 1, 0) == Blocks.air)
                    return new BlockPos(BlockUtils.getUnitX() * i + X, Y - 1, BlockUtils.getUnitZ() * i + Z);
            }
        }
        return null;
    }

}
