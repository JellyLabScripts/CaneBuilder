package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.BlockUtils;
import com.jelly.CaneBuilder.utils.Clock;
import com.jelly.CaneBuilder.utils.Utils;

import static com.jelly.CaneBuilder.KeyBindHelper.*;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.EnumChatFormatting;

public class PlaceSC extends ProcessModule {
    boolean aote = false;
    boolean walkingForward;
    boolean refillingSc;
    boolean canePlaceLag;
    boolean switching = false;
    boolean lagged = false;
    float pitch;
    Clock lagCooldown = new Clock();
    State currentState;

    enum State {
        START,
        EDGE_RIGHT,
        EDGE_LEFT,
        RIGHT,
        LEFT,
        FORWARDS,
        NONE
    }

    @Override
    public void onTick() {
        if (rotation.rotating || currentState == State.START) {
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

        mc.thePlayer.inventory.currentItem = Utils.getFirstHotbarSlotWithSugarcane() - 36;

        switch (currentState) {
            case LEFT:
                updateKeys(false, false, true, false, false, true, false);
                return;
            case RIGHT:
                updateKeys(false, false, false, true, false, true, false);
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
        new Thread(() -> {
            try {
                threadSleep(500);
                mc.thePlayer.inventory.currentItem = 8;
                threadSleep(100);
                onTick(keybindUseItem);
                threadSleep(800);

                clickWindow(mc.thePlayer.openContainer.windowId, 22, 0, 0);

                threadSleep(1000);
                while (Utils.getFirstSlotWithDirt() != -1 && enabled) {
                    clickWindow(mc.thePlayer.openContainer.windowId, 45 + Utils.getFirstSlotWithDirt(), 0, 0);
                    threadSleep(500);
                }
                threadSleep(500);
                mc.thePlayer.closeScreen();
                threadSleep(500);
                mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
                threadSleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 36, 0, 1);
                threadSleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 37, 0, 1);
                threadSleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 38, 0, 1);
                threadSleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 39, 0, 1);
                threadSleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 40, 0, 1);
                threadSleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 41, 0, 1);
                threadSleep(500);
                clickWindow(mc.thePlayer.openContainer.windowId, 43, 0, 1);
                threadSleep(500);
                mc.thePlayer.closeScreen();
                threadSleep(500);
                currentState = State.NONE;
                rotation.easeTo(AngleUtils.parallelToC1(), 50f, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        resetKeybindState();
        canePlaceLag = false;
        refillingSc = false;
        currentState = State.START;
        lagged = false;
        aote = false;
        switching = false;
        // rotation.easeTo(AngleUtils.parallelToC2(), 11.5f, 1000);
    }

    @Override
    public void onDisable() {

    }

    private void updateState() {
        if (currentState == State.START) return;

        if (currentState == State.FORWARDS && BlockUtils.getBlockAround(0, 1, 0).equals(Blocks.dirt)) {
            currentState = State.NONE;
            Utils.addCustomLog("Completed sugarcane placement");
            resetKeybindState();
            CaneBuilder.switchToNextProcess(this);
            return;
        }

        if (currentState != State.START && (!Utils.hasSugarcaneInHotbar() || !Utils.hasSugarcaneInInv())) {
            currentState = State.START;
            resetKeybindState();
            ExecuteRunnable(RefillSc);
            return;
        }

        Utils.addCustomLog("brbrbrbr" + blockLagged() + ", " + blockLaggedFlip());

        if ((currentState == State.RIGHT || currentState == State.LEFT) && !lagged && blockLagged()) {
            Utils.addCustomLog("lagstart");

            if (currentState == State.RIGHT) {
                currentState = State.LEFT;
            } else {
                currentState = State.RIGHT;
            }
            lagCooldown.schedule(300);
            lagged = true;
            return;
        }

        if ((currentState == State.RIGHT || currentState == State.LEFT) && lagged && !blockLaggedFlip() && lagCooldown.passed()) {
            Utils.addCustomLog("lagend");

            if (currentState == State.RIGHT) {
                currentState = State.LEFT;
            } else {
                currentState = State.RIGHT;
            }
            lagged = false;
            return;
        }

        if (BlockUtils.getBlockAround(-1, 0, 0).equals(Blocks.dirt) && BlockUtils.getBlockAround(1, 0, 0).equals(Blocks.dirt)) {
            currentState = State.FORWARDS;
        } else if (BlockUtils.getBlockAround(-1, 0, 0).equals(Blocks.dirt)) {
            if (BlockUtils.getBlockAround(1, -1, 0).equals(Blocks.dirt)) {
                if (currentState == State.EDGE_RIGHT || currentState == State.RIGHT) {
                    currentState = State.RIGHT;
                } else if (currentState != State.LEFT && !switching) {
                    rotation.easeTo(AngleUtils.parallelToC1(), 89, 600);
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
        } else if (BlockUtils.getBlockAround(1, 0, 0).equals(Blocks.dirt)) {
            if (BlockUtils.getBlockAround(-1, -1, 0).equals(Blocks.dirt)) {
                if (currentState == State.EDGE_LEFT || currentState == State.LEFT) {
                    currentState = State.LEFT;
                } else if (currentState != State.RIGHT && !switching) {
                    rotation.easeTo(AngleUtils.parallelToC1(), 89, 600);
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

    Runnable RefillSc = new Runnable() {
        @Override
        public void run() {

            try {
                updateKeys(false, false, false, false, false, false, false);
                if (!Utils.hasSugarcaneInInv()) {
                    ExecuteRunnable(BuySugarcane);
                } else if (!Utils.hasSugarcaneInHotbar()) {
                    ExecuteRunnable(PutScToHotbar);
                } else {
                    Utils.addCustomLog("Unknown case, disabling script");
                }
            } catch (Exception e) {

            }
        }
    };

    Runnable PutScToHotbar = new Runnable() {
        @Override
        public void run() {
            try {
                Utils.addCustomLog("Preparing to move sugarcane to hotbar");
                Thread.sleep(1000);
                if (mc.currentScreen == null)
                    mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
                else
                    return;
                Thread.sleep(1000);

                while (!Utils.isHotbarFull() && Utils.hasSugarcaneInMainInv()) {
                    clickWindow(mc.thePlayer.openContainer.windowId, Utils.getFirstSlotWithSugarcane(), 0, 1);
                    Thread.sleep(500);
                }
                mc.thePlayer.closeScreen();
                currentState = State.NONE;
                Utils.addCustomLog("Finished moving sugarcane to hotbar");

            } catch (Exception e) {
            }
        }
    };

    Runnable BuySugarcane = new Runnable() {
        @Override
        public void run() {
            try {
                if (!enabled) return;

                Utils.addCustomLog("Buying sugarcane from bazaar");
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
                    currentState = State.NONE;
                    Utils.addCustomLog("Finished buying sugarcane from bazaar");

                } else {
                    Utils.addCustomLog("Didn't open bazaar. Disabling script");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    boolean blockLagged() {
        if (currentState == State.RIGHT) {
            return !(sugarcaneIsPresent(-3, 1)) || !(sugarcaneIsPresent(-3, 0)) ||
              !(sugarcaneIsPresent(-2, 1)) || !(sugarcaneIsPresent(-2, 0)) ||
              !(sugarcaneIsPresent(-1, 1)) || !(sugarcaneIsPresent(-1, 0));
        } else {
            return !(sugarcaneIsPresent(3, 1)) || !(sugarcaneIsPresent(3, 0)) ||
              !(sugarcaneIsPresent(2, 1)) || !(sugarcaneIsPresent(2, 0)) ||
              !(sugarcaneIsPresent(1, 1)) || !(sugarcaneIsPresent(1, 0));
        }
    }

    boolean blockLaggedFlip() {
        if (currentState == State.LEFT) {
            return !(sugarcaneIsPresent(-3, 1)) || !(sugarcaneIsPresent(-3, 0)) ||
              !(sugarcaneIsPresent(-2, 1)) || !(sugarcaneIsPresent(-2, 0)) ||
              !(sugarcaneIsPresent(-1, 1)) || !(sugarcaneIsPresent(-1, 0));
        } else {
            return !(sugarcaneIsPresent(3, 1)) || !(sugarcaneIsPresent(3, 0)) ||
              !(sugarcaneIsPresent(2, 1)) || !(sugarcaneIsPresent(2, 0)) ||
              !(sugarcaneIsPresent(1, 1)) || !(sugarcaneIsPresent(1, 0));
        }
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

    void clickWindow(int windowID, int slotID, int mouseButtonClicked, int mode) throws Exception {
        if (mc.thePlayer.openContainer instanceof ContainerChest || mc.currentScreen instanceof GuiInventory) {
            mc.playerController.windowClick(windowID, slotID, mouseButtonClicked, mode, mc.thePlayer);
            Utils.addCustomLog("Pressing slot : " + slotID);
        } else {
            Utils.addCustomMessage(EnumChatFormatting.RED + "Didn't open window! This shouldn't happen and the script has been disabled. Please immediately report to the developer.");
            updateKeys(false, false, false, false, false, false, false);
            throw new Exception();
        }
    }
}