package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.CaneBuilder;
import static com.jelly.CaneBuilder.KeyBindHelper.*;

import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.util.EnumFacing;


public class PlaceDirt1 extends ProcessModule {
    @Override
    public void onTick() {
        mc.thePlayer.inventory.currentItem = 1;

        if (rotation.rotating || Utils.getLocation() != Utils.location.ISLAND) {
            resetKeybindState();
            return;
        }

        boolean shouldPlace = mc.objectMouseOver != null && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1 && mc.objectMouseOver.sideHit != EnumFacing.UP;
        boolean hasPlacedEnd = mc.objectMouseOver != null && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1 && BuilderState.lookingAtParallel() == BuilderState.corner2.getParallel();

        if (hasPlacedEnd) {
            resetKeybindState();
            CaneBuilder.switchToNextProcess(this);
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
