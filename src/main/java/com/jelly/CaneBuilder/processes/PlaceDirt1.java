package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.BuilderState;

import static com.jelly.CaneBuilder.handlers.KeyBindHandler.*;

import com.jelly.CaneBuilder.handlers.MacroHandler;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.BlockUtils;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;


public class PlaceDirt1 extends ProcessModule {
    @Override
    public void onTick() {
        mc.thePlayer.inventory.currentItem =
                BlockUtils.getXZBlockDistanceBetweenTwoBlock(
                        BlockUtils.getPlayerLoc(), new BlockPos(BuilderState.corner1.getX(), BuilderState.corner1.getY(), BuilderState.corner1.getZ())) < 2 ? 7 : 0;

        if (rotation.rotating) {
            resetKeybindState();
            return;
        }

        boolean shouldPlace = mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1 && mc.objectMouseOver.sideHit != EnumFacing.UP;
        boolean hasPlacedEnd = mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1 && BuilderState.lookingAtParallel() == BuilderState.corner2.getParallel();

        if (hasPlacedEnd) {
            resetKeybindState();
            MacroHandler.switchToNextProcess(this);
        }

        updateKeys(false, true, false, false, false, shouldPlace, true);
    }

    @Override
    public void onEnable() {
        rotation.reset();
        rotation.easeTo(AngleUtils.parallelToC1(), 82, 1000);
    }

    @Override
    public void onDisable() {
    }
}
