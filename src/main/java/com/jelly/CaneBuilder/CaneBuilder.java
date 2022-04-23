package com.jelly.CaneBuilder;

import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mod(modid = CaneBuilder.MODID, version = CaneBuilder.VERSION)
public class CaneBuilder {
    public static final String MODID = "canebuilder";
    public static final String NAME = "Cane Builder";
    public static final String VERSION = "1.0";

    Minecraft mc = Minecraft.getMinecraft();

    static int setmode = 0;
    int corner1x = 0;
    int corner1y = 0;
    int corner1z = 0;
    int corner2x = 0;
    int corner2y = 0;
    int corner2z = 0;


    //states
    static boolean enabled = false;
    static boolean diggingPath = false;
    static boolean diggingTrench = false;
    static boolean goLeft = false;

    // for digging trench path
    static boolean slowDig;
    static boolean inDiggingTrench;
    // for digging path part
    static boolean walkingForward;
    static double initialX = 0;
    static double initialZ = 0;
    static float walkForwardDis;
    static boolean pushedOff;
    //for placing sugarcane
    static boolean placingSc;
    static boolean refillingSc;
    static boolean canePlaceLag;
    static boolean fixLag;
    //autoclicker
    static long initialTime = 0;


    /*
     ** @author JellyLab, Polycrylate
     */


    public int playerYaw;
    public int keybindA = mc.gameSettings.keyBindLeft.getKeyCode();
    public int keybindD = mc.gameSettings.keyBindRight.getKeyCode();
    public int keybindW = mc.gameSettings.keyBindForward.getKeyCode();
    public int keybindS = mc.gameSettings.keyBindBack.getKeyCode();
    public int keybindAttack = mc.gameSettings.keyBindAttack.getKeyCode();
    public int keybindUseItem = mc.gameSettings.keyBindUseItem.getKeyCode();
    public int keyBindSpace = mc.gameSettings.keyBindJump.getKeyCode();
    public int keyBindShift = mc.gameSettings.keyBindSneak.getKeyCode();
    public int keyBindJump = mc.gameSettings.keyBindJump.getKeyCode();


    enum direction {
        RIGHT,
        LEFT,
        NONE
    }

    direction currentDirection;
    direction lastLaneDirection;

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

        MinecraftForge.EVENT_BUS.register(new CaneBuilder());
    }
    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event){
        /*if(Keyboard.isKeyDown(Keyboard.KEY_P)){
            if(setmode == 0) {
                Utils.addCustomMessage("Set 1st corner");
                corner1x = (int)mc.thePlayer.posX;
                corner1y = (int)mc.thePlayer.posY - 1;
                corner1z = (int)mc.thePlayer.posZ;
                setmode = 1 - setmode;
            }
            else {
                Utils.addCustomMessage("Set 2nd corner");
                corner2x = (int)mc.thePlayer.posX;
                corner2y = (int)mc.thePlayer.posY - 1;
                corner2z = (int)mc.thePlayer.posZ;
                setmode = 1 - setmode;
            }
        }*/

        if(Keyboard.isKeyDown(Keyboard.KEY_F)){
            if(!enabled){
                Utils.addCustomMessage("Enabling script (Digging trench)");
                updateKeys(false, false, false, false, false, false, false);
                initVar();
                enabled = true;
                diggingTrench = true;
                ExecuteRunnable(InitializeDig);
            } else {
                disableScript();
            }
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_G)){
            if(!enabled){
                Utils.addCustomMessage("Enabling script (Digging path)");
                updateKeys(false, false, false, false, false, false, false);
                initVar();
                enabled = true;
                diggingPath = true;
            }
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_H)){
            if(!enabled){
                Utils.addCustomMessage("Enabling script (Placing sugarcane)");
                updateKeys(false, false, false, false, false, false, false);
                initVar();
                enabled = true;
                placingSc = true;
            }

        }

    }
    @SubscribeEvent
    public void onTickPlayer(TickEvent.ClientTickEvent tickEvent){
        if(tickEvent.phase != TickEvent.Phase.START)
            return;

        if(mc.thePlayer != null && mc.theWorld != null && enabled){
            double dx = Math.abs(mc.thePlayer.posX - mc.thePlayer.lastTickPosX);
            double dy = Math.abs(mc.thePlayer.posY - mc.thePlayer.lastTickPosY);
            double dz = Math.abs(mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ);


            mc.gameSettings.gammaSetting = 100;
            Block blockStandingOn = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)).getBlock();
            if(!placingSc) {
                if (mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiIngameMenu) {
                    updateKeys(false, false, false, false, false, false, false);
                    return;
                }
            }


            if(diggingTrench){
                if(inDiggingTrench){
                    if(!slowDig) {
                        if(getBorderBlock() != null) {

                            //getting close to border
                            if (Math.abs(getBorderBlock().getX() - mc.thePlayer.posX) < 7 && Math.abs(getBorderBlock().getZ() - mc.thePlayer.posZ) < 7) {
                                Utils.addCustomLog("Slow digging, border block = " + getBorderBlock());
                                slowDig = true;
                                ExecuteRunnable(SlowDig);
                                KeyBinding.setKeyBindState(keybindAttack, false);
                                return;
                            }
                        }

                        System.out.println("Digging");
                        Utils.hardRotate(playerYaw);
                        mc.thePlayer.rotationPitch = 20;
                        updateKeys(true, false, mc.gameSettings.keyBindLeft.isKeyDown(), mc.gameSettings.keyBindRight.isKeyDown(), true, false, false);
                    }
                }
            }
            if(diggingPath) {

                if(shouldEndDigging() && !(!Utils.isWalkable(Utils.getRightBlock()) && !Utils.isWalkable(Utils.getRightBlock()))){
                    walkingForward = false;
                    Utils.addCustomMessage("Ended process");
                    disableScript();
                    updateKeys(false, false, false, false, false, false, false);
                    return;
                }
                Utils.hardRotate(playerYaw);
                mc.thePlayer.rotationPitch = 11;
                if (dy == 0) {
                    if (!walkingForward) { //normal
                        KeyBinding.setKeyBindState(keybindAttack, true);
                        KeyBinding.setKeyBindState(keyBindShift, false);
                        if (currentDirection.equals(direction.RIGHT))
                            KeyBinding.setKeyBindState(keybindD, true);
                        else if (currentDirection.equals(direction.LEFT))
                            KeyBinding.setKeyBindState(keybindA, true);
                        else
                            walkingForward = true;
                    } else { // walking forward
                        KeyBinding.setKeyBindState(keyBindShift, true);
                        KeyBinding.setKeyBindState(keybindAttack, false);


                        //unleash keys
                        if (lastLaneDirection.equals(direction.LEFT))
                            KeyBinding.setKeyBindState(keybindD, false);
                        else
                            KeyBinding.setKeyBindState(keybindA, false);

                        //push keys so the next tick it will unleash
                        while (!pushedOff && !lastLaneDirection.equals(direction.NONE)) {
                            if (lastLaneDirection.equals(direction.LEFT)) {
                                Utils.addCustomLog("Bouncing to the right");
                                KeyBinding.setKeyBindState(keybindD, true);
                            } else {
                                Utils.addCustomLog("Bouncing to the left");
                                KeyBinding.setKeyBindState(keybindA, true);
                            }
                            pushedOff = true;
                        }
                        KeyBinding.setKeyBindState(keybindW, true);
                    }
                }


                //change to walk forward
                if (Utils.roundTo2DecimalPlaces(dx) == 0 && Utils.roundTo2DecimalPlaces(dz) == 0) {
                    if (shouldWalkForward() && !walkingForward && ((int) initialX != (int) mc.thePlayer.posX || (int) initialZ != (int) mc.thePlayer.posZ)) {
                        // updateKeybinds(true, false, false, false);
                        walkingForward = true;
                        walkForwardDis = 2.9f;
                        Utils.addCustomLog("Walking forward, walking dis = " + walkForwardDis);
                        pushedOff = false;
                        initialX = mc.thePlayer.posX;
                        initialZ = mc.thePlayer.posZ;
                    }
                }

                //chagnge back to left/right
                if ((Math.abs(initialX - mc.thePlayer.posX) > walkForwardDis || Math.abs(initialZ - mc.thePlayer.posZ) > walkForwardDis) && walkingForward) {

                    mc.thePlayer.sendChatMessage("/setspawn");
                    if (!Utils.isWalkable(Utils.getLeftBlock()) || !Utils.isWalkable(Utils.getBlockAround(-2, 0))) {
                        //set last lane dir
                        currentDirection = direction.RIGHT;
                        lastLaneDirection = direction.RIGHT;
                        updateKeys(false, false, false, true, false);
                    } else if (!Utils.isWalkable(Utils.getRightBlock()) || !Utils.isWalkable(Utils.getBlockAround(2, 0))) {
                        currentDirection = direction.LEFT;
                        lastLaneDirection = direction.LEFT;
                        updateKeys(false, false, true, false, false);
                    }

                    Utils.addCustomLog("Changing motion : Going " + currentDirection);
                    ScheduleRunnable(PressS, 200, TimeUnit.MILLISECONDS);
                    walkingForward = false;
                }
            }
            if(placingSc){

                if(!refillingSc) {
                    if (!Utils.hasSugarcaneInHotbar() || !Utils.hasSugarcaneInInv()) {
                        refillingSc = true;
                        updateKeys(false, false, false, false, false, false, false);
                        ExecuteRunnable(RefillSc);
                        return;
                    }
                    if (shouldEndDigging() && !(!Utils.isWalkable(Utils.getRightBlock()) && !Utils.isWalkable(Utils.getRightBlock()))) {
                        walkingForward = false;
                        Utils.addCustomMessage("Ended placing sugarcane");
                        disableScript();
                        updateKeys(false, false, false, false, false, false, false);
                        return;
                    }

                    Utils.hardRotate(playerYaw);
                    mc.thePlayer.rotationPitch = 46;
                    mc.thePlayer.inventory.currentItem = Utils.getFirstHotbarSlotWithSugarcane() - 36;


                    if (dy == 0) {
                        if (!walkingForward) { //normal

                            if(initialTime == 0 || TimeUnit.MILLISECONDS.convert(System.nanoTime() - initialTime, TimeUnit.NANOSECONDS) > 70 + Utils.nextInt(20)){
                                KeyBinding.onTick(keybindUseItem);
                                initialTime = System.nanoTime();
                            }
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

                            KeyBinding.setKeyBindState(keyBindShift, false);
                            if (currentDirection.equals(direction.RIGHT)) {
                                KeyBinding.setKeyBindState(keybindD, !canePlaceLag);
                                KeyBinding.setKeyBindState(keybindA, canePlaceLag);

                            }
                            else if (currentDirection.equals(direction.LEFT)) {
                                KeyBinding.setKeyBindState(keybindA, !canePlaceLag);
                                KeyBinding.setKeyBindState(keybindD, canePlaceLag);
                            }
                            else
                                walkingForward = true;


                        } else { // walking forward
                            KeyBinding.setKeyBindState(keyBindShift, false);
                            //unleash keys
                            if (lastLaneDirection.equals(direction.LEFT))
                                KeyBinding.setKeyBindState(keybindD, false);
                            else
                                KeyBinding.setKeyBindState(keybindA, false);
                            KeyBinding.setKeyBindState(keybindW, true);
                        }
                    }


                    //change to walk forward
                    if (Utils.roundTo2DecimalPlaces(dx) == 0 && Utils.roundTo2DecimalPlaces(dz) == 0) {
                        if (shouldWalkForward() && !walkingForward && ((int) initialX != (int) mc.thePlayer.posX || (int) initialZ != (int) mc.thePlayer.posZ)) {
                            // updateKeybinds(true, false, false, false);
                            walkingForward = true;
                            walkForwardDis = 2.9f;
                            Utils.addCustomLog("Walking forward, walking dis = " + walkForwardDis);
                            pushedOff = false;
                            initialX = mc.thePlayer.posX;
                            initialZ = mc.thePlayer.posZ;
                        }
                    }

                    //chagnge back to left/right
                    if ((Math.abs(initialX - mc.thePlayer.posX) > walkForwardDis || Math.abs(initialZ - mc.thePlayer.posZ) > walkForwardDis) && walkingForward) {

                        mc.thePlayer.sendChatMessage("/setspawn");
                        if (!Utils.isWalkable(Utils.getLeftBlock()) || !Utils.isWalkable(Utils.getBlockAround(-2, 0))) {
                            //set last lane dir
                            currentDirection = direction.RIGHT;
                            lastLaneDirection = direction.RIGHT;
                            updateKeys(false, false, false, true, false);
                        } else if (!Utils.isWalkable(Utils.getRightBlock()) || !Utils.isWalkable(Utils.getBlockAround(2, 0))) {
                            currentDirection = direction.LEFT;
                            lastLaneDirection = direction.LEFT;
                            updateKeys(false, false, true, false, false);
                        }

                        Utils.addCustomLog("Changing motion : Going " + currentDirection);
                        ScheduleRunnable(PressS, 200, TimeUnit.MILLISECONDS);
                        walkingForward = false;
                    }
                } else {
                    updateKeys(false, false, false, false, false, false, false);
                }
            }
        }

    }


    Runnable PressS = new Runnable() {
        @Override
        public void run() {

            if (walkingForward)
                return;
            try {
                do {
                    Utils.addCustomLog("Pressing S");
                    updateKeys(mc.gameSettings.keyBindForward.isKeyDown(), true, mc.gameSettings.keyBindLeft.isKeyDown(), mc.gameSettings.keyBindRight.isKeyDown(), false);
                    Thread.sleep(150);
                }
                while (Utils.isWalkable(Utils.getBackBlock()) && (!Utils.isWalkable(Utils.getFrontBlock()) || !Utils.isWalkable(Utils.getBlockAround(0, 2))));

                updateKeys(mc.gameSettings.keyBindForward.isKeyDown(), false, mc.gameSettings.keyBindLeft.isKeyDown(), mc.gameSettings.keyBindRight.isKeyDown(), false);
            } catch (Exception e) {
                e.printStackTrace();

            }

        }
    };

    Runnable InitializeDig = new Runnable() {
        @Override
        public void run() {
            try {
                if(!enabled)
                    return;
                Utils.addCustomLog("Initialize digging");
                updateKeys(false, false, false, false, false, false, false);
                Utils.hardRotate(playerYaw);
                Utils.smoothRotatePitchTo(60, 1);
                KeyBinding.onTick(keybindAttack);
                Thread.sleep(500);
                Utils.smoothRotatePitchTo(50, 1);
                KeyBinding.onTick(keybindAttack);
                Thread.sleep(500);
                Utils.smoothRotatePitchTo(30, 1);
                KeyBinding.onTick(keybindAttack);
                Thread.sleep(500);
                Utils.smoothRotatePitchTo(25, 1);
                KeyBinding.onTick(keybindAttack);
                Thread.sleep(300);
                KeyBinding.setKeyBindState(keybindW, true);
                Thread.sleep(200);
                KeyBinding.setKeyBindState(keybindW, false);
                Thread.sleep(100);
                Utils.addCustomLog("Pressing S");
                KeyBinding.setKeyBindState(keybindS, true);
                Thread.sleep(300);
                KeyBinding.setKeyBindState(keybindS, false);
                Thread.sleep(300);

                if(!Utils.isWalkable(Utils.getRightBlock()) && !Utils.isWalkable(Utils.getLeftBlock())) {
                    inDiggingTrench = true;
                    slowDig = false;
                    ScheduleRunnable(AlignInTrench, 1, TimeUnit.SECONDS);
                }
                else {
                    disableScript();
                    Utils.addCustomLog("Wrong location. Script disabled");
                }


            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    };
    Runnable AlignInTrench = () -> {
        try {
            if (goLeft) {
                Utils.addCustomLog("Pressing A, trying to align");
                KeyBinding.setKeyBindState(keybindA, true);
                Thread.sleep(100);
                KeyBinding.setKeyBindState(keybindA, false);
            } else {
                Utils.addCustomLog("Pressing D, trying to align");
                KeyBinding.setKeyBindState(keybindD, true);
                Thread.sleep(100);
                KeyBinding.setKeyBindState(keybindD, false);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    };
    Runnable SlowDig = new Runnable() {
        @Override
        public void run() {
            if(!enabled)
                return;
            try {
                inDiggingTrench = true;
                KeyBinding.setKeyBindState(keybindAttack, false);
                Thread.sleep(1000);
                while((Math.abs(getBorderBlock().getX() - Math.floor(mc.thePlayer.posX)) > 2 || Math.abs(getBorderBlock().getZ() - Math.floor(mc.thePlayer.posZ)) > 2) && enabled){
                    mc.thePlayer.rotationPitch = 60;
                    Utils.addCustomLog("Digging a block");
                    KeyBinding.onTick(keybindAttack);
                    Thread.sleep(1000);
                }
                ExecuteRunnable(GoToNextTrench);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    };
    Runnable GoToNextTrench = new Runnable() {
        @Override
        public void run() {
            try {
                if(!enabled)
                    return;
                if(shouldEndDiggingTrench()){
                    Utils.addCustomLog("Dig trench completed");
                    disableScript();
                    updateKeys(false, false, false, false, false, false, false);
                    return;
                }
                updateKeys(true, false, false, false, false, false, false);
                KeyBinding.setKeyBindState(keyBindJump, true);
                Thread.sleep(200);
                KeyBinding.setKeyBindState(keyBindJump, false);
                Thread.sleep(200);
                updateKeys(false, false, false, false, false, false, false);
                Thread.sleep(500);

                if (goLeft)
                    Utils.addCustomLog("Going left");
                else
                    Utils.addCustomLog("Going Right");

                if (mc.thePlayer.rotationYaw < 180)
                    Utils.smoothRotateClockwise(180, 2);
                else
                    Utils.smoothRotateAnticlockwise(180, 2);
                Thread.sleep(1000);
                BlockPos targetBlockPos;
                if (goLeft)
                    targetBlockPos = new BlockPos(Utils.getUnitZ() * -1 * -3 + mc.thePlayer.posX, mc.thePlayer.posY,
                            Utils.getUnitX() * -3 + mc.thePlayer.posZ);
                else
                    targetBlockPos = new BlockPos(Utils.getUnitZ() * -1 * 3 + mc.thePlayer.posX, mc.thePlayer.posY,
                            Utils.getUnitX() * 3 + mc.thePlayer.posZ);
                updateKeys(false, false, false, false, false, false, false);
                Utils.addCustomLog("target block : " + targetBlockPos);
                while ((Math.floor(mc.thePlayer.posX) != targetBlockPos.getX() || Math.floor(mc.thePlayer.posZ) != targetBlockPos.getZ()) && enabled) {
                    updateKeys(false, false, goLeft, !goLeft, false, false, true);
                    Thread.sleep(1);
                }
                Thread.sleep(50);
                updateKeys(false, false, false, false, false, false, true);
                // NEED ALIGNMENT
                // Utils.align();
                Thread.sleep(500);
                Utils.addCustomLog("Starting new row");
                goLeft = !goLeft;

                if (playerYaw < 180)
                    playerYaw += 180;
                else
                    playerYaw -= 180;
                updateKeys(false, false, false, false, false, false, false);

                ScheduleRunnable(InitializeDig, 1, TimeUnit.SECONDS);

            } catch(Exception e){
                e.printStackTrace();
            }

        }
    };
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
                    disableScript();
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
                    disableScript();
                }

            } catch(Exception e){
                e.printStackTrace();
            }

        }
    };




    direction calculateDirection() {
        ArrayList<Integer> unwalkableBlocks = new ArrayList<>();
        if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)).getBlock().equals(Blocks.end_portal_frame)) {
            for (int i = -3; i < 3; i++) {
                if (!Utils.isWalkable(Utils.getBlockAround(i, 0, 1))) {
                    unwalkableBlocks.add(i);
                }
            }
        } else {
            for (int i = -3; i < 3; i++) {
                if (!Utils.isWalkable(Utils.getBlockAround(i, 0))) {
                    unwalkableBlocks.add(i);
                }
            }
        }

        if (unwalkableBlocks.size() == 0)
            return direction.RIGHT;
        else if (unwalkableBlocks.size() > 1 && hasPosAndNeg(unwalkableBlocks)) {
            return direction.NONE;
        } else if (unwalkableBlocks.get(0) > 0)
            return direction.LEFT;
        else
            return direction.RIGHT;
    }
    boolean hasPosAndNeg(ArrayList<Integer> ar) {
        boolean hasPos = false;
        boolean hasNeg = false;
        for (Integer integer : ar) {
            if (integer < 0)
                hasNeg = true;
            else
                hasPos = true;
        }
        return hasPos && hasNeg;

    }
    boolean shouldWalkForward() {
        return (Utils.isWalkable(Utils.getBackBlock()) && Utils.isWalkable(Utils.getFrontBlock())) ||
                (!Utils.isWalkable(Utils.getBackBlock()) && !Utils.isWalkable(Utils.getLeftBlock())) ||
                (!Utils.isWalkable(Utils.getBackBlock()) && !Utils.isWalkable(Utils.getRightBlock())) ||
                (!Utils.isWalkable(Utils.getFrontBlock()) && !Utils.isWalkable(Utils.getRightBlock())) ||
                (!Utils.isWalkable(Utils.getFrontBlock()) && !Utils.isWalkable(Utils.getLeftBlock()));
    }
    void ScheduleRunnable(Runnable r, int delay, TimeUnit tu) {
        ScheduledExecutorService eTemp = Executors.newScheduledThreadPool(1);
        eTemp.schedule(r, delay, tu);
        eTemp.shutdown();
    }

    void ExecuteRunnable(Runnable r) {
        ScheduledExecutorService eTemp = Executors.newScheduledThreadPool(1);
        eTemp.execute(r);
        eTemp.shutdown();
    }
    BlockPos getBorderBlock(){
        double X = mc.thePlayer.posX;
        double Y = mc.thePlayer.posY;
        double Z = mc.thePlayer.posZ;
        for(int i = 0; i < 10; i++){
            if(Utils.getBlockAround(0, i, 0) != Blocks.air) {
                if (Utils.getBlockAround(0, i + 1, 0) == Blocks.air)
                    return new BlockPos(Utils.getUnitX() * i + X, Y - 1, Utils.getUnitZ() * i + Z);
            }
        }
        return null;
    }

    boolean shouldEndDigging(){
        for(int i = 5; i < 7; i++) {
            if(Utils.getBlockAround(0, i, -1).equals(Blocks.air))
                return true;
        }
        return false;

    }
    boolean shouldEndDiggingTrench(){
        if(onRightSideOfFarm()) {
            if(Utils.getBlockAround(-3, 0, 0).equals(Blocks.air) && !Utils.getBlockAround(-4, 0, 0).equals(Blocks.air)) {
                for (int i = 0; i < 5; i++) {
                    if (Utils.getBlockAround(i, 0, -1).equals(Blocks.air))
                        return true;
                }
            }
        } else {
            System.out.println(Utils.getBlockAround(-3, 0, 0));
            if(Utils.getBlockAround(3, 0, 0).equals(Blocks.air) && !Utils.getBlockAround(4, 0, 0).equals(Blocks.air)) {
                for (int i = 0; i > -5; i--) {
                    if (Utils.getBlockAround(i, 0, -1).equals(Blocks.air))
                        return true;
                }
            }
        }
        return false;

    }
    boolean blockLagged(direction playerGoingDir){
        if(playerGoingDir == direction.RIGHT) {
            return !(sugarcaneIsPresent(-3, 1)) || !(sugarcaneIsPresent(-3, 0)) ||
                    !(sugarcaneIsPresent(-2, 1)) || !(sugarcaneIsPresent(-2, 1));
        } else {
            return !(sugarcaneIsPresent(3, 1)) || !(sugarcaneIsPresent(3, 0)) ||
                    !(sugarcaneIsPresent(2, 1)) || !(sugarcaneIsPresent(2, 0));

           /* if(isWaterBlock(3, 2, -1)){
                if(isSugarcaneBlock(3, 1, 0)){
                    if(isWaterBlock(2, 2, -1)){
                        if(isSugarcaneBlock(2, 1, 0))
                            return false;
                        else return true;
                    }
                } else return true;

            } else if(isWaterBlock(2, 2, -1)){
                if(isSugarcaneBlock(2, 1, 0))
                    return false;
                else return true;
            }*/
        }
    }
    boolean sugarcaneIsPresent(int rightOffset, int frontOffset){
        if(isWaterBlock(rightOffset, frontOffset + 1, -1) || isWaterBlock(rightOffset, frontOffset - 1, -1)){
            return isSugarcaneBlock(rightOffset, frontOffset, 0);
        }
        return true;
    }
    boolean isSugarcaneBlock(int rightOffset, int frontOffset, int upOffset){
        return Utils.getBlockAround(rightOffset, frontOffset, upOffset).equals(Blocks.reeds);
    }
    boolean isWaterBlock(int rightOffset, int frontOffset, int upOffset){
        return Utils.getBlockAround(rightOffset, frontOffset, upOffset).equals(Blocks.water) || Utils.getBlockAround(rightOffset, frontOffset, upOffset).equals(Blocks.flowing_water);
    }
    boolean onRightSideOfFarm(){
        for (int i = 0; i < 10; i++) {
            if (Utils.isWalkable(Utils.getBlockAround(i, 0, -1))) {
                return true;
            }
        }
        return false;
    }
    void initVar(){

        currentDirection = calculateDirection();
        lastLaneDirection = calculateDirection();
        walkingForward = false;
        walkForwardDis = 2.9f;
        initialX = mc.thePlayer.posX;
        initialZ = mc.thePlayer.posZ;
        pushedOff = false;
        slowDig = false;
        inDiggingTrench = false;
        canePlaceLag = false;
        fixLag = false;
        if(onRightSideOfFarm())
            goLeft = false;
        else
            goLeft = true;
        playerYaw = Math.round(Utils.get360RotationYaw() / 90) < 4 ? Math.round(Utils.get360RotationYaw() / 90) * 90 : 0;
    }


    public void updateKeys(boolean wBool, boolean sBool, boolean aBool, boolean dBool, boolean atkBool,  boolean useBool, boolean shiftBool) {
        KeyBinding.setKeyBindState(keybindW, wBool);
        KeyBinding.setKeyBindState(keybindS, sBool);
        KeyBinding.setKeyBindState(keybindA, aBool);
        KeyBinding.setKeyBindState(keybindD, dBool);
        KeyBinding.setKeyBindState(keybindAttack, atkBool);
        KeyBinding.setKeyBindState(keybindUseItem, useBool);
        KeyBinding.setKeyBindState(keyBindShift, shiftBool);
    }
    void updateKeys(boolean wBool, boolean sBool, boolean aBool, boolean dBool, boolean atkBool) {
        KeyBinding.setKeyBindState(keybindW, wBool);
        KeyBinding.setKeyBindState(keybindS, sBool);
        KeyBinding.setKeyBindState(keybindA, aBool);
        KeyBinding.setKeyBindState(keybindD, dBool);
        KeyBinding.setKeyBindState(keybindAttack, atkBool);
    }
    void clickWindow(int windowID, int slotID, int mouseButtonClicked, int mode) throws Exception{

        if(mc.thePlayer.openContainer instanceof ContainerChest || mc.currentScreen instanceof  GuiInventory) {
            mc.playerController.windowClick(windowID, slotID, mouseButtonClicked, mode, mc.thePlayer);
            Utils.addCustomLog("Pressing slot : " + slotID);
        }
        else {
            Utils.addCustomMessage(EnumChatFormatting.RED + "Didn't open window! This shouldn't happen and the script has been disabled. Please immediately report to the developer.");
            updateKeys(false, false, false, false, false, false, false);
            disableScript();
            throw new Exception();

        }
    }
    void disableScript(){
        enabled = false;
        diggingTrench = false;
        diggingPath = false;
        placingSc = false;
        refillingSc = false;
        Utils.addCustomMessage("Disabling script");
        updateKeys(false, false, false, false, false, false, false);
    }


}

