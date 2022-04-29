package com.jelly.CaneBuilder;

import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.BlockUtils;
import com.jelly.CaneBuilder.utils.Utils;
import net.java.games.input.Mouse;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MouseHelper;
import net.minecraft.world.biome.BiomeCache;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.lwjgl.input.Keyboard;
import scala.tools.nsc.Global;

import java.lang.reflect.Field;
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


    static KeyBinding[] customKeyBinds = new KeyBinding[6];
    //states
    static boolean enabled = false;
    static boolean diggingPath = false;
    static boolean diggingTrench = false;
    static boolean goLeft = false;
    static boolean inFailsafe;
    static boolean error;
    //For place layer
    static boolean placeBlock1;
    static boolean placeBlock2;
    // for digging trench path
    static boolean slowDig;
    static boolean inDiggingTrench;
    // for digging path part
    static boolean walkingForward;
    static double initialX = 0;
    static double initialZ = 0;
    static float walkForwardDis;
    static boolean pushedOff;
    public static BlockPos targetBlockPos= new BlockPos(10000, 10000, 10000);
    //for placing sugarcane
    static boolean placingSc;
    static boolean refillingSc;
    static boolean canePlaceLag;
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

    MouseHelper mouseHelper = new MouseHelper();

    enum direction {
        RIGHT,
        LEFT,
        NONE
    }

    direction currentDirection;
    direction lastLaneDirection;

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

        customKeyBinds[0] = new KeyBinding("Set corner", Keyboard.KEY_P, "CaneBuilder");
        customKeyBinds[1] = new KeyBinding("Enable auto place dirt layer", Keyboard.KEY_F, "CaneBuilder");
        customKeyBinds[2] = new KeyBinding("Enable dig trench", Keyboard.KEY_G, "CaneBuilder");
        customKeyBinds[3] = new KeyBinding("Enable dig path", Keyboard.KEY_H, "CaneBuilder");
        customKeyBinds[4] = new KeyBinding("Enable auto place crops", Keyboard.KEY_J, "CaneBuilder");
        customKeyBinds[5] = new KeyBinding("Disable script", Keyboard.KEY_Z, "CaneBuilder");

        for (int i = 0; i < customKeyBinds.length; i++) {
            ClientRegistry.registerKeyBinding(customKeyBinds[i]);
        }

        MinecraftForge.EVENT_BUS.register(new CaneBuilder());
    }
    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event){
        if(customKeyBinds[0].isKeyDown()){
            if(setmode == 0) {

                corner1x = (int)mc.thePlayer.posX;
                corner1y = (int)mc.thePlayer.posY - 1;
                corner1z = (int)mc.thePlayer.posZ;
                Utils.addCustomMessage("Set 1st corner (X=" + corner1x + " Y=" + corner1y + " Z=" + corner1z + ")");
            }
            else {
                corner2x = (int)mc.thePlayer.posX;
                corner2y = (int)mc.thePlayer.posY - 1;
                corner2z = (int)mc.thePlayer.posZ;
                Utils.addCustomMessage("Set 2nd corner (X=" + corner2x + " Y=" + corner2y + " Z=" + corner2z + ")");
            }
            setmode = 1 - setmode;
        }
        if(customKeyBinds[1].isKeyDown()){
            if(!enabled){
                if(corner1x == 0 && corner1z == 0 && corner2x == 0 && corner2z == 0){
                    Utils.addCustomMessage("Set the corners");
                } else {
                    if ((int) mc.thePlayer.posX == corner1x && (int) mc.thePlayer.posZ == corner1z) {
                        Utils.addCustomMessage("Enabling script (Placing dirt layer)");
                        updateKeys(false, false, false, false, false, false, false);
                        initVar();
                        enabled = true;
                        placeBlock1 = true;
                    } else
                        Utils.addCustomMessage("Stand on 1st corner to start! (X=" + corner1x + " Y=" + corner1y + " Z=" + corner1z + ")");
                }
            }
        }

        if(customKeyBinds[2].isKeyDown()){
            if(!enabled){
                Utils.addCustomMessage("Enabling script (Digging trench)");
                updateKeys(false, false, false, false, false, false, false);
                initVar();
                enabled = true;
                diggingTrench = true;
                ExecuteRunnable(InitializeDig);
            }
        }
        if(customKeyBinds[3].isKeyDown()){
            if(!enabled){
                Utils.addCustomMessage("Enabling script (Digging path)");
                updateKeys(false, false, false, false, false, false, false);
                initVar();
                enabled = true;
                diggingPath = true;
            }
        }
        if(customKeyBinds[4].isKeyDown()){
            if(!enabled){
                Utils.addCustomMessage("Enabling script (Placing sugarcane)");
                updateKeys(false, false, false, false, false, false, false);
                initVar();
                enabled = true;
                placingSc = true;
            }

        }
        if(customKeyBinds[5].isKeyDown()){
            if(enabled)
                disableScript();
        }

    }
    @SubscribeEvent
    public void onMessageReceived(ClientChatReceivedEvent event) {

        if (event.message.getFormattedText().contains("You were spawned in Limbo") && !inFailsafe && enabled) {
            activateFailsafe();
            ScheduleRunnable(LeaveSBIsand, 8, TimeUnit.SECONDS);
        }
        if ((event.message.getFormattedText().contains("Sending to server") && !inFailsafe && enabled)) {
            activateFailsafe();
            ScheduleRunnable(WarpHome, 10, TimeUnit.SECONDS);
        }
        if ((event.message.getFormattedText().contains("DYNAMIC") || (event.message.getFormattedText().contains("Couldn't warp you")) && inFailsafe)) {
            error = true;
        }
        if ((event.message.getFormattedText().contains("SkyBlock Lobby") && !inFailsafe && enabled)) {
            activateFailsafe();
            ScheduleRunnable(LeaveSBIsand, 10, TimeUnit.SECONDS);
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
            Block blockIn = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)).getBlock();
            BlockPos blockInPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            if(!placingSc) {
                if (mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiIngameMenu) {
                    updateKeys(false, false, false, false, false, false, false);
                    return;
                }
            }
            if(inFailsafe)
                return;

            if (placeBlock1) {
                mc.thePlayer.inventory.currentItem = 1;
                AngleUtils.hardRotate(corner2z - corner1z > 0 ? 180 : 0);
                mc.thePlayer.rotationPitch = 82;

                KeyBinding.setKeyBindState(keybindUseItem, dx == 0 && dz == 0);
                KeyBinding.setKeyBindState(keybindS, true);
                KeyBinding.setKeyBindState(keyBindShift, true);

                if ((int) mc.thePlayer.posZ == corner2z && blockStandingOn != Blocks.air) {
                    placeBlock1 = false;
                    placeBlock2 = true;
                    KeyBinding.setKeyBindState(keybindUseItem, false);
                    KeyBinding.setKeyBindState(keybindS, false);
                    KeyBinding.setKeyBindState(keyBindShift, false);
                }
            } else if (placeBlock2) {
                mc.thePlayer.inventory.currentItem = 0;
                AngleUtils.hardRotate(corner2x - corner1x > 0 ? 90 : -90);
                mc.thePlayer.rotationPitch = 82;
                KeyBinding.setKeyBindState(keybindUseItem, dx == 0 && dz == 0);
                KeyBinding.setKeyBindState(keybindS, true);
                KeyBinding.setKeyBindState(keyBindShift, true);

                if ((int) mc.thePlayer.posX == corner2x && blockStandingOn != Blocks.air) {

                    disableScript();
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
                        AngleUtils.hardRotate(playerYaw);
                        mc.thePlayer.rotationPitch = 20;
                        updateKeys(true, false, mc.gameSettings.keyBindLeft.isKeyDown(), mc.gameSettings.keyBindRight.isKeyDown(), true, false, false);
                    }
                }
            }
            if(diggingPath) {
                if(shouldEndDigging() && !(!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getRightBlock()))){
                    walkingForward = false;
                    Utils.addCustomMessage("Ended process");
                    disableScript();
                    updateKeys(false, false, false, false, false, false, false);
                    return;
                }
                mc.gameSettings.pauseOnLostFocus = false;
                mc.gameSettings.gammaSetting = 100;
                //angles (locked)
                if (!inFailsafe) {
                    mc.thePlayer.rotationPitch = 11;
                    AngleUtils.hardRotate(playerYaw);
                    KeyBinding.setKeyBindState(keybindAttack, true);
                }
                //states
                if (dy == 0 && !inFailsafe) {
                    if (!walkingForward) { //normal

                        KeyBinding.setKeyBindState(keyBindShift, false);
                        if (currentDirection.equals(direction.RIGHT))
                            KeyBinding.setKeyBindState(keybindD, true);
                        else if (currentDirection.equals(direction.LEFT))
                            KeyBinding.setKeyBindState(keybindA, true);
                        else
                            walkingForward = true;
                    } else { // walking forward

                        //hole drop fix (prevent sneaking at the hole)
                        KeyBinding.setKeyBindState(keyBindShift, !BlockUtils.isWalkable(blockStandingOn));

                        //unleash keys
                        if (lastLaneDirection.equals(direction.LEFT))
                            updateKeys(mc.gameSettings.keyBindForward.isKeyDown(), mc.gameSettings.keyBindBack.isKeyDown(), mc.gameSettings.keyBindLeft.isKeyDown(), false, false);
                        else
                            updateKeys(mc.gameSettings.keyBindForward.isKeyDown(), mc.gameSettings.keyBindBack.isKeyDown(), false, mc.gameSettings.keyBindRight.isKeyDown(), false);

                        //push keys so the next tick it will unleash
                        while (!pushedOff && !lastLaneDirection.equals(direction.NONE)) {
                            if (lastLaneDirection.equals(direction.LEFT)) {
                                Utils.addCustomLog("Bouncing to the right");
                                updateKeys(mc.gameSettings.keyBindForward.isKeyDown(), mc.gameSettings.keyBindBack.isKeyDown(), mc.gameSettings.keyBindLeft.isKeyDown(), true, false);
                            } else {
                                Utils.addCustomLog("Bouncing to the left");
                                updateKeys(mc.gameSettings.keyBindForward.isKeyDown(), mc.gameSettings.keyBindBack.isKeyDown(), true, mc.gameSettings.keyBindRight.isKeyDown(), false);
                            }
                            pushedOff = true;
                        }
                        KeyBinding.setKeyBindState(keybindW, true);
                    }
                }


                //change to walk forward
                if (Utils.roundTo2DecimalPlaces(dx) == 0 && Utils.roundTo2DecimalPlaces(dz) == 0 && !inFailsafe) {
                    if (shouldWalkForward() && !walkingForward && ((int) initialX != (int) mc.thePlayer.posX || (int) initialZ != (int) mc.thePlayer.posZ)) {
                        updateKeys(true, false, false, false, false);
                        walkingForward = true;
                        targetBlockPos = calculateTargetBlockPos();
                        Utils.addCustomLog("Target block : " + targetBlockPos.toString());
                        pushedOff = false;
                        initialX = mc.thePlayer.posX;
                        initialZ = mc.thePlayer.posZ;
                    }
                }

                //chagnge back to left/right
                if (blockInPos.getX() == targetBlockPos.getX() && blockInPos.getZ() == targetBlockPos.getZ() && walkingForward ) {//&& BlockUtils.isInCenterOfBlock()
                    mc.thePlayer.sendChatMessage("/setspawn");
                    updateKeys(false, false, false, false, true);

                    initialX = mc.thePlayer.posX;
                    initialZ = mc.thePlayer.posZ;

                    if (!BlockUtils.isWalkable(BlockUtils.getLeftBlock()) || !BlockUtils.isWalkable(BlockUtils.getBlockAround(-2, 0))) {
                        //set last lane dir
                        currentDirection = direction.RIGHT;
                        lastLaneDirection = direction.RIGHT;
                    } else if (!BlockUtils.isWalkable(BlockUtils.getRightBlock()) || !BlockUtils.isWalkable(BlockUtils.getBlockAround(2, 0))) {
                        currentDirection = direction.LEFT;
                        lastLaneDirection = direction.LEFT;
                    }
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
                    if (shouldEndDigging() && !(!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getRightBlock()))) {
                        walkingForward = false;
                        Utils.addCustomMessage("Ended placing sugarcane");
                        disableScript();
                        updateKeys(false, false, false, false, false, false, false);
                        return;
                    }

                    AngleUtils.hardRotate(playerYaw);
                    mc.thePlayer.rotationPitch = 46;
                    mc.thePlayer.inventory.currentItem = Utils.getFirstHotbarSlotWithSugarcane() - 36;


                    if (dy == 0) {
                        if (!walkingForward) { //normal

                            KeyBinding.setKeyBindState(keybindUseItem, true);
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
                            KeyBinding.setKeyBindState(keybindUseItem, false);
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

                        KeyBinding.setKeyBindState(keybindUseItem, false);
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
                while (BlockUtils.isWalkable(BlockUtils.getBackBlock()) && (!BlockUtils.isWalkable(BlockUtils.getFrontBlock()) || !BlockUtils.isWalkable(BlockUtils.getBlockAround(0, 2))));

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
                AngleUtils.hardRotate(playerYaw);
                AngleUtils.smoothRotatePitchTo(60, 1);
                KeyBinding.onTick(keybindAttack);
                Thread.sleep(500);
                AngleUtils.smoothRotatePitchTo(50, 1);
                KeyBinding.onTick(keybindAttack);
                Thread.sleep(500);
                AngleUtils.smoothRotatePitchTo(30, 1);
                KeyBinding.onTick(keybindAttack);
                Thread.sleep(500);
                AngleUtils.smoothRotatePitchTo(25, 1);
                KeyBinding.onTick(keybindAttack);
                Thread.sleep(300);
                KeyBinding.setKeyBindState(keybindW, true);
                Thread.sleep(300);
                KeyBinding.setKeyBindState(keybindW, false);
                Thread.sleep(100);
                Utils.addCustomLog("Pressing S");
                KeyBinding.setKeyBindState(keybindS, true);
                Thread.sleep(300);
                KeyBinding.setKeyBindState(keybindS, false);
                Thread.sleep(300);

                if(!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())) {
                    inDiggingTrench = true;
                    slowDig = false;
                    return;
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
                    AngleUtils.smoothRotateClockwise(180, 2);
                else
                    AngleUtils.smoothRotateAnticlockwise(180, 2);
                Thread.sleep(1000);
                BlockPos targetBlockPos;
                if (goLeft)
                    targetBlockPos = new BlockPos(BlockUtils.getUnitZ() * -1 * -3 + mc.thePlayer.posX, mc.thePlayer.posY,
                            BlockUtils.getUnitX() * -3 + mc.thePlayer.posZ);
                else
                    targetBlockPos = new BlockPos(BlockUtils.getUnitZ() * -1 * 3 + mc.thePlayer.posX, mc.thePlayer.posY,
                            BlockUtils.getUnitX() * 3 + mc.thePlayer.posZ);
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
                return;

            } catch(Exception e){
                e.printStackTrace();
            }

        }
    };
    /*Runnable goToNextWaterTrench = new Runnable() {
        @Override
        public void run() {
            try {
                if(!enabled)
                    return;

                BlockPos targetBlockPos;
                if (goLeft)
                    targetBlockPos = new BlockPos(BlockUtils.getUnitZ() * -1 * -3 + mc.thePlayer.posX, mc.thePlayer.posY,
                            BlockUtils.getUnitX() * -3 + mc.thePlayer.posZ);
                else
                    targetBlockPos = new BlockPos(BlockUtils.getUnitZ() * -1 * 3 + mc.thePlayer.posX, mc.thePlayer.posY,
                            BlockUtils.getUnitX() * 3 + mc.thePlayer.posZ);
                updateKeys(false, false, false, false, false, false, false);
                Utils.addCustomLog("target block : " + targetBlockPos);
                while ((Math.floor(mc.thePlayer.posX) != targetBlockPos.getX() || Math.floor(mc.thePlayer.posZ) != targetBlockPos.getZ()) && enabled) {
                    updateKeys(false, false, goLeft, !goLeft, false, false, true);
                    Thread.sleep(1);
                }
                Thread.sleep(50);
                updateKeys(false, false, false, false, false, false, true);
                ExecuteRunnable(placePrismapumpAndWater);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Runnable placePrismapumpAndWater = new Runnable() {
        @Override
        public void run() {
            try {
                if(!enabled)
                    return;
                mc.thePlayer.inventory.currentItem = 0;
                AngleUtils.hardRotate(playerYaw);
                AngleUtils.smoothRotatePitchTo(70, 1.2f);
                KeyBinding.onTick(keybindUseItem);
                Thread.sleep(500);
                mc.thePlayer.inventory.currentItem = 1;
                AngleUtils.smoothRotateTo(playerYaw + (goLeft? -22 : 22), 1.2f);
                AngleUtils.smoothRotatePitchTo(43, 1.2f);
                Thread.sleep(1000);
                KeyBinding.onTick(keybindUseItem);
                Thread.sleep(500);
                mc.thePlayer.inventory.currentItem = 2;
                AngleUtils.hardRotate(playerYaw);
                AngleUtils.smoothRotatePitchTo(70, 1.2f);
                Thread.sleep(1000);
                KeyBinding.setKeyBindState(keybindAttack, true);
                Thread.sleep(500);
                KeyBinding.setKeyBindState(keybindAttack, false);
                mc.thePlayer.inventory.currentItem = 0;
                if(shouldEndPlacingWater()){
                    Utils.addCustomLog("Place water completed");
                    disableScript();
                    updateKeys(false, false, false, false, false, false, false);
                    return;
                }
                ExecuteRunnable(goToNextWaterTrench);


            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };*/


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
                    disableScript();
                }

            } catch(Exception e){
                e.printStackTrace();
            }

        }
    };
    Runnable LeaveSBIsand = new Runnable() {
        @Override
        public void run() {
            mc.thePlayer.sendChatMessage("/l");
            ScheduleRunnable(Rejoin, 5, TimeUnit.SECONDS);
        }
    };
    Runnable WarpHub = new Runnable() {
        @Override
        public void run() {
            mc.thePlayer.sendChatMessage("/warp hub");
            ScheduleRunnable(WarpHome, 5, TimeUnit.SECONDS);
        }
    };

    Runnable Rejoin = new Runnable() {
        @Override
        public void run() {
            mc.thePlayer.sendChatMessage("/play sb");
            ScheduleRunnable(WarpHome, 5, TimeUnit.SECONDS);
        }
    };

    Runnable WarpHome = new Runnable() {
        @Override
        public void run() {
            mc.thePlayer.sendChatMessage("/warp home");
            ScheduleRunnable(afterRejoin1, 3, TimeUnit.SECONDS);
        }
    };


    Runnable afterRejoin1 = new Runnable() {
        @Override
        public void run() {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            if (!error) {
                ScheduleRunnable(afterRejoin2, 1, TimeUnit.SECONDS);
            } else {
                Utils.addCustomLog("Error detected. Waiting 20 seconds");
                ScheduleRunnable(WarpHome, 20, TimeUnit.SECONDS);
                error = false;
            }

        }
    };
    Runnable afterRejoin2 = () -> {

        KeyBinding.setKeyBindState(keyBindShift, false);


        mc.inGameHasFocus = true;
        mouseHelper.grabMouseCursor();
        mc.displayGuiScreen((GuiScreen) null);
        Field f = null;
        f = FieldUtils.getDeclaredField(mc.getClass(), "leftClickCounter", true);
        try {
            f.set(mc, 10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initVar();
        inFailsafe = false;
        enabled = true;
    };




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
        return (BlockUtils.isWalkable(BlockUtils.getBackBlock()) && BlockUtils.isWalkable(BlockUtils.getFrontBlock())) ||
                (!BlockUtils.isWalkable(BlockUtils.getBackBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())) ||
                (!BlockUtils.isWalkable(BlockUtils.getBackBlock()) && !BlockUtils.isWalkable(BlockUtils.getRightBlock())) ||
                (!BlockUtils.isWalkable(BlockUtils.getFrontBlock()) && !BlockUtils.isWalkable(BlockUtils.getRightBlock())) ||
                (!BlockUtils.isWalkable(BlockUtils.getFrontBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())) ||
                (!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock()));
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
            if(BlockUtils.getBlockAround(0, i, 0) != Blocks.air) {
                if (BlockUtils.getBlockAround(0, i + 1, 0) == Blocks.air)
                    return new BlockPos(BlockUtils.getUnitX() * i + X, Y - 1, BlockUtils.getUnitZ() * i + Z);
            }
        }
        return null;
    }

    boolean shouldEndDigging(){
        for(int i = 5; i < 7; i++) {
            if(BlockUtils.getBlockAround(0, i, -1).equals(Blocks.air))
                return true;
        }
        return false;

    }
    boolean shouldEndDiggingTrench(){
        if(onRightSideOfFarm()) {
            if(BlockUtils.getBlockAround(-3, 0, 0).equals(Blocks.air) && !BlockUtils.getBlockAround(-4, 0, 0).equals(Blocks.air)) {
                for (int i = 0; i < 5; i++) {
                    if (BlockUtils.getBlockAround(i, 0, -1).equals(Blocks.air))
                        return true;
                }
            }
        } else {
            if(BlockUtils.getBlockAround(3, 0, 0).equals(Blocks.air) && !BlockUtils.getBlockAround(4, 0, 0).equals(Blocks.air)) {
                for (int i = 0; i > -5; i--) {
                    if (BlockUtils.getBlockAround(i, 0, -1).equals(Blocks.air))
                        return true;
                }
            }
        }
        return false;

    }
    boolean shouldEndPlacingWater(){
        if(onRightSideOfFarm()) {

            if(BlockUtils.isWalkable(BlockUtils.getBlockAround(-3, 1, -1)) && !BlockUtils.isWalkable(BlockUtils.getBlockAround(-4, 1, -1))) {
                for (int i = 0; i < 5; i++) {
                    if (BlockUtils.getBlockAround(i, 0, -1).equals(Blocks.air))
                        return true;
                }
            }
        } else {
            if(BlockUtils.isWalkable(BlockUtils.getBlockAround(3, 1, -1)) && !BlockUtils.isWalkable(BlockUtils.getBlockAround(4, 1, -1))) {
                for (int i = 0; i > -5; i--) {
                    if (BlockUtils.getBlockAround(i, 0, -1).equals(Blocks.air))
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
        }
    }
    boolean sugarcaneIsPresent(int rightOffset, int frontOffset){
        if(isWaterBlock(rightOffset, frontOffset + 1, -1) || isWaterBlock(rightOffset, frontOffset - 1, -1)){
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
    boolean onRightSideOfFarm(){
        for (int i = 0; i < 10; i++) {
            if (BlockUtils.isWalkable(BlockUtils.getBlockAround(i, 0, -1))) {
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
        inFailsafe = false;
        error = false;
        goLeft = !onRightSideOfFarm();
        playerYaw = Math.round(AngleUtils.get360RotationYaw() / 90) < 4 ? Math.round(AngleUtils.get360RotationYaw() / 90) * 90 : 0;
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
    BlockPos calculateTargetBlockPos(){
        if(!BlockUtils.isWalkable(BlockUtils.getRightBlock()) || !BlockUtils.isWalkable(BlockUtils.getLeftBlock())){
            if(!BlockUtils.isWalkable(BlockUtils.getRightBlock()) && !BlockUtils.isWalkable(BlockUtils.getLeftBlock())){
                return BlockUtils.getBlockPosAround(0, 1, 0);
            } else {
                if(!BlockUtils.isWalkable(BlockUtils.getBlockAround(-1, 1, 0)) && !BlockUtils.isWalkable(BlockUtils.getBlockAround(1, 1, 0))) {
                    Utils.addCustomLog("Detected one block off");
                    return BlockUtils.getBlockPosAround(0, 2, 0);
                }
                else {
                    return BlockUtils.getBlockPosAround(0, 3, 0);
                }

            }
        }

        Utils.addCustomLog("can't calculate target block!");
        return new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);


    }
    void disableScript(){
        enabled = false;
        diggingTrench = false;
        diggingPath = false;
        placingSc = false;
        refillingSc = false;
        placeBlock1 = false;
        placeBlock2 = false;
        Utils.addCustomMessage("Disabling script");
        updateKeys(false, false, false, false, false, false, false);
    }
    void activateFailsafe() {
        inFailsafe = true;
        enabled = false;
        walkingForward = false;
        updateKeys(false, false, false, false, false, false, false);
    }


}

