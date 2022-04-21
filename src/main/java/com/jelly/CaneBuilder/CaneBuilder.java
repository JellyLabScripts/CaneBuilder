package com.jelly.CaneBuilder;

import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
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
    static boolean diggingPath;
    static boolean diggingTrench;


    // for digging part
    static boolean walkingForward;
    static double initialX = 0;
    static double initialZ = 0;
    static float walkForwardDis;
    static boolean pushedOff;


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
    public void onTickPlayer(TickEvent.ClientTickEvent tickEvent){
        if(tickEvent.phase != TickEvent.Phase.START)
            return;

        if(mc.thePlayer != null && mc.theWorld != null && enabled){
            double dx = Math.abs(mc.thePlayer.posX - mc.thePlayer.lastTickPosX);
            double dy = Math.abs(mc.thePlayer.posY - mc.thePlayer.lastTickPosY);
            double dz = Math.abs(mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ);
            Utils.hardRotate(90);
            mc.thePlayer.rotationPitch = 11;
            mc.gameSettings.gammaSetting = 100;
            Block blockStandingOn = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)).getBlock();
            if (mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiIngameMenu) {
                updateKeys(false, false, false, false, false, false, false);
                return;
            }
            if(shouldEndDigging() && !(!Utils.isWalkable(Utils.getRightBlock()) && !Utils.isWalkable(Utils.getRightBlock()))){
                walkingForward = false;
                Utils.addCustomMessage("Ended process");
                enabled = false;
                updateKeys(false, false, false, false, false, false, false);
                return;
            }
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

                    //hole drop fix (prevent sneaking at the hole)
                    KeyBinding.setKeyBindState(keyBindShift, !Utils.isWalkable(blockStandingOn));


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

    }
    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event){
        if(Keyboard.isKeyDown(Keyboard.KEY_P)){
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
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_F)){
            if(!enabled){
                Utils.addCustomMessage("Enabling script");
                updateKeys(false, false, false, false, false, false, false);
                initVar();
                enabled = true;
                diggingTrench = true;
            } else {
                Utils.addCustomMessage("Disabling script");
                updateKeys(false, false, false, false, false, false, false);
                enabled = false;
            }
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_G)){
            if(!enabled){
                Utils.addCustomMessage("Enabling script");
                updateKeys(false, false, false, false, false, false, false);
                initVar();
                enabled = true;
                diggingPath = true;
            }
        }
    }
    void updateKeys(boolean wBool, boolean sBool, boolean aBool, boolean dBool, boolean atkBool,  boolean useBool, boolean shiftBool) {
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

    Runnable PressS = new Runnable() {
        @Override
        public void run() {

            if (walkingForward)
                return;
            try {
                do {
                    Utils.addCustomLog("Pressing S");
                    updateKeys(mc.gameSettings.keyBindForward.isKeyDown(), true, mc.gameSettings.keyBindLeft.isKeyDown(), mc.gameSettings.keyBindRight.isKeyDown(), true);
                    Thread.sleep(50);
                }
                while (Utils.isWalkable(Utils.getBackBlock()) && (!Utils.isWalkable(Utils.getFrontBlock()) || !Utils.isWalkable(Utils.getBlockAround(0, 2))));

                updateKeys(mc.gameSettings.keyBindForward.isKeyDown(), false, mc.gameSettings.keyBindLeft.isKeyDown(), mc.gameSettings.keyBindRight.isKeyDown(), true);


            } catch (Exception e) {
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

    boolean shouldEndDigging(){
        for(int i = 5; i < 7; i++) {
            if(Utils.getBlockAround(0, i, -1).equals(Blocks.air))
                return true;
        }
        return false;

    }
    void initVar(){
        currentDirection = calculateDirection();
        lastLaneDirection = calculateDirection();
        walkingForward = false;
        walkForwardDis = 2.9f;
        pushedOff = false;
    }
}
