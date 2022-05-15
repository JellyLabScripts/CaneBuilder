package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.Utils;
import static com.jelly.CaneBuilder.KeyBindHelper.*;
import net.minecraft.util.EnumFacing;

public class PlaceDirt4 extends ProcessModule{
    @Override
    public void onTick() {
        mc.thePlayer.inventory.currentItem = 0;

        if (rotation.rotating) {
            resetKeybindState();
            return;
        }

        boolean shouldPlace = mc.objectMouseOver != null && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1 && mc.objectMouseOver.sideHit != EnumFacing.UP;
        boolean hasPlacedEnd = mc.objectMouseOver != null && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1 && BuilderState.lookingAtPerpendicular() == BuilderState.corner1.getPerpendicular();

        if (hasPlacedEnd) {
            mc.thePlayer.sendChatMessage("/setspawn");
            Utils.addCustomMessage("Second dirt layer done");
            resetKeybindState();
            CaneBuilder.switchToNextProcess(this);
        }

        updateKeys(false, true, false, false, false, shouldPlace, true);
    }

    @Override
    public void onEnable() {
        rotation.easeTo(AngleUtils.perpendicularToC2(), 82, 1000);
    }

    @Override
    public void onDisable() {

    }

}
