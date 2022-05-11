package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.utils.AngleUtils;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProcessModule {
    public Minecraft mc = Minecraft.getMinecraft();
    public boolean enabled;
    public int keybindA = mc.gameSettings.keyBindLeft.getKeyCode();
    public int keybindD = mc.gameSettings.keyBindRight.getKeyCode();
    public int keybindW = mc.gameSettings.keyBindForward.getKeyCode();
    public int keybindS = mc.gameSettings.keyBindBack.getKeyCode();
    public int keybindAttack = mc.gameSettings.keyBindAttack.getKeyCode();
    public int keybindUseItem = mc.gameSettings.keyBindUseItem.getKeyCode();
    public int keyBindShift = mc.gameSettings.keyBindSneak.getKeyCode();
    public int keyBindJump = mc.gameSettings.keyBindJump.getKeyCode();
    enum direction {
        RIGHT,
        LEFT,
        NONE
    }
    public void onTick(){
    }
    public void onEnable(){

    }
    public void onDisable(){

    }
    public boolean isEnabled(){
        return enabled;
    }
    public void toggle(){
        enabled = !enabled;
    }

    protected void threadSleep(int millis) {
        try {
            Thread.sleep(millis);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    protected void ScheduleRunnable(Runnable r, int delay, TimeUnit tu) {
        ScheduledExecutorService eTemp = Executors.newScheduledThreadPool(1);
        eTemp.schedule(r, delay, tu);
        eTemp.shutdown();
    }

    protected void ExecuteRunnable(Runnable r) {
        ScheduledExecutorService eTemp = Executors.newScheduledThreadPool(1);
        eTemp.execute(r);
        eTemp.shutdown();
    }
    protected void resetKeybindState(){
        KeyBinding.setKeyBindState(keybindA, false);
        KeyBinding.setKeyBindState(keybindS, false);
        KeyBinding.setKeyBindState(keybindW, false);
        KeyBinding.setKeyBindState(keybindD, false);
        KeyBinding.setKeyBindState(keyBindShift, false);
        KeyBinding.setKeyBindState(keyBindJump, false);
        KeyBinding.setKeyBindState(keybindAttack, false);
        KeyBinding.setKeyBindState(keybindUseItem, false);
    }
    protected void setKeyBindState(int keyCode, boolean pressed){
        if(pressed){
            if(mc.currentScreen != null){
                Utils.addCustomLog("In GUI, pausing");
                KeyBinding.setKeyBindState(keyCode, false);
                return;
            }
        }
        KeyBinding.setKeyBindState(keyCode, pressed);
    }
    protected void onTick(int keyCode){
        if(mc.currentScreen == null){
            KeyBinding.onTick(keyCode);
        }
    }
    protected void AOTE(){
        try {
            Utils.addCustomLog("AOTEing");
            mc.thePlayer.inventory.currentItem = 7;
            AngleUtils.smoothRotatePitchTo(89, 1.2f);
            Thread.sleep(200);
            KeyBinding.onTick(keybindUseItem);
            Thread.sleep(1000);
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    protected void updateKeys(boolean wBool, boolean sBool, boolean aBool, boolean dBool, boolean atkBool,  boolean useBool, boolean shiftBool) {
        setKeyBindState(keybindW, wBool);
        setKeyBindState(keybindS, sBool);
        setKeyBindState(keybindA, aBool);
        setKeyBindState(keybindD, dBool);
        setKeyBindState(keybindAttack, atkBool);
        setKeyBindState(keybindUseItem, useBool);
        setKeyBindState(keyBindShift, shiftBool);
    }
    protected void updateKeys(boolean wBool, boolean sBool, boolean aBool, boolean dBool, boolean atkBool) {
        setKeyBindState(keybindW, wBool);
        setKeyBindState(keybindS, sBool);
        setKeyBindState(keybindA, aBool);
        setKeyBindState(keybindD, dBool);
        setKeyBindState(keybindAttack, atkBool);
    }



}
