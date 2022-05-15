package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.BlockUtils;
import com.jelly.CaneBuilder.utils.Utils;

import static com.jelly.CaneBuilder.KeyBindHelper.*;

import net.minecraft.init.Blocks;

public class DigPath2 extends ProcessModule {
    enum State {
        EDGE_RIGHT,
        EDGE_LEFT,
        RIGHT,
        LEFT,
        FORWARDS,
        NONE
    }

    State currentState;
    boolean aote = false;
    float pitch;
    boolean switching = false;

    @Override
    public void onTick() {
        if (rotation.rotating) {
            resetKeybindState();
            return;
        }

        if (aote) {
            mc.thePlayer.inventory.currentItem = 6;
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

        updateState();
        mc.thePlayer.inventory.currentItem = 2;
        switch (currentState) {
            case LEFT:
                updateKeys(false, false, true, false, true, false, false);
                return;
            case RIGHT:
                updateKeys(false, false, false, true, true, false, false);
                return;
            case FORWARDS:
                updateKeys(true, false, false, false, false, false, true);
                return;
            default:
                resetKeybindState();
        }
    }

    @Override
    public void onEnable() {
        resetKeybindState();
        currentState = State.NONE;
        aote = false;
        rotation.easeTo(AngleUtils.parallelToC2(), 11.5f, 1000);
        switching = false;
    }

    @Override
    public void onDisable() {

    }

    private void updateState() {
        if (currentState == State.FORWARDS && !BlockUtils.getBlockAround(0, 1, 0).equals(Blocks.air)) {
            currentState = State.NONE;
            Utils.addCustomLog("Completed dig path");
            resetKeybindState();
            CaneBuilder.switchToNextProcess(this);
            return;
        }

        if (currentState == State.FORWARDS && (!BlockUtils.getBlockAround(0, 3, 0).equals(Blocks.air) || !BlockUtils.getBlockAround(0, 2, 0).equals(Blocks.air))) {
            currentState = State.FORWARDS;
            return;
        }

        if (!BlockUtils.getBlockAround(-1, 0, 0).equals(Blocks.air) && !BlockUtils.getBlockAround(1, 0, 0).equals(Blocks.air)) {
            currentState = State.FORWARDS;
        } else if (!BlockUtils.getBlockAround(-1, 0, 0).equals(Blocks.air)) {
            if (!BlockUtils.getBlockAround(1, -1, 0).equals(Blocks.air)) {
                if (currentState == State.EDGE_RIGHT || currentState == State.RIGHT) {
                    currentState = State.RIGHT;
                } else if (currentState != State.LEFT && !switching) {
                    rotation.easeTo(AngleUtils.parallelToC2(), 89, 600);
                    currentState = State.EDGE_RIGHT;
                    mc.thePlayer.inventory.currentItem = 6;
                    aote = true;
                    pitch = mc.thePlayer.rotationPitch;
                } else {
                    switching = true;
                    currentState = State.FORWARDS;
                }
            } else {
                currentState = State.FORWARDS;
                switching = false;
            }
        } else if (!BlockUtils.getBlockAround(1, 0, 0).equals(Blocks.air)) {
            if (!BlockUtils.getBlockAround(-1, -1, 0).equals(Blocks.air)) {
                if (currentState == State.EDGE_LEFT || currentState == State.LEFT) {
                    currentState = State.LEFT;
                } else if (currentState != State.RIGHT && !switching) {
                    rotation.easeTo(AngleUtils.parallelToC2(), 89, 600);
                    currentState = State.EDGE_LEFT;
                    mc.thePlayer.inventory.currentItem = 6;
                    aote = true;
                    pitch = mc.thePlayer.rotationPitch;
                } else {
                    switching = true;
                    currentState = State.FORWARDS;
                }
            } else {
                currentState = State.FORWARDS;
                switching = false;
            }
        } else {
            if (currentState != State.LEFT && currentState != State.RIGHT) {
                currentState = State.RIGHT;
            }
        }
    }
}
