package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.ThreadManager;
import com.jelly.CaneBuilder.utils.*;

import static com.jelly.CaneBuilder.KeyBindHelper.*;
import static com.jelly.CaneBuilder.utils.Utils.clickWindow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import scala.concurrent.Await;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class PlaceSC extends ProcessModule {
    boolean refillingSc;
    boolean canePlaceLag;
    boolean switching = false;
    boolean lagged = false;
    boolean pushedOff = false;

    BlockPos targetBlockPos = new BlockPos(10000, 10000, 10000);
    Clock lagCooldown = new Clock();
    State currentState;
    State lastState;


    enum State {
        START,
        RIGHT,
        LEFT,
        FORWARD,
        SWITCH,
        NONE
    }


    @Override
    public void onTick() {
        if (rotation.rotating || refillingSc) {
            resetKeybindState();
            return;
        }
        if (currentState == State.START)
            return;

        if (blockLagged() && !lagged) {
            Utils.addCustomLog("Detected not placed sugarcane");
            lagged = true;
            lagCooldown.schedule(700);
        }
        if (lagged) {
            if (lagCooldown.passed()) {
                targetBlockPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                lagged = false;
            }
            updateKeys(false, false, currentState == State.RIGHT, currentState == State.LEFT, false, true, false);
            return;
        }
        if (!Utils.hasSugarcaneInHotbar() || !Utils.hasSugarcaneInInv()) {
            refillingSc = true;
            updateKeys(false, false, false, false, false, false, false);
            ExecuteRunnable(RefillSc);
            return;
        }


        updateState();


        lastState = currentState;

        mc.thePlayer.rotationPitch = 50;
        mc.thePlayer.inventory.currentItem = Utils.getFirstHotbarSlotWithSugarcane() - 36;

        switch (currentState) {
            case LEFT:
                updateKeys(false, false, true, false, false, true, false);
                return;
            case RIGHT:
                updateKeys(false, false, false, true, false, true, false);
                return;
            case SWITCH:
                Utils.addCustomLog("Switching");
                updateKeys(false, false, false, false, false, true, false);
                if (!pushedOff) {
                    updateKeys(false, false, !BlockUtils.isWalkable(BlockUtils.getLeftBlock()), BlockUtils.isWalkable(BlockUtils.getLeftBlock()), false, true, false);
                }
                pushedOff = true;
                return;
            case FORWARD:
                if (BlockUtils.getBlockAround(0, 1, 0).equals(Blocks.dirt)) {
                    currentState = State.NONE;
                    Utils.addCustomLog("Completed sugarcane placement");
                    resetKeybindState();
                    CaneBuilder.switchToNextProcess(this);
                    return;
                }
                updateKeys(true, false, false, false, false, false, true);
                pushedOff = false;
                return;
            default:
                resetKeybindState();
        }
    }

    @Override
    public void onEnable() {
        canePlaceLag = false;
        refillingSc = false;
        lagged = false;
        switching = false;
        currentState = State.START;

        ExecuteRunnable(new Thread(() -> {
            try {
                //autosell dirt
                threadSleep(500);
                mc.thePlayer.inventory.currentItem = 8;
                threadSleep(100);
                onTick(keybindUseItem);
                threadSleep(800);
                Utils.clickWindow(mc.thePlayer.openContainer.windowId, 22, 0, 0);
                threadSleep(1000);
                while (Utils.getFirstSlotWithDirt() != -1 && enabled) {
                    Utils.clickWindow(mc.thePlayer.openContainer.windowId, 45 + Utils.getFirstSlotWithDirt(), 0, 0);
                    threadSleep(500);
                }
                threadSleep(500);
                mc.thePlayer.closeScreen();
                //clear hotbar & set rancher's boots
                Utils.addCustomLog("Setting Rancher's boot's speed");
                Thread.sleep(500);
                mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
                Thread.sleep(500);
                for(int i = 36; i < 44; i ++){
                    clickWindow(mc.thePlayer.openContainer.windowId, i, 0, 1);
                    Thread.sleep(500);
                }
                Thread.sleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 8, 0, 0);
                Thread.sleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 36, 0, 0);
                Thread.sleep(250);
                mc.thePlayer.closeScreen();
                Thread.sleep(250);
                mc.thePlayer.inventory.currentItem = 0;
                Thread.sleep(250);
                KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
                Thread.sleep(1000);
                Method m = ((GuiEditSign)mc.currentScreen).getClass().getDeclaredMethod("func_73869_a", char.class, int.class);
                Utils.addCustomLog(m.toString());
                m.setAccessible(true);
                m.invoke(mc.currentScreen, '\r', 14);
                Thread.sleep(500);
                m.invoke(mc.currentScreen, '\r', 14);
                Thread.sleep(500);
                m.invoke(mc.currentScreen, '\r', 14);
                Thread.sleep(500);
                m.invoke(mc.currentScreen, '2', 16);
                Thread.sleep(500);
                m.invoke(mc.currentScreen, '0', 16);
                Thread.sleep(500);
                m.invoke(mc.currentScreen, '0', 16);
                Thread.sleep(500);
                mc.thePlayer.closeScreen();

                //init pos
                Utils.addCustomLog("Initializing place sugarcane");
                rotation.easeTo(AngleUtils.parallelToC1(), 89f, 1000);
                while (rotation.rotating)
                    threadSleep(1);

                Utils.goToRelativeBlock(0, calculateInitWalk());
                threadSleep(500);
                mc.thePlayer.inventory.currentItem = 6;
                resetKeybindState();
                setKeyBindState(keybindUseItem, true);
                if (Math.abs(mc.thePlayer.posX % 1) == 0.5 && Math.abs(mc.thePlayer.posZ % 1) == 0.5) {
                    Utils.addCustomLog("in center");
                    rotation.reset();
                    rotation.easeTo(AngleUtils.get360RotationYaw(), 50f, 500);
                    mc.thePlayer.inventory.currentItem = 2;
                    setKeyBindState(keybindUseItem, false);
                }
                threadSleep(500);
                currentState = State.NONE;
                targetBlockPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            } catch (Exception e) {
                CaneBuilder.disableScript();
            }
        }));
        resetKeybindState();

    }



    @Override
    public void onDisable() {

    }

    private void updateState() {
        BlockPos blockInPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        if (lastState == State.SWITCH) {
            currentState = State.FORWARD;
            return;
        }
        if (lastState == State.FORWARD) {
            if (blockInPos.getX() != targetBlockPos.getX() || blockInPos.getZ() != targetBlockPos.getZ() || !isInCenterOfBlock())
                return;
            Utils.addCustomLog("Changing back");
            currentState = BlockUtils.isWalkable(BlockUtils.getLeftBlock()) ? State.LEFT : State.RIGHT;
            return;
        }
        if ((!BlockUtils.isWalkable(BlockUtils.getLeftBlock()) || !BlockUtils.isWalkable(BlockUtils.getRightBlock())) &&
          (blockInPos.getX() != targetBlockPos.getX() || blockInPos.getZ() != targetBlockPos.getZ()) &&
          Math.round(Math.abs(mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * 100.0) / 100.0 == 0 && Math.round(Math.abs(mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * 100.0) / 100.0 == 0) {
            currentState = State.SWITCH;
            targetBlockPos = calculateTargetBlockPos();
            return;
        }
        if (lastState != currentState || currentState == State.NONE) {
            currentState = calculateDirection();
            rotation.reset();
        }
    }

    Thread RefillSc = new Thread() {
        @Override
        public void run() {

            try {
                updateKeys(false, false, false, false, false, false, false);
                if (!Utils.hasSugarcaneInInv()) {
                    Utils.addCustomLog("Buying sugarcane from bazaar");
                    mc.thePlayer.sendChatMessage("/bz");
                    Thread.sleep(1000);
                    if ((mc.thePlayer.openContainer instanceof ContainerChest)) {
                        Utils.clickWindow(mc.thePlayer.openContainer.windowId, 0, 0, 0);
                        Thread.sleep(1000);
                        Utils.clickWindow(mc.thePlayer.openContainer.windowId, 22, 0, 0);
                        Thread.sleep(1000);
                        Utils.clickWindow(mc.thePlayer.openContainer.windowId, 10, 0, 0);
                        Thread.sleep(1000);
                        Utils.clickWindow(mc.thePlayer.openContainer.windowId, 10, 0, 0);
                        Thread.sleep(1000);
                        Utils.clickWindow(mc.thePlayer.openContainer.windowId, 14, 0, 0);
                        Thread.sleep(1000);
                        mc.thePlayer.closeScreen();
                        Thread.sleep(500);
                        refillingSc = false;
                        Utils.addCustomLog("Finished buying sugarcane from bazaar");

                    } else {
                        Utils.addCustomLog("Didn't open bazaar. Disabling script");
                    }
                } else if (!Utils.hasSugarcaneInHotbar()) {
                    Utils.addCustomLog("Preparing to move sugarcane to hotbar");
                    Thread.sleep(1000);
                    if (mc.currentScreen == null)
                        mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
                    else
                        return;
                    Thread.sleep(1000);

                    while (!Utils.isHotbarFull() && Utils.hasSugarcaneInMainInv()) {
                        Utils.clickWindow(mc.thePlayer.openContainer.windowId, Utils.getFirstSlotWithSugarcane(), 0, 1);
                        Thread.sleep(500);
                    }
                    mc.thePlayer.closeScreen();
                    refillingSc = false;
                    Utils.addCustomLog("Finished moving sugarcane to hotbar");
                } else {
                    Utils.addCustomLog("Unknown case, disabling script");
                }
            } catch (Exception e) {
                CaneBuilder.disableScript();
            }
        }
    };
    BlockPos calculateTargetBlockPos() {

        if (!BlockUtils.isWalkable(BlockUtils.getRightBlock()) || !BlockUtils.isWalkable(BlockUtils.getLeftBlock())) {
            if (!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())) {
                return BlockUtils.getBlockPosAround(0, 1, 0);
            } else {
                if (!BlockUtils.isWalkable(BlockUtils.getBlockAround(-1, 1, 0)) && !BlockUtils.isWalkable(BlockUtils.getBlockAround(1, 1, 0))) {
                    Utils.addCustomLog("BlockPos : +2");
                    return BlockUtils.getBlockPosAround(0, 2, 0);
                } else {
                    Utils.addCustomLog("BlockPos : +3");
                    return BlockUtils.getBlockPosAround(0, 3, 0);
                }

            }
        }
        Utils.addCustomLog("Can't calculate block pos");
        return new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    int calculateInitWalk() {
        for (int i = 0; i < 5; i++) {
            if (BlockUtils.isWalkable(BlockUtils.getBlockAround(1, i, 0)) || BlockUtils.isWalkable(BlockUtils.getBlockAround(-1, i, 0)))
                return i;
        }
        return 0;
    }

    State calculateDirection() {
        ArrayList<Integer> unwalkableBlocks = new ArrayList<>();
        if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)).getBlock().equals(Blocks.end_portal_frame)) {
            for (int i = -3; i < 3; i++) {
                if (!BlockUtils.isWalkable(BlockUtils.getBlockAround(i, 0, 1))) {
                    unwalkableBlocks.add(i);
                }
            }
        } else {
            for (int i = -3; i < 3; i++) {
                if (!BlockUtils.isWalkable(BlockUtils.getBlockAround(i, 0, 0))) {
                    unwalkableBlocks.add(i);
                }
            }
        }

        if (unwalkableBlocks.size() == 0)
            return State.RIGHT;
        else if (unwalkableBlocks.get(0) > 0)
            return State.LEFT;
        else
            return State.RIGHT;
    }

    boolean blockLagged() {
        if (currentState == State.RIGHT) {
            return IntStream.rangeClosed(-8, -1).anyMatch(i -> !sugarcaneIsPresent(i, 0) || !sugarcaneIsPresent(i, 1));
        } else if (currentState == State.LEFT) {
            return IntStream.rangeClosed(1, 8).anyMatch(i -> !sugarcaneIsPresent(i, 0) || !sugarcaneIsPresent(i, 1));
        } else
            return false;
    }

    boolean sugarcaneIsPresent(int rightOffset, int frontOffset) {
        if (isWaterBlock(rightOffset, frontOffset + 1, -1) || isWaterBlock(rightOffset, frontOffset - 1, -1)) {
            if (!isWaterBlock(rightOffset, frontOffset + 2, -1))
                return true;
            return isSugarcaneBlock(rightOffset, frontOffset, 0);
        }
        return true;
    }

    boolean isSugarcaneBlock(int rightOffset, int frontOffset, int upOffset) {
        return BlockUtils.getBlockAround(rightOffset, frontOffset, upOffset).equals(Blocks.reeds);
    }

    boolean isWaterBlock(int rightOffset, int frontOffset, int upOffset) {
        return BlockUtils.getBlockAround(rightOffset, frontOffset, upOffset).equals(Blocks.water) || BlockUtils.getBlockAround(rightOffset, frontOffset, upOffset).equals(Blocks.flowing_water);
    }


    public static boolean isInCenterOfBlock() {
        return (Math.round(AngleUtils.get360RotationYaw()) == 180 || Math.round(AngleUtils.get360RotationYaw()) == 0) ? Math.abs(Minecraft.getMinecraft().thePlayer.posZ) % 1 > 0.3f && Math.abs(Minecraft.getMinecraft().thePlayer.posZ) % 1 < 0.7f :
          Math.abs(Minecraft.getMinecraft().thePlayer.posX) % 1 > 0.3f && Math.abs(Minecraft.getMinecraft().thePlayer.posX) % 1 < 0.7f;

    }
}