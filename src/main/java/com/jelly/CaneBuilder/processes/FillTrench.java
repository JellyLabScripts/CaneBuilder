package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.BlockUtils;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.util.concurrent.TimeUnit;


public class FillTrench extends ProcessModule{

    boolean goLeft = false;
    int playerYaw = 0;

    @Override
    public void onTick() {
    }
    @Override
    public void onEnable(){
        new Thread(() -> {
            playerYaw = Math.round(AngleUtils.get360RotationYaw() / 90) < 4 ? Math.round(AngleUtils.get360RotationYaw() / 90) * 90 : 0;
            AOTE();
            ExecuteRunnable(placePrismapumpAndWater);
            goLeft = onRightSideOfFarm();
        }).start();

    }
    @Override
    public void onDisable(){
        goLeft = false;
        playerYaw = 0;
    }

    Runnable goToNextWaterTrench = new Runnable() {
        @Override
        public void run() {
            try {
                if(!enabled)
                    return;
                if(shouldEndPlacingWater()){
                    resetKeybindState();
                    Utils.addCustomLog("Place water completed");
                    CaneBuilder.switchToNextProcess(FillTrench.this);
                    return;
                }

                BlockPos targetBlockPos;
                if (goLeft)
                    targetBlockPos = new BlockPos(BlockUtils.getUnitZ() * -1 * -3 + mc.thePlayer.posX, mc.thePlayer.posY,
                            BlockUtils.getUnitX() * -3 + mc.thePlayer.posZ);
                else
                    targetBlockPos = new BlockPos(BlockUtils.getUnitZ() * -1 * 3 + mc.thePlayer.posX, mc.thePlayer.posY,
                            BlockUtils.getUnitX() * 3 + mc.thePlayer.posZ);
                resetKeybindState();
                Utils.addCustomLog("target block : " + targetBlockPos);
                while ((Math.floor(mc.thePlayer.posX) != targetBlockPos.getX() || Math.floor(mc.thePlayer.posZ) != targetBlockPos.getZ()) && enabled || !Utils.isInCenterOfBlockSideways()) {
                    setKeyBindState(keyBindShift, true);
                    setKeyBindState(keybindA, goLeft);
                    setKeyBindState(keybindD, !goLeft);
                    Thread.sleep(1);
                }

                resetKeybindState();
                Thread.sleep(50);
                KeyBinding.setKeyBindState(keyBindShift, true);
                AOTE();
                ExecuteRunnable(placePrismapumpAndWater);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Runnable placePrismapumpAndWater = () -> {
        try {

            if(!enabled)
                 return;
            if(shouldEndPlacingWater()){
                resetKeybindState();
                Utils.addCustomLog("Place water completed");
                CaneBuilder.switchToNextProcess(FillTrench.this);
                return;
            }

            mc.thePlayer.inventory.currentItem = 3;
            AngleUtils.hardRotate(playerYaw);
            AngleUtils.smoothRotatePitchTo(70, 1.2f);
            onTick(keybindUseItem);
            Thread.sleep(500);
            mc.thePlayer.inventory.currentItem = 4;
            AngleUtils.smoothRotateTo(playerYaw + (goLeft? -13 : 13 ), 1.2f);
            AngleUtils.smoothRotatePitchTo(42, 1.2f);
            Thread.sleep(1000);
            onTick(keybindUseItem);
            Thread.sleep(500);
            mc.thePlayer.inventory.currentItem = 5;
            AngleUtils.hardRotate(playerYaw);
            AngleUtils.smoothRotatePitchTo(70, 1.2f);
            Thread.sleep(1000);
            setKeyBindState(keybindAttack, true);
            while(!BlockUtils.isWalkable(BlockUtils.getBlockAround(0, 1, -1)))
                threadSleep(1);
            setKeyBindState(keybindAttack, false);
            mc.thePlayer.inventory.currentItem = 3;
            if(shouldEndPlacingWater()){
                resetKeybindState();
                Utils.addCustomLog("Place water completed");
                CaneBuilder.switchToNextProcess(FillTrench.this);
                return;
            }
            ExecuteRunnable(goToNextWaterTrench);


        }catch (Exception e){
            e.printStackTrace();
        }
    };
    boolean onRightSideOfFarm(){
        for (int i = 0; i < 10; i++) {
            if (BlockUtils.isWalkable(BlockUtils.getBlockAround(i, -1, -1))) {
                return true;
            }
        }
        return false;
    }
    boolean shouldEndPlacingWater(){
        if(onRightSideOfFarm()) {
            Utils.addCustomLog(BlockUtils.getBlockAround(-3, 1, -1).toString() + " " + "On Right side of farm");
            if((BlockUtils.getBlockAround(-3, 1, -1).equals(Blocks.water) || BlockUtils.getBlockAround(-3, 1, -1).equals(Blocks.flowing_water))
                    && !BlockUtils.isWalkable(BlockUtils.getBlockAround(-4, 1, -1))) {
                return BlockUtils.getBlockAround(3, 1, -1).equals(Blocks.air) && BlockUtils.getBlockAround(2, 1, -1).equals(Blocks.air) && BlockUtils.getBlockAround(4, 1, -1).equals(Blocks.air);
            }
        } else {
            Utils.addCustomLog(BlockUtils.getBlockAround(3, 1, -1).toString() + " " + "On left side of farm");
            if((BlockUtils.getBlockAround(3, 1, -1).equals(Blocks.water) || BlockUtils.getBlockAround(3, 1, -1).equals(Blocks.flowing_water))
                    && !BlockUtils.isWalkable(BlockUtils.getBlockAround(4, 1, -1))) {
                return BlockUtils.getBlockAround(-3, 1, -1).equals(Blocks.air) && BlockUtils.getBlockAround(-4, 1, -1).equals(Blocks.air) && BlockUtils.getBlockAround(-2, 1, -1).equals(Blocks.air);
            }
        }
        return false;

    }

}
