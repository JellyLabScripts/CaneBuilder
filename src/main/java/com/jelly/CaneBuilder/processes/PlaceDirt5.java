package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.features.Failsafe;
import com.jelly.CaneBuilder.handlers.MacroHandler;
import com.jelly.CaneBuilder.handlers.ThreadHandler;
import com.jelly.CaneBuilder.utils.*;

import static com.jelly.CaneBuilder.handlers.KeyBindHandler.*;

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
        PLACING,
        EFFECT
    }
    boolean setEffect = false;
    State currentState;

    @Override
    public void onTick() {

        if(ScoreboardUtils.getLocation() != ScoreboardUtils.location.ISLAND){
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
            if (Math.abs(mc.thePlayer.posX % 1) == 0.5 && Math.abs(mc.thePlayer.posZ % 1) == 0.5 && (int)mc.thePlayer.posY <= BuilderState.corner1.getY() + 3) {
                aote = false;
                rotation.reset();
                rotation.easeTo(AngleUtils.get360RotationYaw(), onSecondLayer ? 82 : 89, 500);
                mc.thePlayer.inventory.currentItem = 0;
                setKeyBindState(keybindUseItem, false);
            }
            return;
        }

        if (currentState == State.TELEPORTING) {
            if (ScoreboardUtils.getLocation() == ScoreboardUtils.location.ISLAND && teleportWait.passed()) {
                resetKeybindState();
                currentState = State.EFFECT;
            }
            return;
        }

        if(currentState == State.EFFECT){
            if(!setEffect){
                ThreadHandler.executeThread(new Thread(() -> {
                    ProcessUtils.disableJumpPotion();
                    currentState = State.PLACING;
                    rotation.easeTo(AngleUtils.parallelToC1(), 89, 1000);
                    mc.thePlayer.inventory.currentItem = 6;
                    aote = true;
                }));
                setEffect = true;
            }
            return;
        }

        if (!onSecondLayer) {
            mc.thePlayer.inventory.currentItem = 7;
            if (mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && BlockUtils.isWalkable(BlockUtils.getBlockAround(0, 1, -1))
                    && BlockUtils.isWalkable(BlockUtils.getBlockAround(1, 0, -1)) && BlockUtils.isWalkable(BlockUtils.getBlockAround(-1, 0, -1)) && BlockUtils.isWalkable(BlockUtils.getBlockAround(0, -1, -1))
                    && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1.2) {
                LogUtils.addCustomLog("On third layer");
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
                boolean shouldPlace = mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && mc.objectMouseOver.sideHit == EnumFacing.UP && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() > 1.8;
                setKeyBindState(keyBindJump, false);
                updateKeys(false, false, false, false, false, shouldPlace, true);
            }
        } else {

            if((int)mc.thePlayer.posY > BuilderState.corner1.getY() + 3){
                MacroHandler.disableScript("Wrong y level detected. Probably lag or you set the wrong y level");
                return;

            }
            if (setTP) {
                if (tpSet.passed()) {
                    mc.thePlayer.sendChatMessage("/setspawn");
                    setTP = false;
                }
                resetKeybindState();
                return;
            }
            mc.thePlayer.inventory.currentItem = 0;
            boolean shouldPlace = mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1 && mc.objectMouseOver.sideHit != EnumFacing.UP;
            boolean hasPlacedEnd = mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && mc.thePlayer.posY - mc.objectMouseOver.getBlockPos().getY() <= 1 && BuilderState.lookingAtParallel() == BuilderState.corner2.getParallel();

            if (hasPlacedEnd) {
                LogUtils.addCustomLog("Done third line");
                resetKeybindState();
                MacroHandler.switchToNextProcess(this);
            }

            updateKeys(false, true, false, false, false, shouldPlace, true);
        }
    }

    @Override
    public void onEnable() {
        Failsafe.pauseOnLeave = false;
        setTP = false;
        mc.thePlayer.inventory.currentItem = 0;
        onSecondLayer = (int)mc.thePlayer.posY - BuilderState.corner1.getY() == 3;
        mc.thePlayer.inventory.currentItem = 0;
        mc.thePlayer.sendChatMessage("/hub");
        currentState = State.TELEPORTING;
        teleportWait.schedule(2000);
        setEffect = false;

    }

    @Override
    public void onDisable() {
        onSecondLayer = false;
    }
}
