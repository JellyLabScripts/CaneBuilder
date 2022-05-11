package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public class PlaceDirt1 extends ProcessModule {
    @Override
    public void onTick() {
        mc.thePlayer.inventory.currentItem = 1;
        mc.thePlayer.rotationPitch = 82;

        double dx = Math.abs(mc.thePlayer.posX - mc.thePlayer.lastTickPosX);
        double dz = Math.abs(mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ);
        Block blockStandingOn = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)).getBlock();

        setKeyBindState(keybindUseItem, dx == 0f && dz == 0f && !Utils.isInCenterOfBlockForward());
        setKeyBindState(keybindS, true);
        setKeyBindState(keyBindShift, true);

        if ((int) mc.thePlayer.posZ == CaneBuilder.corner2z &&  blockStandingOn != Blocks.air) {
            setKeyBindState(keybindUseItem, false);
            setKeyBindState(keybindS, false);
            setKeyBindState(keyBindShift, false);
            CaneBuilder.switchToNextProcess(this);
        } else {
            AngleUtils.hardRotate(CaneBuilder.corner2z > CaneBuilder.corner1z ? 180 : 0);
        }
    }

}
