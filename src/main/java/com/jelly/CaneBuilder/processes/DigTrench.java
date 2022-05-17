package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.BlockUtils;
import com.jelly.CaneBuilder.utils.Clock;
import com.jelly.CaneBuilder.utils.Utils;
import static com.jelly.CaneBuilder.KeyBindHelper.*;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public class DigTrench extends ProcessModule {
    enum State {
        START,
        DROP_DIG,
        FULL_DIG,
        JUMP_OUT,
        WALK_NEXT
    }

    boolean aote;
    float pitch;
    State currentState;
    Clock jumpCooldown = new Clock();
    BlockPos lastBroken;

    @Override
    public void onTick() {
        if (rotation.rotating) {
            resetKeybindState();
            return;
        }

        if (aote) {
            resetKeybindState();
            // KeyBinding.onTick(keybindUseItem);
            setKeyBindState(keybindUseItem, true);
            if (Math.abs(mc.thePlayer.posX % 1) == 0.5 && Math.abs(mc.thePlayer.posZ % 1) == 0.5) {
                aote = false;
                rotation.reset();
                rotation.easeTo(AngleUtils.get360RotationYaw(), pitch, 500);
                setKeyBindState(keybindUseItem, false);
            }
            return;
        }

        if (BuilderState.onParallel() == BuilderState.corner2.getParallel()) {
            resetKeybindState();
            Utils.addCustomLog("Dig trench completed");
            CaneBuilder.switchToNextProcess(DigTrench.this);
        }

        mc.thePlayer.inventory.currentItem = 2;

        switch (currentState) {
            case START:
                if (BlockUtils.getBlockAround(1, 0, -1).equals(Blocks.air)) {
                    updateKeys(false, false, true, false, false, false, true);
                } else if (BlockUtils.getBlockAround(-1, 0, -1).equals(Blocks.air)) {
                    updateKeys(false, false, false, true, false, false, true);
                } else if (BlockUtils.getBlockAround(0, -1, -1).equals(Blocks.air)) {
                    updateKeys(true, false, false, false, false, false, true);
                } else {
                    currentState = State.WALK_NEXT;
                }
                return;

            case DROP_DIG:
                if (!BlockUtils.getBlockAround(0, 1, -1).equals(Blocks.air)) {
                    if (mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos().equals(BlockUtils.getBlockPosAround(0, 1, -1))) {
                        onTick(keybindAttack);
                        return;
                    } else {
                        rotation.easeTo(AngleUtils.get360RotationYaw(), 60, 500);
                    }
                } else if (!BlockUtils.getBlockAround(0, 2, -1).equals(Blocks.air)) {
                    if (mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos().equals(BlockUtils.getBlockPosAround(0, 2, -1))) {
                        onTick(keybindAttack);
                        return;
                    } else {
                        rotation.easeTo(AngleUtils.get360RotationYaw(), 50, 500);
                    }
                } else if (!BlockUtils.getBlockAround(0, 3, -1).equals(Blocks.air)) {
                    if (mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos().equals(BlockUtils.getBlockPosAround(0, 3, -1))) {
                        onTick(keybindAttack);
                        return;
                    } else {
                        rotation.easeTo(AngleUtils.get360RotationYaw(), 30, 500);
                    }
                } else if (!BlockUtils.getBlockAround(0, 4, -1).equals(Blocks.air)) {
                    if (mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos().equals(BlockUtils.getBlockPosAround(0, 4, -1))) {
                        onTick(keybindAttack);
                        return;
                    } else {
                        rotation.easeTo(AngleUtils.get360RotationYaw(), 25, 500);
                    }
                } else {
                    resetKeybindState();
                    rotation.easeTo(AngleUtils.getClosest(), 30, 500);
                    currentState = State.FULL_DIG;
                }
                return;

            case FULL_DIG:
                if (BlockUtils.getBlockAround(0, 3, 0).equals(Blocks.air) &&
                  BlockUtils.getBlockAround(0, 2, 0).equals(Blocks.dirt) &&
                  BlockUtils.getBlockAround(0, 1, 0).equals(Blocks.dirt) &&
                  (int) mc.thePlayer.posY == BuilderState.corner1.getY() + 1) {
                    resetKeybindState();
                    currentState = State.JUMP_OUT;
                    return;
                }

                boolean shouldDig = mc.objectMouseOver != null && BuilderState.corner1.getY() + 1 == mc.objectMouseOver.getBlockPos().getY() &&
                  (BlockUtils.getBlockAroundFrom(mc.objectMouseOver.getBlockPos(), 0, 2, 0).equals(Blocks.dirt) || BlockUtils.getBlockAroundFrom(mc.objectMouseOver.getBlockPos(), 0, 3, 0).equals(Blocks.dirt)) &&
                  (lastBroken == null || !lastBroken.equals(mc.objectMouseOver.getBlockPos()) && mc.thePlayer.posY == mc.objectMouseOver.getBlockPos().getY());

                updateKeys(true, false, false, false, false);
                if (shouldDig) {
                    Utils.addCustomLog("Ticking for: " + mc.objectMouseOver.getBlockPos());
                    lastBroken = mc.objectMouseOver.getBlockPos();
                    onTick(keybindAttack);
                }
                return;

            case JUMP_OUT:
                if (mc.thePlayer.posY == BuilderState.corner1.getY() + 2) {
                    KeyBinding.setKeyBindState(keyBindJump, false);
                    resetKeybindState();
                    Utils.addCustomLog("Landed, switching to next trench");
                    currentState = State.WALK_NEXT;
                    rotation.easeTo(AngleUtils.get360RotationYaw() + 180, mc.thePlayer.rotationPitch, 1000);
                    return;
                }
                updateKeys(true, false, false, false, false);
                KeyBinding.setKeyBindState(keyBindJump, jumpCooldown.passed());
                if (jumpCooldown.passed()) jumpCooldown.schedule(1000);
                return;

            case WALK_NEXT:
                double rightVector = BlockUtils.getUnitX() * (BuilderState.corner2.getZ() - mc.thePlayer.posZ) + BlockUtils.getUnitZ() * (mc.thePlayer.posX - BuilderState.corner2.getX());
                if (BlockUtils.getBlockAround(0, 1, -1).equals(Blocks.dirt) &&
                  BlockUtils.getBlockAround((rightVector > 0 ? -1 : 1), 1, -1).equals(Blocks.dirt) &&
                  BlockUtils.getBlockAround((rightVector > 0 ? -1 : 1) * 3, 1, -1).equals(Blocks.air) &&
                  BlockUtils.getBlockAround((rightVector > 0 ? 1 : -1), 1, -1).equals(Blocks.dirt)) {
                    // Reached next row to dig
                    currentState = State.DROP_DIG;
                    rotation.easeTo(AngleUtils.get360RotationYaw(), 89, 500);
                    mc.thePlayer.inventory.currentItem = 6;
                    resetKeybindState();
                    aote = true;
                    pitch = mc.thePlayer.rotationPitch;
                } else {
                    updateKeys(false, false, rightVector < 0, rightVector > 0, false, false, true);
                }
        }
    }

    @Override
    public void onEnable() {
        resetKeybindState();
        currentState = State.START;
        aote = false;
        lastBroken = null;
        rotation.easeTo(AngleUtils.getClosest(), 60, 1000);
    }

    @Override
    public void onDisable() {

    }
}
