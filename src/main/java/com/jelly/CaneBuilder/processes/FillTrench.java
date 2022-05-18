package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.BlockUtils;
import com.jelly.CaneBuilder.utils.Clock;
import com.jelly.CaneBuilder.utils.Utils;
import static com.jelly.CaneBuilder.KeyBindHelper.*;
import net.minecraft.init.Blocks;

public class FillTrench extends ProcessModule {
    enum State {
        START,
        PLACE_PUMP,
        PLACE_WATER,
        BREAK_PUMP,
        WALK_NEXT
    }

    boolean aote;
    float pitch;
    State currentState;
    Clock rotateCooldown = new Clock();
    boolean done = false;
    Clock wait = new Clock();

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
                mc.thePlayer.inventory.currentItem = 3;
                setKeyBindState(keybindUseItem, false);
            }
            return;
        }

        if (done) {
            resetKeybindState();
            if (wait.passed()) {
                Utils.addCustomMessage("Fill trench completed!");
                CaneBuilder.switchToNextProcess(this);
            }
            return;
        }

        if ((BlockUtils.getUnitX() != 0 && Math.floor(mc.thePlayer.posZ) == BuilderState.corner1.getZ()) ||
          BlockUtils.getUnitZ() != 0 && Math.floor(mc.thePlayer.posX) == BuilderState.corner1.getX()) {
            resetKeybindState();
            if (!done) {
                done = true;
                Utils.addCustomLog("Waiting 10 seconds for water flow");
                wait.schedule(10000);
            }
            return;
        }

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

            case PLACE_PUMP:
                if (BlockUtils.getBlockAround(0, 1, -1).equals(Blocks.air)) {
                    mc.thePlayer.inventory.currentItem = 3;
                    onTick(keybindUseItem);
                } else if (BlockUtils.getBlockAround(0, 1, -1).equals(Blocks.prismarine)) {
                    resetKeybindState();
                    rotation.easeTo(AngleUtils.getClosest() - 13, 42, 500);
                    currentState = State.PLACE_WATER;
                    rotateCooldown.schedule(2000);
                }
                return;

            case PLACE_WATER:
                if (BlockUtils.getBlockAround(0, 3, -1).equals(Blocks.water)) {
                    resetKeybindState();
                    rotation.easeTo(AngleUtils.getClosest(), 70, 500);
                    mc.thePlayer.inventory.currentItem = 5;
                    currentState = State.BREAK_PUMP;
                } else {
                    Utils.addCustomLog("Placing water");
                    mc.thePlayer.inventory.currentItem = 4;
                    if(rotation.completed && rotateCooldown.passed())
                    setKeyBindState(keybindUseItem, true);
                }
                return;

            case BREAK_PUMP:
                if (BlockUtils.getBlockAround(0, 1, -1).equals(Blocks.prismarine)) {
                    mc.thePlayer.inventory.currentItem = 5;
                    setKeyBindState(keybindAttack, true);
                } else if (BlockUtils.getBlockAround(0, 1, -1).equals(Blocks.water) || BlockUtils.getBlockAround(0, 1, -1).equals(Blocks.flowing_water)) {
                    resetKeybindState();
                    mc.thePlayer.inventory.currentItem = 4;
                    currentState = State.WALK_NEXT;
                } else {
                    resetKeybindState();
                    Utils.addCustomLog("Waiting for water to flow backwards");
                }
                return;

            case WALK_NEXT:
                double rightVector = BlockUtils.getUnitX() * -1 * (BuilderState.corner2.getZ() - mc.thePlayer.posZ) + BlockUtils.getUnitZ() * -1 * (mc.thePlayer.posX - BuilderState.corner2.getX());
                if ((BlockUtils.getBlockAround(0, 1, -1).equals(Blocks.air) || BlockUtils.getBlockAround(0, 1, -1).equals(Blocks.prismarine)) &&
                  BlockUtils.getBlockAround((rightVector > 0 ? -1 : 1), 1, -1).equals(Blocks.dirt) &&
                  BlockUtils.getBlockAround((rightVector > 0 ? 1 : -1), 1, -1).equals(Blocks.dirt)) {
                    // Reached next row to dig
                    currentState = State.PLACE_PUMP;
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
        done = false;
        currentState = State.START;
        aote = false;
        rotation.easeTo(AngleUtils.getClosest(), 70, 1000);
    }

    @Override
    public void onDisable() {

    }
}
