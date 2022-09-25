package com.jelly.CaneBuilder.handlers;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.player.Baritone;
import com.jelly.CaneBuilder.gui.GUI;
import com.jelly.CaneBuilder.processes.*;
import com.jelly.CaneBuilder.utils.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeyBindHandler {
    static Minecraft mc = Minecraft.getMinecraft();
    static KeyBinding[] customKeyBinds = new KeyBinding[4];
    static int setmode = 0;
    public static KeyBinding keybindA = mc.gameSettings.keyBindLeft;
    public static KeyBinding keybindD = mc.gameSettings.keyBindRight;
    public static KeyBinding keybindW = mc.gameSettings.keyBindForward;
    public static KeyBinding keybindS = mc.gameSettings.keyBindBack;
    public static KeyBinding keybindAttack = mc.gameSettings.keyBindAttack;
    public static KeyBinding keybindUseItem = mc.gameSettings.keyBindUseItem;
    public static KeyBinding keyBindShift = mc.gameSettings.keyBindSneak;
    public static KeyBinding keyBindJump = mc.gameSettings.keyBindJump;



    public static void initializeCustomKeybindings() {
        customKeyBinds[0] = new KeyBinding("Open GUI", Keyboard.KEY_RSHIFT, "CaneBuilder");
        customKeyBinds[1] = new KeyBinding("Enable full script", Keyboard.KEY_F, "CaneBuilder");
        customKeyBinds[2] = new KeyBinding("Disable full script", Keyboard.KEY_Z, "CaneBuilder");
        customKeyBinds[3] = new KeyBinding("Set corner", Keyboard.KEY_P, "CaneBuilder");

        for (KeyBinding customKeyBind : customKeyBinds) {
            ClientRegistry.registerKeyBinding(customKeyBind);
        }
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {

        if (customKeyBinds[0].isKeyDown()) {
            mc.displayGuiScreen(new GUI());
            return;

        }
        if (customKeyBinds[1].isKeyDown()) {
            if (!BuilderState.enabled) {
                for (ProcessModule process : MacroHandler.processes) {
                    if (process instanceof PlaceDirt1) {
                        MacroHandler.layerCount = 0;
                        MacroHandler.startScript(process);
                    }
                }

            }
            return;
        }
        if (customKeyBinds[2].isKeyDown()) {
            if(BuilderState.enabled) {
                for (ProcessModule process : MacroHandler.processes) {
                    if (process.isEnabled()) {
                        process.toggle();
                    }
                }
                MacroHandler.disableScript();
                Baritone.stopWalk();
            }
            return;
        }
        if(customKeyBinds[3].isKeyDown()){
            if (setmode == 0) {
                BuilderState.setCorner1((int) Math.floor(mc.thePlayer.posX), (int) Math.floor(mc.thePlayer.posY - 1), (int) Math.floor(mc.thePlayer.posZ));
            } else {
                BuilderState.setCorner2((int) Math.floor(mc.thePlayer.posX), (int) Math.floor(mc.thePlayer.posY - 1), (int) Math.floor(mc.thePlayer.posZ));
            }
            setmode = 1 - setmode;
        }

    }



    public static void setKeyBindState(KeyBinding key, boolean pressed) {
        if (pressed) {
            if (mc.currentScreen != null) {
                realSetKeyBindState(key, false);
                return;
            }
        }
        realSetKeyBindState(key, pressed);
    }

    public static void updateKeys(boolean wBool, boolean sBool, boolean aBool, boolean dBool, boolean atkBool, boolean useBool, boolean shiftBool) {
        if (mc.currentScreen != null) {
            resetKeybindState();
            return;
        }
        realSetKeyBindState(keybindW, wBool);
        realSetKeyBindState(keybindS, sBool);
        realSetKeyBindState(keybindA, aBool);
        realSetKeyBindState(keybindD, dBool);
        realSetKeyBindState(keybindAttack, atkBool);
        realSetKeyBindState(keybindUseItem, useBool);
        realSetKeyBindState(keyBindShift, shiftBool);
    }

    public static void updateKeys(boolean wBool, boolean sBool, boolean aBool, boolean dBool, boolean atkBool) {
        if (mc.currentScreen != null) {
            resetKeybindState();
            return;
        }
        realSetKeyBindState(keybindW, wBool);
        realSetKeyBindState(keybindS, sBool);
        realSetKeyBindState(keybindA, aBool);
        realSetKeyBindState(keybindD, dBool);
        realSetKeyBindState(keybindAttack, atkBool);
    }

    public static void onTick(KeyBinding key) {
        if (mc.currentScreen == null) {
            KeyBinding.onTick(key.getKeyCode());
        }
    }

    public static void resetKeybindState() {
        realSetKeyBindState(keybindA, false);
        realSetKeyBindState(keybindS, false);
        realSetKeyBindState(keybindW, false);
        realSetKeyBindState(keybindD, false);
        realSetKeyBindState(keyBindShift, false);
        realSetKeyBindState(keyBindJump, false);
        realSetKeyBindState(keybindAttack, false);
        realSetKeyBindState(keybindUseItem, false);
    }

    private static void realSetKeyBindState(KeyBinding key, boolean pressed){
        if(pressed){
            if(!key.isKeyDown()){
                KeyBinding.onTick(key.getKeyCode());
            }
            KeyBinding.setKeyBindState(key.getKeyCode(), true);

        } else {
            KeyBinding.setKeyBindState(key.getKeyCode(), false);
        }

    }
}
