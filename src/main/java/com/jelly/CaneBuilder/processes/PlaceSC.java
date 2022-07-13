package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.handlers.MacroHandler;
import com.jelly.CaneBuilder.utils.*;

import static com.jelly.CaneBuilder.handlers.KeyBindHandler.*;
import static com.jelly.CaneBuilder.utils.InventoryUtils.clickWindow;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class PlaceSC extends ProcessModule {
    volatile boolean refillingSc;
    volatile boolean canePlaceLag;
    volatile boolean switching = false;
    volatile boolean lagged = false;
    volatile boolean pushedOff = false;

    volatile BlockPos targetBlockPos = new BlockPos(10000, 10000, 10000);
    volatile Clock lagCooldown = new Clock();
    volatile State currentState;
    volatile State lastState;


    public final static Field rightClickDelayTimerField;

    static {
        rightClickDelayTimerField = ReflectionHelper.findField(Minecraft.class, "field_71467_ac", "rightClickDelayTimer");

        if (rightClickDelayTimerField != null) {
            rightClickDelayTimerField.setAccessible(true);
        }
    }
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

        if (rotation.rotating || refillingSc || ScoreboardUtils.getLocation() != ScoreboardUtils.location.ISLAND) {
            resetKeybindState();
            return;
        }
        if (currentState == State.START)
            return;


        try {
            rightClickDelayTimerField.set(mc, 0);
        }catch (Exception ignored){}

        if (blockLagged() && !lagged) {
            LogUtils.addCustomLog("Detected not placed sugarcane");
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
        if (!InventoryUtils.hasSugarcaneInHotbar() || !InventoryUtils.hasSugarcaneInInv()) {
            refillingSc = true;
            updateKeys(false, false, false, false, false, false, false);
            ExecuteRunnable(new Thread(RefillSc));
            return;
        }


        updateState();
        lastState = currentState;


        rotation.lockAngle(AngleUtils.parallelToC1(), 50f);
        mc.thePlayer.inventory.currentItem = InventoryUtils.getFirstHotbarSlotWithSugarcane() - 36;


        switch (currentState) {
            case LEFT:
                updateKeys(false, false, true, false, false, true, false);
                return;
            case RIGHT:
                updateKeys(false, false, false, true, false, true, false);
                return;
            case SWITCH:
                LogUtils.addCustomLog("Switching");
                updateKeys(false, false, false, false, false, true, false);
                if (!pushedOff) {
                    updateKeys(false, false, !BlockUtils.isWalkable(BlockUtils.getLeftBlock()), BlockUtils.isWalkable(BlockUtils.getLeftBlock()), false, true, false);
                }
                pushedOff = true;
                return;
            case FORWARD:
                if (BlockUtils.getBlockAround(0, 1, 0).equals(Blocks.dirt)) {
                    currentState = State.NONE;
                    LogUtils.addCustomLog("Completed sugarcane placement");
                    resetKeybindState();
                    MacroHandler.switchToNextProcess(this);
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
        pushedOff = false;
        currentState = State.START;
        ExecuteRunnable(new Thread(() -> {
            try {
                threadSleep(500);
                //autosell dirt
                threadSleep(500);
                mc.thePlayer.inventory.currentItem = 8;
                threadSleep(100);
                onTick(keybindUseItem);
                threadSleep(800);
                clickWindow(mc.thePlayer.openContainer.windowId, 22, 0, 0);
                threadSleep(1000);
                while (InventoryUtils.getFirstSlotWithDirt() != -1 && enabled && InventoryUtils.countDirtStack() > 3) {
                    clickWindow(mc.thePlayer.openContainer.windowId, 45 + InventoryUtils.getFirstSlotWithDirt(), 0, 0);
                    threadSleep(500);
                }
                threadSleep(500);
                mc.thePlayer.closeScreen();
                threadSleep(500);
                ProcessUtils.setRancherBootsTo200();
                //clear hotbar
                InventoryUtils.openInventory();
                for(int i = 0; i < 8; i++) {
                    if (mc.thePlayer.inventoryContainer.getSlot(i + 36).getHasStack()) {
                        clickWindow(mc.thePlayer.openContainer.windowId, 36 + i, 0, 1);
                        Thread.sleep(500);
                    }
                }
                Thread.sleep(500);
                mc.thePlayer.closeScreen();
                Thread.sleep(500);
                //init pos
                LogUtils.addCustomLog("Initializing place sugarcane");
                threadSleep(500);
                rotation.easeTo(AngleUtils.parallelToC1(), 50f, 1000);
                while (rotation.rotating)
                    threadSleep(1);
                threadSleep(500);
                lastState = State.START;
                currentState = State.NONE;
                targetBlockPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            } catch (Exception e) {
                MacroHandler.disableScript();
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
            mc.thePlayer.sendChatMessage("/setspawn");
            LogUtils.addCustomLog("Changing back");
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

    Runnable RefillSc = new Runnable() {
        @Override
        public void run() {

            try {

                LogUtils.addCustomLog("Refilling sugarcane");
                updateKeys(false, false, false, false, false, false, false);
                if (!InventoryUtils.hasSugarcaneInInv()) {
                    LogUtils.addCustomLog("Buying sugarcane from bazaar");
                    mc.thePlayer.sendChatMessage("/bz");
                    Thread.sleep(1000);
                    if ((mc.thePlayer.openContainer instanceof ContainerChest)) {
                        clickWindow(mc.thePlayer.openContainer.windowId, 0, 0, 0);
                        Thread.sleep(1000);
                        clickWindow(mc.thePlayer.openContainer.windowId, 22, 0, 0);
                        Thread.sleep(1000);
                        clickWindow(mc.thePlayer.openContainer.windowId, 10, 0, 0);
                        Thread.sleep(1000);
                        clickWindow(mc.thePlayer.openContainer.windowId, 10, 0, 0);
                        Thread.sleep(1000);
                        clickWindow(mc.thePlayer.openContainer.windowId, 14, 0, 0);
                        Thread.sleep(1000);
                        mc.thePlayer.closeScreen();
                        Thread.sleep(500);
                        refillingSc = false;
                        LogUtils.addCustomLog("Finished buying sugarcane from bazaar");

                    } else {
                        LogUtils.addCustomLog("Didn't open bazaar. Disabling script");
                    }
                } else if (!InventoryUtils.hasSugarcaneInHotbar()) {
                    LogUtils.addCustomLog("Preparing to move sugarcane to hotbar");
                    Thread.sleep(1000);
                    if (mc.currentScreen == null)
                        InventoryUtils.openInventory();
                    else
                        return;
                    Thread.sleep(1000);

                    while (!InventoryUtils.isHotbarFull() && InventoryUtils.hasSugarcaneInMainInv()) {
                        clickWindow(mc.thePlayer.openContainer.windowId, InventoryUtils.getFirstSlotWithSugarcane(), 0, 1);
                        Thread.sleep(500);
                    }
                    mc.thePlayer.closeScreen();
                    refillingSc = false;
                    LogUtils.addCustomLog("Finished moving sugarcane to hotbar");
                } else {
                    LogUtils.addCustomLog("Unknown case, disabling script");
                }
            } catch (Exception e) {
                MacroHandler.disableScript();
            }
        }
    };
    BlockPos calculateTargetBlockPos() {

        if (!BlockUtils.isWalkable(BlockUtils.getRightBlock()) || !BlockUtils.isWalkable(BlockUtils.getLeftBlock())) {
            if (!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())) {
                return BlockUtils.getBlockPosAround(0, 1, 0);
            } else {
                if (!BlockUtils.isWalkable(BlockUtils.getBlockAround(-1, 1, 0)) && !BlockUtils.isWalkable(BlockUtils.getBlockAround(1, 1, 0))) {
                    LogUtils.addCustomLog("BlockPos : +2");
                    return BlockUtils.getBlockPosAround(0, 2, 0);
                } else {
                    LogUtils.addCustomLog("BlockPos : +3");
                    return BlockUtils.getBlockPosAround(0, 3, 0);
                }

            }
        }
        LogUtils.addCustomLog("Can't calculate block pos");
        return new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
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