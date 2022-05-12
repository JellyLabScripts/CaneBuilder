package com.jelly.CaneBuilder;

import com.jelly.CaneBuilder.processes.*;
import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.BlockUtils;
import com.jelly.CaneBuilder.utils.Utils;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.jelly.CaneBuilder.utils.Utils.regrabMouse;
import static com.jelly.CaneBuilder.utils.Utils.ungrabMouse;

@Mod(modid = CaneBuilder.MODID, version = CaneBuilder.VERSION)
public class CaneBuilder {
    public static final String MODID = "canebuilder";
    public static final String NAME = "Cane Builder";
    public static final String VERSION = "1.0";

    public static List<ProcessModule> processes = new ArrayList<>();


    static Minecraft mc = Minecraft.getMinecraft();

    static int setmode = 0;
    public static int corner1x = 0;
    public static int corner1y = 0;
    public static int corner1z = 0;
    public static int corner2x = 0;
    public static int corner2y = 0;
    public static int corner2z = 0;


    static KeyBinding[] customKeyBinds = new KeyBinding[7];
    //states
    static boolean enabled = false;
    static boolean diggingPath = false;
    static boolean diggingTrench = false;
    static boolean goLeft = false;
    static boolean inFailsafe;
    static boolean error;
    // for digging trench path
    static boolean slowDig;
    static boolean inDiggingTrench;
    // for digging path part

    //for placing sugarcane

    //autoclicker
    static long initialTime = 0;

    /*
     ** @author JellyLab, Polycrylate
     */


    public volatile static int playerYaw;
    public static int keybindA = mc.gameSettings.keyBindLeft.getKeyCode();
    public static int keybindD = mc.gameSettings.keyBindRight.getKeyCode();
    public static int keybindW = mc.gameSettings.keyBindForward.getKeyCode();
    public static int keybindS = mc.gameSettings.keyBindBack.getKeyCode();
    public static int keybindAttack = mc.gameSettings.keyBindAttack.getKeyCode();
    public static int keybindUseItem = mc.gameSettings.keyBindUseItem.getKeyCode();
    public static int keyBindSpace = mc.gameSettings.keyBindJump.getKeyCode();
    public static int keyBindShift = mc.gameSettings.keyBindSneak.getKeyCode();
    public static int keyBindJump = mc.gameSettings.keyBindJump.getKeyCode();

    MouseHelper mouseHelper = new MouseHelper();

    enum direction {
        RIGHT,
        LEFT,
        NONE
    }

    // 0 -> builder's wand 1 -> infinite dirt wand 2-> shovel 3 ->prismapump 4-> water bucket 5-> pickaxe
    direction currentDirection;
    direction lastLaneDirection;

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

        customKeyBinds[0] = new KeyBinding("Set corner", Keyboard.KEY_P, "CaneBuilder");
        customKeyBinds[1] = new KeyBinding("Enable full script", Keyboard.KEY_F, "CaneBuilder");
        customKeyBinds[2] = new KeyBinding("Enable dig trench", Keyboard.KEY_G, "CaneBuilder");
        customKeyBinds[3] = new KeyBinding("Enable fill trench", Keyboard.KEY_H, "CaneBuilder");
        customKeyBinds[4] = new KeyBinding("Enable dig path", Keyboard.KEY_J, "CaneBuilder");
        customKeyBinds[5] = new KeyBinding("Enable auto place crops", Keyboard.KEY_K, "CaneBuilder");
        customKeyBinds[6] = new KeyBinding("Disable script", Keyboard.KEY_Z, "CaneBuilder");

        for (KeyBinding customKeyBind : customKeyBinds) {
            ClientRegistry.registerKeyBinding(customKeyBind);
        }

        MinecraftForge.EVENT_BUS.register(new CaneBuilder());

        processes.add(new PlaceDirt1());
        processes.add(new PlaceDirt2());
        processes.add(new PlaceDirt3());
        processes.add(new PlaceDirt4());
        processes.add(new DigTrench());
        processes.add(new FillTrench());
        processes.add(new PlaceDirt5());
        processes.add(new PlaceDirt6());
        processes.add(new DigPath1());
        processes.add(new DigPath2());
        processes.add(new PlaceSC());
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
                        for(ProcessModule process : processes){
                            if(process instanceof PlaceDirt1) {
                                process.toggle();
                                process.onEnable();
                                enabled = true;
                            }
                        }
                    } else
                        Utils.addCustomMessage("Stand on 1st corner to start! (X=" + corner1x + " Y=" + corner1y + " Z=" + corner1z + ")");
                }
            }
        }
        if(customKeyBinds[2].isKeyDown()){
            if(!enabled){
                Utils.addCustomMessage("Enabling script (Digging trench)");
                ungrabMouse();
                for(ProcessModule process : processes){
                    if(process instanceof DigTrench) {
                        process.toggle();
                        process.onEnable();
                        enabled = true;
                    }
                }
            }
        }
        if(customKeyBinds[3].isKeyDown()){
            if(!enabled){
                Utils.addCustomMessage("Enabling script (Filling trench)");
                ungrabMouse();
                for(ProcessModule process : processes){
                    if(process instanceof FillTrench) {
                        process.toggle();
                        process.onEnable();
                        enabled = true;
                    }
                }
            }
        }
        if(customKeyBinds[4].isKeyDown()){
            if(!enabled){
                Utils.addCustomMessage("Enabling script (Digging path)");
                ungrabMouse();
                for(ProcessModule process : processes){
                    if(process instanceof DigPath2) {
                        process.toggle();
                        process.onEnable();
                        enabled = true;
                    }
                }
            }
        }
        if(customKeyBinds[5].isKeyDown()){
            if(!enabled){
                Utils.addCustomMessage("Enabling script (Placing sugarcane)");
                ungrabMouse();
                for(ProcessModule process : processes){
                    if(process instanceof PlaceSC) {
                        process.toggle();
                        process.onEnable();
                        enabled = true;
                    }
                }
            }

        }
        if(customKeyBinds[6].isKeyDown()){
            if(enabled) {
                for(ProcessModule process : processes){
                    if(process.isEnabled()) {
                        process.toggle();
                        process.onDisable();
                    }
                }
                disableScript();
            }

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
    public static void switchToNextProcess(ProcessModule currentModule){
        Utils.addCustomLog("Switching processes");
        updateKeys(false, false, false, false, false, false, false);
        for(int i = 0; i < processes.size(); i++){
            if(processes.get(i).equals(currentModule)){
                processes.get(i).toggle();
                processes.get(i).onDisable();
                if(i < processes.size() - 1) {
                    processes.get(i + 1).onEnable();
                    processes.get(i + 1).toggle();
                }
                else {
                    Utils.addCustomLog("Completed process");
                    updateKeys(false, false, false, false, false, false, false);
                }
            }
        }
    }
    @SubscribeEvent
    public void onTickPlayer(TickEvent.ClientTickEvent tickEvent){
        if(tickEvent.phase != TickEvent.Phase.START)
            return;


        if(mc.thePlayer != null && mc.theWorld != null && enabled){

            for(ProcessModule process : processes){
                if(process.isEnabled()) {
                    process.onTick();
                }
            }
        }

    }






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



    void ScheduleRunnable(Runnable r, int delay, TimeUnit tu) {
        ScheduledExecutorService eTemp = Executors.newScheduledThreadPool(1);
        eTemp.schedule(r, delay, tu);
        eTemp.shutdown();
    }

    void initVar(){
        slowDig = false;
        inDiggingTrench = false;
        inFailsafe = false;
        error = false;
       // playerYaw = Math.round(AngleUtils.get360RotationYaw() / 90) < 4 ? Math.round(AngleUtils.get360RotationYaw() / 90) * 90 : 0;
    }


    public static void updateKeys(boolean wBool, boolean sBool, boolean aBool, boolean dBool, boolean atkBool,  boolean useBool, boolean shiftBool) {
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


    void disableScript(){
        enabled = false;
        diggingTrench = false;
        diggingPath = false;
        regrabMouse();
        Utils.addCustomMessage("Disabling script");
        updateKeys(false, false, false, false, false, false, false);
        for(ProcessModule process : processes){
            if(process.isEnabled()){
                process.onDisable();
                process.toggle();
            }
        }
    }
    void activateFailsafe() {
        inFailsafe = true;
        enabled = false;
        updateKeys(false, false, false, false, false, false, false);
    }

}

