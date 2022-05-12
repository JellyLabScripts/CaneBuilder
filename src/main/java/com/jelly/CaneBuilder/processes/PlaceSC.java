package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.CaneBuilder;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.BlockUtils;
import com.jelly.CaneBuilder.utils.Utils;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlaceSC extends ProcessModule{
    boolean AOTEing = false;
    boolean walkingForward;
    double initialX = 0;
    double initialZ = 0;
    boolean pushedOff;
    public BlockPos targetBlockPos= new BlockPos(10000, 10000, 10000);
    boolean refillingSc;
    boolean canePlaceLag;

    int playerYaw;
    direction currentDirection;
    direction lastLaneDirection;

    @Override
    public void onTick(){
        if(AOTEing) {
            resetKeybindState();
            return;
        }
        double dx = Math.abs(mc.thePlayer.posX - mc.thePlayer.lastTickPosX);
        double dy = Math.abs(mc.thePlayer.posY - mc.thePlayer.lastTickPosY);
        double dz = Math.abs(mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ);

        BlockPos blockInPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);

        if(!refillingSc) {
            if(((!BlockUtils.isWalkable(BlockUtils.getBlockAround(1, 1, 0)) && !BlockUtils.isWalkable(BlockUtils.getBlockAround(0, 1, 0))) ||
                    (!BlockUtils.isWalkable(BlockUtils.getBlockAround(-1, 1, 0)) && !BlockUtils.isWalkable(BlockUtils.getBlockAround(0, 1, 0))))){
                walkingForward = false;
                Utils.addCustomMessage("Ended placing sugarcane");
                CaneBuilder.switchToNextProcess(this);
                updateKeys(false, false, false, false, false, false, false);
                return;
            }
            if (!Utils.hasSugarcaneInHotbar() || !Utils.hasSugarcaneInInv()) {
                refillingSc = true;
                updateKeys(false, false, false, false, false, false, false);
                ExecuteRunnable(RefillSc);
                return;
            }


            AngleUtils.hardRotate(playerYaw);
            mc.thePlayer.rotationPitch = 50;
            mc.thePlayer.inventory.currentItem = Utils.getFirstHotbarSlotWithSugarcane() - 36;

            if (dy == 0) {
                if (!walkingForward) { //normal

                    setKeyBindState(keybindUseItem, true);
                    setKeyBindState(keybindW, false);
                    if(!canePlaceLag) {
                        canePlaceLag = blockLagged(currentDirection);
                        if(canePlaceLag) {
                            Utils.addCustomLog("Detected lag");
                            ScheduleRunnable(ResumePlacing, 1, TimeUnit.SECONDS);
                        }
                    }
                    if(canePlaceLag){
                        initialX = mc.thePlayer.posX;
                        initialZ = mc.thePlayer.posZ;
                    }

                    setKeyBindState(keyBindShift, false);
                    if (currentDirection.equals(direction.RIGHT)) {
                        setKeyBindState(keybindD, !canePlaceLag);
                        setKeyBindState(keybindA, canePlaceLag);

                    }
                    else if (currentDirection.equals(direction.LEFT)) {
                        setKeyBindState(keybindA, !canePlaceLag);
                        setKeyBindState(keybindD, canePlaceLag);
                    }
                    else
                        walkingForward = true;


                } else { // walking forward
                    setKeyBindState(keyBindShift, false);
                    //unleash keys
                    if (lastLaneDirection.equals(direction.LEFT))
                        setKeyBindState(keybindD, false);
                    else
                        setKeyBindState(keybindA, false);
                    setKeyBindState(keybindW, true);
                }
            }


            //change to walk forward
            if (Utils.roundTo2DecimalPlaces(dx) == 0 && Utils.roundTo2DecimalPlaces(dz) == 0) {
                if (shouldWalkForward() && !walkingForward && ((int) initialX != (int) mc.thePlayer.posX || (int) initialZ != (int) mc.thePlayer.posZ)) { // &&
                    setKeyBindState(keybindUseItem, false);
                    // updateKeybinds(true, false, false, false);
                    walkingForward = true;
                    targetBlockPos = calculateTargetBlockPos();
                    Utils.addCustomLog("Target block : " + targetBlockPos.toString());
                    pushedOff = false;
                    initialX = mc.thePlayer.posX;
                    initialZ = mc.thePlayer.posZ;
                }
            }

            //chagnge back to left/right
            if (blockInPos.getX() == targetBlockPos.getX() && blockInPos.getZ() == targetBlockPos.getZ() && walkingForward) {

                new Thread(() -> {
                    AOTEing = true;
                    AOTE();
                    setKeyBindState(keybindUseItem, false);
                    mc.thePlayer.sendChatMessage("/setspawn");
                    if (!BlockUtils.isWalkable(BlockUtils.getLeftBlock()) || !BlockUtils.isWalkable(BlockUtils.getBlockAround(-2, 0))) {
                        //set last lane dir
                        currentDirection = direction.RIGHT;
                        lastLaneDirection = direction.RIGHT;
                        updateKeys(false, false, false, true, false);
                    } else if (!BlockUtils.isWalkable(BlockUtils.getRightBlock()) || !BlockUtils.isWalkable(BlockUtils.getBlockAround(2, 0))) {
                        currentDirection = direction.LEFT;
                        lastLaneDirection = direction.LEFT;
                        updateKeys(false, false, true, false, false);
                    }

                    initialX = mc.thePlayer.posX;
                    initialZ = mc.thePlayer.posZ;
                    Utils.addCustomLog("Changing motion : Going " + currentDirection);
                  ///  ScheduleRunnable(PressS, 200, TimeUnit.MILLISECONDS);
                    walkingForward = false;
                    AOTEing = false;
                }).start();

            }
        } else {
            updateKeys(false, false, false, false, false, false, false);
        }

    }

    @Override
    public void onEnable(){
        AOTEing = true;
        walkingForward = false;
        initialX = 10000;
        initialZ = 10000;

        if(!(!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock()))) {
            initialX = mc.thePlayer.posX;
            initialZ = mc.thePlayer.posZ;
        }
        pushedOff = false;
        canePlaceLag = false;
        refillingSc = false;
        new Thread(() -> {

            try {
                AngleUtils.smoothRotateClockwise(180);
                threadSleep(500);
                AOTE();
                threadSleep(500);
                mc.thePlayer.inventory.currentItem = (8);
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
                clickWindow(mc.thePlayer.openContainer.windowId, 42, 0, 1);
                threadSleep(500);
                mc.thePlayer.closeScreen();
                threadSleep(500);
                AngleUtils.smoothRotatePitchTo(50, 1.2f);
                threadSleep(500);
                currentDirection = calculateDirection();
                lastLaneDirection = calculateDirection();
                playerYaw = Math.round(AngleUtils.get360RotationYaw() / 90) < 4 ? Math.round(AngleUtils.get360RotationYaw() / 90) * 90 : 0;
                AOTEing = false;
            }catch(Exception e){
                e.printStackTrace();
            }
        }).start();

    }

    Runnable ResumePlacing = () -> canePlaceLag = false;

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
            } catch(Exception e){

            }
        }
    };
    Runnable PutScToHotbar = new Runnable() {
        @Override
        public void run() {

            try {
                Utils.addCustomLog("Preparing to move sugarcane to hotbar");
                Thread.sleep(1000);
                mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
                Thread.sleep(1000);

                while(!Utils.isHotbarFull() && Utils.hasSugarcaneInMainInv()){
                    clickWindow(mc.thePlayer.openContainer.windowId, Utils.getFirstSlotWithSugarcane(), 0, 1);
                    Thread.sleep(500);
                }
                mc.thePlayer.closeScreen();
                refillingSc = false;
                Utils.addCustomLog("Finished moving sugarcane to hotbar");

            } catch(Exception e){
            }
        }
    };
    Runnable BuySugarcane = new Runnable() {
        @Override
        public void run() {
            try {
                if(!enabled) return;

                Utils.addCustomLog("Buying sugarcane from bazaar");
                mc.thePlayer.sendChatMessage("/bz");
                Thread.sleep(1000);
                if((mc.thePlayer.openContainer instanceof ContainerChest)){
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
                    Utils.addCustomLog("Finished buying sugarcane from bazaar");

                } else {
                    Utils.addCustomLog("Didn't open bazaar. Disabling script");
                }

            } catch(Exception e){
                e.printStackTrace();
            }

        }
    };

    boolean blockLagged(direction playerGoingDir){
        if(playerGoingDir == direction.RIGHT) {
            return !(sugarcaneIsPresent(-3, 1)) || !(sugarcaneIsPresent(-3, 0)) ||
                    !(sugarcaneIsPresent(-2, 1)) || !(sugarcaneIsPresent(-2, 1));
        } else {
            return !(sugarcaneIsPresent(3, 1)) || !(sugarcaneIsPresent(3, 0)) ||
                    !(sugarcaneIsPresent(2, 1)) || !(sugarcaneIsPresent(2, 0));
        }
    }
    boolean sugarcaneIsPresent(int rightOffset, int frontOffset){
        if(isWaterBlock(rightOffset, frontOffset + 1, -1) || isWaterBlock(rightOffset, frontOffset - 1, -1)){
            if(!isWaterBlock(rightOffset, frontOffset + 2, -1))
                return true;
            return isSugarcaneBlock(rightOffset, frontOffset, 0);
        }
        return true;
    }
    boolean isSugarcaneBlock(int rightOffset, int frontOffset, int upOffset){
        return BlockUtils.getBlockAround(rightOffset, frontOffset, upOffset).equals(Blocks.reeds);
    }
    boolean isWaterBlock(int rightOffset, int frontOffset, int upOffset){
        return BlockUtils.getBlockAround(rightOffset, frontOffset, upOffset).equals(Blocks.water) || BlockUtils.getBlockAround(rightOffset, frontOffset, upOffset).equals(Blocks.flowing_water);
    }
    boolean shouldWalkForward() {
        return (BlockUtils.isWalkable(BlockUtils.getBackBlock()) && BlockUtils.isWalkable(BlockUtils.getFrontBlock())) ||
                (!BlockUtils.isWalkable(BlockUtils.getBackBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())) ||
                (!BlockUtils.isWalkable(BlockUtils.getBackBlock()) && !BlockUtils.isWalkable(BlockUtils.getRightBlock())) ||
                (!BlockUtils.isWalkable(BlockUtils.getFrontBlock()) && !BlockUtils.isWalkable(BlockUtils.getRightBlock())) ||
                (!BlockUtils.isWalkable(BlockUtils.getFrontBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())) ||
                (!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock()));
    }
    void clickWindow(int windowID, int slotID, int mouseButtonClicked, int mode) throws Exception{

        if(mc.thePlayer.openContainer instanceof ContainerChest || mc.currentScreen instanceof  GuiInventory) {
            mc.playerController.windowClick(windowID, slotID, mouseButtonClicked, mode, mc.thePlayer);
            Utils.addCustomLog("Pressing slot : " + slotID);
        }
        else {
            Utils.addCustomMessage(EnumChatFormatting.RED + "Didn't open window! This shouldn't happen and the script has been disabled. Please immediately report to the developer.");
            updateKeys(false, false, false, false, false, false, false);
            throw new Exception();
        }
    }
    direction calculateDirection() {
        ArrayList<Integer> unwalkableBlocks = new ArrayList<>();
        if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)).getBlock().equals(Blocks.end_portal_frame)) {
            for (int i = -3; i < 3; i++) {
                if (!BlockUtils.isWalkable(BlockUtils.getBlockAround(i, 0, 1))) {
                    unwalkableBlocks.add(i);
                }
            }
        } else {
            for (int i = -3; i < 3; i++) {
                if (!BlockUtils.isWalkable(BlockUtils.getBlockAround(i, 0))) {
                    unwalkableBlocks.add(i);
                }
            }
        }

        if (unwalkableBlocks.size() == 0)
            return direction.RIGHT;
        else if (unwalkableBlocks.get(0) > 0)
            return direction.LEFT;
        else
            return direction.RIGHT;
    }


    BlockPos calculateTargetBlockPos(){

        if(!BlockUtils.isWalkable(BlockUtils.getRightBlock()) || !BlockUtils.isWalkable(BlockUtils.getLeftBlock())){
            if(!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())){
                for(int i = 0; i < 5; i++){
                    if(!(!BlockUtils.isWalkable(BlockUtils.getBlockAround(1, i, 0)) && !BlockUtils.isWalkable(BlockUtils.getBlockAround(-1, i, 0))))
                        return BlockUtils.getBlockPosAround(0, i, 0);
                }

            } else {
                return BlockUtils.getBlockPosAround(0, 3, 0);


            }
        }

        Utils.addCustomLog("can't calculate target block!");
        return new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);


    }
}
