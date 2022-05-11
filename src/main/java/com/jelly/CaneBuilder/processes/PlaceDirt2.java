package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.util.concurrent.TimeUnit;

public class PlaceDirt2 extends ProcessModule{
    @Override
    public void onTick() {
        mc.thePlayer.inventory.currentItem = 0;
        mc.thePlayer.rotationPitch = 82;
        double dx = Math.abs(mc.thePlayer.posX - mc.thePlayer.lastTickPosX);
        double dz = Math.abs(mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ);
        Block blockStandingOn = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)).getBlock();
        setKeyBindState(keybindUseItem, dx == 0f && dz == 0f && !Utils.isInCenterOfBlockForward());
        setKeyBindState(keybindS, true);
        setKeyBindState(keyBindShift, true);
        if ((int) mc.thePlayer.posX == CaneBuilder.corner2x && blockStandingOn != Blocks.air) {
            Utils.addCustomMessage("Second dirt layer done");
            CaneBuilder.switchToNextProcess(this);
        } else {
            AngleUtils.hardRotate(CaneBuilder.corner2x > CaneBuilder.corner1x ? 90 : 270);
        }

    }
}
