package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.BlockUtils;
import com.jelly.CaneBuilder.utils.Clock;
import com.jelly.CaneBuilder.utils.Utils;
import static com.jelly.CaneBuilder.KeyBindHelper.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class PlaceDirt5 extends ProcessModule {
    boolean onSecondLayer = false;
    boolean aote = false;
    float pitch;
    Clock jumpCooldown = new Clock();
    Clock placeCooldown = new Clock();
    Clock teleportWait = new Clock();
    Clock tpSet = new Clock();
    boolean setTP = false;
    enum State {
        TELEPORTING,
        PLACING
    }
    State currentState;

    @Override
    public void onTick() {

        if(Utils.getLocation() != Utils.location.ISLAND){
            resetKeybindState();
            return;
        }

        if (rotation.rotating) {
            resetKeybindState();
            return;
        }

        if (aote) {
            resetKeybindState();
            setKeyBindState(keybindUseItem, true);
            if (Math.abs(mc.thePlayer.posX % 1) == 0.5 && Math.abs(mc.thePlayer.posZ % 1) == 0.5) {
                aote = false;
                rotation.reset();
                rotation.easeTo(AngleUtils.get360RotationYaw(), onSecondLayer ? 82 : 89, 500);
                mc.thePlayer.inventory.currentItem = 1;
                setKeyBindState(keybindUseItem, false);
            }
            return;
        }

        if (currentState == State.TELEPORTING) {
            if (Utils.getLocation() == Utils.location.ISLAND && teleportWait.passed()) {
                resetKeybindState();
                currentState = State.PLACING;
                rotation.easeTo(AngleUtils.parallelToC1(), 89, 1000);
                mc.thePlayer.inventory.currentItem = 6;
                aote = true;
            }
            return;
        }


        if (!onSecondLayer) {
            mc.thePlayer.inventory.currentItem = 7;
            if (mc.objectMouseOver != null && BlockUtils.isWalkable(BlockUtils.getBlockAround(0, 1, -1))
                    && BlockUtils.isWalkable(BlockUtils.getBlockAround(1, 0, -1)) && BlockUtils.isWalkable(BlockUtils.getBlockAround(-1, 0, -1)) && BlockUtils.isWalkable(BlockUtils.getBlockAround(0, -1, -1))
                    && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1.2) {
                Utils.addCustomLog("On third layer");
                onSecondLayer = true;
                setTP = true;
                tpSet.schedule(2000);
                rotation.easeTo(AngleUtils.parallelToC1(), 82, 600);
            } else if (jumpCooldown.passed()) {
                resetKeybindState();
                setKeyBindState(keyBindJump, true);
                jumpCooldown.schedule(1000);
                placeCooldown.schedule(250);
            } else {
                boolean shouldPlace = mc.objectMouseOver != null && mc.objectMouseOver.sideHit == EnumFacing.UP && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() > 1.8;
                setKeyBindState(keyBindJump, false);
                updateKeys(false, false, false, false, false, shouldPlace, true);
            }
        } else {

            if (setTP) {
                if (tpSet.passed()) {
                    mc.thePlayer.sendChatMessage("/setspawn");
                    setTP = false;
                }
                resetKeybindState();
                return;
            }
            mc.thePlayer.inventory.currentItem = 1;
            boolean shouldPlace = mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1 && mc.objectMouseOver.sideHit != EnumFacing.UP;
            boolean hasPlacedEnd = mc.objectMouseOver != null && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1 && BuilderState.lookingAtParallel() == BuilderState.corner2.getParallel();

            if (hasPlacedEnd) {
                Utils.addCustomLog("Done third line");
                resetKeybindState();
                CaneBuilder.switchToNextProcess(this);
            }

            updateKeys(false, true, false, false, false, shouldPlace, true);
        }
    }

    @Override
    public void onEnable() {
        setTP = false;
        mc.thePlayer.inventory.currentItem = 1;
        onSecondLayer = (int)mc.thePlayer.posY - BuilderState.corner1.getY() == 3;
        mc.thePlayer.inventory.currentItem = 1;
        mc.thePlayer.sendChatMessage("/hub");
        currentState = State.TELEPORTING;
        teleportWait.schedule(2000);
    }

    @Override
    public void onDisable() {
        onSecondLayer = false;
    }
}
