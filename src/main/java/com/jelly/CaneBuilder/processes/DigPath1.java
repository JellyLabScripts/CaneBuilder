package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.BlockUtils;
import com.jelly.CaneBuilder.utils.Clock;
import com.jelly.CaneBuilder.utils.Utils;

import static com.jelly.CaneBuilder.KeyBindHelper.*;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public class DigPath1 extends ProcessModule {
    enum State {
        TELEPORTING,
        START,
        DROP_DIG,
        DIG_SIDE,
        WALK_BACK,
        MAIN_OUTER,
        PREP_INNER,
        MAIN_INNER,
        WALK_BACK_MAIN
    }

    State currentState;
    boolean aote = false;
    float pitch;
    int current = 0;
    BlockPos lastBroken;
    Clock teleportWait = new Clock();

    @Override
    public void onTick() {
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
                rotation.easeTo(AngleUtils.get360RotationYaw(), pitch, 500);
                mc.thePlayer.inventory.currentItem = 2;
                setKeyBindState(keybindUseItem, false);
            }
            return;
        }

        if (currentState == State.TELEPORTING) {
            if (Utils.getLocation() == Utils.location.ISLAND && teleportWait.passed()) {
                resetKeybindState();
                currentState = State.START;
                rotation.easeTo(AngleUtils.parallelToC2(), 60, 1000);
            }
            return;
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
                    currentState = State.DROP_DIG;
                    rotation.easeTo(AngleUtils.get360RotationYaw(), 89, 500);
                    mc.thePlayer.inventory.currentItem = 6;
                    resetKeybindState();
                    aote = true;
                    pitch = mc.thePlayer.rotationPitch;
                }
                return;

            case DROP_DIG:
                if (!BlockUtils.getBlockAround(0, 1, -1).equals(Blocks.air)) {
                    if (mc.objectMouseOver.getBlockPos().equals(BlockUtils.getBlockPosAround(0, 1, -1))) {
                        onTick(keybindAttack);
                        return;
                    } else {
                        rotation.easeTo(AngleUtils.get360RotationYaw(), 60, 500);
                    }
                } else if (!BlockUtils.getBlockAround(0, 2, -1).equals(Blocks.air)) {
                    if (mc.objectMouseOver.getBlockPos().equals(BlockUtils.getBlockPosAround(0, 2, -1))) {
                        onTick(keybindAttack);
                        return;
                    } else {
                        rotation.easeTo(AngleUtils.get360RotationYaw(), 50, 500);
                    }
                } else if (!BlockUtils.getBlockAround(0, 3, -1).equals(Blocks.air)) {
                    if (mc.objectMouseOver.getBlockPos().equals(BlockUtils.getBlockPosAround(0, 3, -1))) {
                        onTick(keybindAttack);
                        return;
                    } else {
                        rotation.easeTo(AngleUtils.get360RotationYaw(), 30, 500);
                    }
                } else if (!BlockUtils.getBlockAround(0, 4, -1).equals(Blocks.air)) {
                    if (mc.objectMouseOver.getBlockPos().equals(BlockUtils.getBlockPosAround(0, 4, -1))) {
                        onTick(keybindAttack);
                        return;
                    } else {
                        rotation.easeTo(AngleUtils.get360RotationYaw(), 25, 500);
                    }
                } else {
                    resetKeybindState();
                    rotation.easeTo(AngleUtils.getClosest(), 30, 500);
                    currentState = State.DIG_SIDE;
                }
                return;

            case DIG_SIDE:
                if (BlockUtils.getBlockAround(0, 3, 0).equals(Blocks.air) &&
                  BlockUtils.getBlockAround(0, 2, 0).equals(Blocks.dirt) &&
                  BlockUtils.getBlockAround(0, 1, 0).equals(Blocks.dirt) &&
                  (int) mc.thePlayer.posY == BuilderState.corner1.getY() + 2) {
                    resetKeybindState();
                    rotation.easeTo(AngleUtils.parallelToC1(), 30, 600);
                    currentState = State.WALK_BACK;
                    return;
                }

                boolean shouldDig = mc.objectMouseOver != null && BuilderState.corner1.getY() + 2 == mc.objectMouseOver.getBlockPos().getY() &&
                  (BlockUtils.getBlockAroundFrom(mc.objectMouseOver.getBlockPos(), 0, 2, 0).equals(Blocks.dirt) || BlockUtils.getBlockAroundFrom(mc.objectMouseOver.getBlockPos(), 0, 3, 0).equals(Blocks.dirt)) &&
                  (lastBroken == null || !lastBroken.equals(mc.objectMouseOver.getBlockPos()) && mc.thePlayer.posY == mc.objectMouseOver.getBlockPos().getY());

                updateKeys(true, false, false, false, false, false, false);
                if (shouldDig) {
                    Utils.addCustomLog("Ticking for: " + mc.objectMouseOver.getBlockPos());
                    lastBroken = mc.objectMouseOver.getBlockPos();
                    onTick(keybindAttack);
                }
                return;

            case WALK_BACK:
                if (BlockUtils.getBlockAround(0, 3, 0).equals(Blocks.air) &&
                  BlockUtils.getBlockAround(0, 2, 0).equals(Blocks.dirt) &&
                  BlockUtils.getBlockAround(0, 1, 0).equals(Blocks.dirt) &&
                  (int) mc.thePlayer.posY == BuilderState.corner1.getY() + 2) {
                    if (current == 1) {
                        resetKeybindState();
                        CaneBuilder.switchToNextProcess(this);
                        Utils.addCustomLog("Completed dig path 1");
                        return;
                    }
                    resetKeybindState();
                    currentState = State.MAIN_OUTER;
                    rotation.easeTo(AngleUtils.perpendicularToC2(), 89, 600);
                    mc.thePlayer.inventory.currentItem = 6;
                    aote = true;
                    pitch = mc.thePlayer.rotationPitch;
                    return;
                }

                updateKeys(true, false, false, false, false);
                return;

            case MAIN_OUTER:
                if (BlockUtils.getBlockAround(0, 2, 0).equals(Blocks.air) &&
                  BlockUtils.getBlockAround(0, 1, 0).equals(Blocks.dirt) &&
                  (int) mc.thePlayer.posY == BuilderState.corner1.getY() + 2) {
                    resetKeybindState();
                    rotation.easeTo(AngleUtils.parallelToC2(), 89, 600);
                    currentState = State.PREP_INNER;
                    mc.thePlayer.inventory.currentItem = 6;
                    aote = true;
                    pitch = mc.thePlayer.rotationPitch;
                    return;
                }

                shouldDig = mc.objectMouseOver != null && BuilderState.corner1.getY() + 2 == mc.objectMouseOver.getBlockPos().getY() &&
                  (BlockUtils.getBlockAroundFrom(mc.objectMouseOver.getBlockPos(), 0, 1, 0).equals(Blocks.dirt) || BlockUtils.getBlockAroundFrom(mc.objectMouseOver.getBlockPos(), 0, 2, 0).equals(Blocks.dirt)) &&
                  (lastBroken == null || !lastBroken.equals(mc.objectMouseOver.getBlockPos()) && mc.thePlayer.posY == mc.objectMouseOver.getBlockPos().getY());

                updateKeys(true, false, false, false, false);
                if (shouldDig) {
                    Utils.addCustomLog("Ticking for: " + mc.objectMouseOver.getBlockPos());
                    lastBroken = mc.objectMouseOver.getBlockPos();
                    onTick(keybindAttack);
                }
                return;

            case PREP_INNER:
                if (BlockUtils.getBlockAround(0, 0, 0).equals(Blocks.air) &&
                  BlockUtils.getBlockAround(0, -1, 0).equals(Blocks.air) &&
                  BlockUtils.getBlockAround(0, -2, 0).equals(Blocks.dirt) &&
                  (int) mc.thePlayer.posY == BuilderState.corner1.getY() + 2) {
                    resetKeybindState();
                    rotation.easeTo(AngleUtils.perpendicularToC1(), 89, 600);
                    currentState = State.MAIN_INNER;
                    mc.thePlayer.inventory.currentItem = 6;
                    aote = true;
                    pitch = mc.thePlayer.rotationPitch;
                    return;
                }

                shouldDig = mc.objectMouseOver != null && mc.thePlayer.posY == mc.objectMouseOver.getBlockPos().getY() &&
                  BlockUtils.getBlockAroundFrom(mc.objectMouseOver.getBlockPos(), 0, -1, 0).equals(Blocks.air) &&
                  !BlockUtils.getBlockAroundFrom(mc.objectMouseOver.getBlockPos(), 0, -2, 0).equals(Blocks.air);

                updateKeys(true, false, false, false, shouldDig, false, true);
                return;

            case MAIN_INNER:
                if (BlockUtils.getBlockAround(0, 3, 0).equals(Blocks.air) &&
                  BlockUtils.getBlockAround(0, 2, 0).equals(Blocks.air) &&
                  BlockUtils.getBlockAround(0, 1, 0).equals(Blocks.dirt) &&
                  (int) mc.thePlayer.posY == BuilderState.corner1.getY() + 2) {
                    resetKeybindState();
                    rotation.easeTo(AngleUtils.perpendicularToC2(), 89, 600);
                    currentState = State.WALK_BACK_MAIN;
                    mc.thePlayer.inventory.currentItem = 6;
                    aote = true;
                    pitch = mc.thePlayer.rotationPitch;
                    return;
                }

                shouldDig = mc.objectMouseOver != null && BuilderState.corner1.getY() + 2 == mc.objectMouseOver.getBlockPos().getY() &&
                  (!BlockUtils.getBlockAroundFrom(mc.objectMouseOver.getBlockPos(), 0, 1, 0).equals(Blocks.air) ||
                    (BlockUtils.getBlockAroundFrom(mc.objectMouseOver.getBlockPos(), 0, 1, 0).equals(Blocks.air) &&
                      !BlockUtils.getBlockAroundFrom(mc.objectMouseOver.getBlockPos(), 0, 2, 0).equals(Blocks.air))) &&
                  (lastBroken == null || !lastBroken.equals(mc.objectMouseOver.getBlockPos()) && mc.thePlayer.posY == mc.objectMouseOver.getBlockPos().getY());

                updateKeys(true, false, false, false, false);
                if (shouldDig) {
                    Utils.addCustomLog("Ticking for: " + mc.objectMouseOver.getBlockPos());
                    lastBroken = mc.objectMouseOver.getBlockPos();
                    onTick(keybindAttack);
                }
                return;

            case WALK_BACK_MAIN:
                if (BlockUtils.getBlockAround(0, 2, 0).equals(Blocks.air) &&
                  BlockUtils.getBlockAround(0, 1, 0).equals(Blocks.dirt) &&
                  (int) mc.thePlayer.posY == BuilderState.corner1.getY() + 2) {
                    resetKeybindState();
                    rotation.easeTo(AngleUtils.parallelToC2(), 89, 600);
                    currentState = State.DIG_SIDE;
                    current = 1;
                    mc.thePlayer.inventory.currentItem = 6;
                    aote = true;
                    pitch = mc.thePlayer.rotationPitch;
                    return;
                }

                updateKeys(true, false, false, false, false);
                return;
        }
    }

    @Override
    public void onEnable() {
        resetKeybindState();
        mc.thePlayer.sendChatMessage("/hub");
        currentState = State.TELEPORTING;
        aote = false;
        current = 0;
        lastBroken = null;
        teleportWait.schedule(2000);
    }

    @Override
    public void onDisable() {

    }
}
