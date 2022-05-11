package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.BlockUtils;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public class DigPath1 extends ProcessModule{

    int digMode = 0; // def -> side1 0 -> main1 1 -> main2 2 -> side2 ->3 (post after)
    boolean slowDig = false;
    boolean shouldBeDigging = false;
    boolean initialRotateLeft = false;
    int playerYaw;

    BlockPos startingPoint;

    @Override
    public void onTick() {
        if(!slowDig && shouldBeDigging) {
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
    @Override
    public void onEnable() {
        playerYaw = Math.round(AngleUtils.get360RotationYaw() / 90) < 4 ? Math.round(AngleUtils.get360RotationYaw() / 90) * 90 : 0;
        slowDig = false;
        shouldBeDigging = false;
        digMode = 0;
        mc.thePlayer.inventory.currentItem = 2;
        ExecuteRunnable(initSideTrench);

    }


    @Override
    public void onDisable(){
        digMode = 0;
        slowDig = false;
        shouldBeDigging = false;
        initialRotateLeft = false;
    }
    Runnable SlowDig = new Runnable() {
        @Override
        public void run() {
            if(!enabled)
                return;
            try {
                mc.thePlayer.inventory.currentItem = 2;
                setKeyBindState(keybindAttack, false);
                Thread.sleep(1000);
                while((Math.abs(getBorderBlock().getX() - Math.floor(mc.thePlayer.posX)) > 1 || Math.abs(getBorderBlock().getZ() - Math.floor(mc.thePlayer.posZ)) > 1) && enabled){
                    mc.thePlayer.rotationPitch = 60;
                    Utils.addCustomLog("Digging a block");
                    onTick(keybindAttack);
                    Thread.sleep(1000);
                }
                Utils.addCustomLog("" + digMode);
                if(digMode == 2)
                    ExecuteRunnable(initSideTrench2);
                else
                    ExecuteRunnable(GoBackToCorner);

            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    };
    Runnable GoBackToCorner = new Runnable() {
        @Override
        public void run() {
            shouldBeDigging = false;
            if(!enabled)
                return;
            resetKeybindState();
            AngleUtils.smoothRotateClockwise(180);
            playerYaw = Math.round(AngleUtils.get360RotationYaw() / 90) < 4 ? Math.round(AngleUtils.get360RotationYaw() / 90) * 90 : 0;
            AngleUtils.hardRotate(playerYaw);
            threadSleep(500);
            Utils.addCustomLog("DIG MODE : " + digMode);
            if(digMode == 0 || digMode == 1) {
                while (!BlockUtils.getBlockPosAround(0, 0, 0).equals(startingPoint)) {
                    AngleUtils.hardRotate(playerYaw);
                    setKeyBindState(keybindW, true);
                }
            } else if(digMode == 3){
                while (!((!BlockUtils.isWalkable(BlockUtils.getBlockAround(1, 1, 0)) && !BlockUtils.isWalkable(BlockUtils.getBlockAround(0, 1, 0))) ||
                        (!BlockUtils.isWalkable(BlockUtils.getBlockAround(-1, 1, 0)) && !BlockUtils.isWalkable(BlockUtils.getBlockAround(0, 1, 0)))) && enabled) {
                    AngleUtils.hardRotate(playerYaw);
                    setKeyBindState(keybindW, true);
                }
            }
            resetKeybindState();
            AOTE();
            mc.thePlayer.inventory.currentItem = 2;
            Utils.addCustomLog("Went back to starting point");
            if(digMode == 0)
                ExecuteRunnable(initMainTrench);
            else if(digMode == 1)
                ExecuteRunnable(initMainTrench2);
            else if(digMode == 2)
                ExecuteRunnable(initSideTrench2);
            else
                CaneBuilder.switchToNextProcess(DigPath1.this);

        }
    };

    Runnable initMainTrench = new Runnable() {
        @Override
        public void run() {

            if(!enabled)
                return;
            digMode++;
            Utils.addCustomLog("Initializing dig main trench");
            resetKeybindState();
            if(initialRotateLeft) {
                AngleUtils.smoothRotateAnticlockwise(90, 1.2f);
            } else {
                AngleUtils.smoothRotateClockwise(90, 1.2f);
            }
            AngleUtils.smoothRotatePitchTo(64, 1.2f);
            onTick(keybindAttack);
            threadSleep(500);
            AngleUtils.smoothRotatePitchTo(30, 1.2f);
            onTick(keybindAttack);
            threadSleep(500);
            AngleUtils.smoothRotatePitchTo(25, 1.2f);
            onTick(keybindAttack);
            threadSleep(500);
            playerYaw = Math.round(AngleUtils.get360RotationYaw() / 90) < 4 ? Math.round(AngleUtils.get360RotationYaw() / 90) * 90 : 0;
            resetKeybindState();
            Utils.addCustomLog("Starting to dig main trench");
            shouldBeDigging = true;
            slowDig = false;

        }
    };
    Runnable initMainTrench2 = () -> {
        if(!enabled)
            return;
        digMode++;
        Utils.addCustomLog("Initializing dig main trench 2");
        resetKeybindState();
        if(initialRotateLeft) {
            AngleUtils.smoothRotateClockwise(90, 1.2f);
        } else {
            AngleUtils.smoothRotateAnticlockwise(90, 1.2f);
        }
        setKeyBindState(keyBindShift, true);
        threadSleep(500);
        Utils.goToRelativeBlock(0, 1);
        setKeyBindState(keyBindShift, false);
        AOTE();
        threadSleep(500);
        mc.thePlayer.inventory.currentItem = 2;
        if(initialRotateLeft) {
            AngleUtils.smoothRotateClockwise(90, 1.2f);
        } else {
            AngleUtils.smoothRotateAnticlockwise(90, 1.2f);
        }
        mc.thePlayer.inventory.currentItem = 2;
        playerYaw = Math.round(AngleUtils.get360RotationYaw() / 90) < 4 ? Math.round(AngleUtils.get360RotationYaw() / 90) * 90 : 0;
        threadSleep(500);
        AngleUtils.smoothRotatePitchTo(64, 1.2f);
        onTick(keybindAttack);
        threadSleep(500);
        AngleUtils.smoothRotatePitchTo(30, 1.2f);
        onTick(keybindAttack);
        threadSleep(500);
        AngleUtils.smoothRotatePitchTo(25, 1.2f);
        onTick(keybindAttack);
        threadSleep(500);

        resetKeybindState();
        Utils.addCustomLog("Starting to dig main trench 2");
        shouldBeDigging = true;
        slowDig = false;
    };
    Runnable initSideTrench = new Runnable() {
        @Override
        public void run() {

            Utils.addCustomLog("Initializing dig side trench");

            mc.thePlayer.sendChatMessage("/hub");
            threadSleep(5000);
            mc.thePlayer.sendChatMessage("/warp home");
            threadSleep(5000);
            setKeyBindState(keyBindShift, true);
            threadSleep(500);
            setKeyBindState(keyBindShift, false);
            threadSleep(500);
            if(waterOnTheRight()){
                Utils.goToRelativeBlock(1, 1);
            } else {
                Utils.goToRelativeBlock(-1, 1);
            }
            threadSleep(500);
            AOTE();
            threadSleep(500);
            if(waterOnTheRight()){
                AngleUtils.smoothRotateClockwise(90, 1.2f);
                initialRotateLeft = false;
            } else {
                AngleUtils.smoothRotateAnticlockwise(90, 1.2f);
                initialRotateLeft = true;
            }
            mc.thePlayer.inventory.currentItem = 2;
            playerYaw = Math.round(AngleUtils.get360RotationYaw() / 90) < 4 ? Math.round(AngleUtils.get360RotationYaw() / 90) * 90 : 0;
            AngleUtils.smoothRotatePitchTo(89, 1.2f);
            onTick(keybindAttack);
            threadSleep(500);
            startingPoint = BlockUtils.getBlockPosAround(0, 0, 0);
            AngleUtils.smoothRotatePitchTo(64, 1.2f);
            onTick(keybindAttack);
            threadSleep(500);
            AngleUtils.smoothRotatePitchTo(30, 1.2f);
            onTick(keybindAttack);
            threadSleep(500);
            AngleUtils.smoothRotatePitchTo(25, 1.2f);
            onTick(keybindAttack);
            threadSleep(500);
            shouldBeDigging = true;
            slowDig = false;
            Utils.addCustomLog("Starting to dig side trench");

        }
    };
    Runnable initSideTrench2 = new Runnable() {
        @Override
        public void run() {

            if(!enabled)
                return;
            digMode++;

            Utils.addCustomLog("Initializing dig side trench 2");
            resetKeybindState();
            if(initialRotateLeft) {
                AngleUtils.smoothRotateAnticlockwise(90, 1.2f);
            } else {
                AngleUtils.smoothRotateClockwise(90, 1.2f);
            }
            AngleUtils.smoothRotatePitchTo(30, 1.2f);
            onTick(keybindAttack);
            threadSleep(500);
            AngleUtils.smoothRotatePitchTo(25, 1.2f);
            onTick(keybindAttack);
            threadSleep(500);
            playerYaw = Math.round(AngleUtils.get360RotationYaw() / 90) < 4 ? Math.round(AngleUtils.get360RotationYaw() / 90) * 90 : 0;
            resetKeybindState();
            Utils.addCustomLog("Starting to dig side trench 2");
            shouldBeDigging = true;
            slowDig = false;

        }
    };

    boolean waterOnTheRight(){
        for(int i = 0; i < 5; i++){
            if(BlockUtils.getBlockAround(i, 3, -2).equals(Blocks.water)
                    || BlockUtils.getBlockAround(i, 3, -2).equals(Blocks.flowing_water)){
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
            if(BlockUtils.getBlockAround(0, i, -1) != Blocks.air) {
                if (BlockUtils.getBlockAround(0, i + 1, -1) == Blocks.air)
                    return new BlockPos(BlockUtils.getUnitX() * i + X, Y - 1, BlockUtils.getUnitZ() * i + Z);
            }
        }
        return null;
    }


}
