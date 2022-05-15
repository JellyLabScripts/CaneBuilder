package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.Clock;
import com.jelly.CaneBuilder.utils.Utils;
import static com.jelly.CaneBuilder.KeyBindHelper.*;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class PlaceDirt3 extends ProcessModule {
    boolean onSecondLayer = false;
    Clock jumpCooldown = new Clock();

    @Override
    public void onTick() {
        if (rotation.rotating) {
            resetKeybindState();
            return;
        }

        if (!onSecondLayer) {
            mc.thePlayer.inventory.currentItem = 7;
            if (mc.objectMouseOver != null && BuilderState.corner1.getY() + 1 == mc.objectMouseOver.getBlockPos().getY() && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1.2) {
                onSecondLayer = true;
                rotation.easeTo(AngleUtils.parallelToC2(), 82, 600);
            } else if (jumpCooldown.passed()) {
                resetKeybindState();
                setKeyBindState(keyBindJump, true);
                jumpCooldown.schedule(1000);
            } else {
                boolean shouldPlace = mc.objectMouseOver != null && BuilderState.corner1.getY() == mc.objectMouseOver.getBlockPos().getY() && mc.objectMouseOver.sideHit == EnumFacing.UP && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() > 1.8;
                setKeyBindState(keyBindJump, false);
                updateKeys(false, false, false, false, false, shouldPlace, true);
            }
        } else {
            mc.thePlayer.inventory.currentItem = 1;
            boolean shouldPlace = mc.objectMouseOver != null && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1 && mc.objectMouseOver.sideHit != EnumFacing.UP;
            boolean hasPlacedEnd = mc.objectMouseOver != null && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1 && BuilderState.lookingAtParallel() == BuilderState.corner1.getParallel();

            if (hasPlacedEnd) {
                resetKeybindState();
                CaneBuilder.switchToNextProcess(this);
            }

            updateKeys(false, true, false, false, false, shouldPlace, true);
        }
    }

    @Override
    public void onEnable() {
        Block blockStandingOn = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)).getBlock();
        Block blockBelowStandingOn = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 2, mc.thePlayer.posZ)).getBlock();
        onSecondLayer = blockStandingOn.equals(Blocks.dirt) && blockBelowStandingOn.equals(Blocks.dirt);
        rotation.easeTo(AngleUtils.parallelToC2(), onSecondLayer ? 82 : 89, 1000);
    }

    @Override
    public void onDisable(){
        onSecondLayer = false;
    }
}
