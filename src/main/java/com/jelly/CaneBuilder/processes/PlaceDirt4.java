package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.handlers.MacroHandler;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.LogUtils;

import static com.jelly.CaneBuilder.handlers.KeyBindHandler.*;
import net.minecraft.util.EnumFacing;

public class PlaceDirt4 extends ProcessModule{
    @Override
    public void onTick() {
        mc.thePlayer.inventory.currentItem = 0;


        boolean shouldPlace = mc.objectMouseOver != null && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1 && mc.objectMouseOver.sideHit != EnumFacing.UP;
        boolean hasPlacedEnd = mc.objectMouseOver != null && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1 && BuilderState.lookingAtPerpendicular() == BuilderState.corner1.getPerpendicular();

        if (hasPlacedEnd) {
            mc.thePlayer.sendChatMessage("/setspawn");
            LogUtils.addCustomMessage("Second dirt layer complete!");
            resetKeybindState();
            MacroHandler.switchToNextProcess(this);
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
