package com.jelly.CaneBuilder;

import com.jelly.CaneBuilder.gui.GUI;
import com.jelly.CaneBuilder.processes.*;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeyBindHelper {
    static Minecraft mc = Minecraft.getMinecraft();
    static KeyBinding[] customKeyBinds = new KeyBinding[4];
    static int setmode = 0;
    public static int keybindA = mc.gameSettings.keyBindLeft.getKeyCode();
    public static int keybindD = mc.gameSettings.keyBindRight.getKeyCode();
    public static int keybindW = mc.gameSettings.keyBindForward.getKeyCode();
    public static int keybindS = mc.gameSettings.keyBindBack.getKeyCode();
    public static int keybindAttack = mc.gameSettings.keyBindAttack.getKeyCode();
    public static int keybindUseItem = mc.gameSettings.keyBindUseItem.getKeyCode();
    public static int keyBindSpace = mc.gameSettings.keyBindJump.getKeyCode();
    public static int keyBindShift = mc.gameSettings.keyBindSneak.getKeyCode();
    public static int keyBindJump = mc.gameSettings.keyBindJump.getKeyCode();

    public static void initializeCustomKeybindings() {
        customKeyBinds[0] = new KeyBinding("Open GUI", Keyboard.KEY_RSHIFT, "CaneBuilder");
        customKeyBinds[1] = new KeyBinding("Enable full script", Keyboard.KEY_F, "CaneBuilder");
        customKeyBinds[2] = new KeyBinding("Disable full script", Keyboard.KEY_Z, "CaneBuilder");
        customKeyBinds[3] = new KeyBinding("Set corner", Keyboard.KEY_P, "CaneBuilder");

        for (KeyBinding customKeyBind : customKeyBinds) {
            ClientRegistry.registerKeyBinding(customKeyBind);
        }
    }

    public static void onKeyPress(InputEvent.KeyInputEvent event) {


        if (customKeyBinds[0].isKeyDown()) {
            mc.displayGuiScreen(new GUI());
            return;

        }
        if (customKeyBinds[1].isKeyDown()) {
            if (!BuilderState.enabled) {
                if(Math.floor(mc.thePlayer.posX) == BuilderState.corner1.getX() && Math.floor(mc.thePlayer.posZ) != BuilderState.corner1.getZ()) {
                    Utils.addCustomMessage("Stand on 1st corner to start! " + BuilderState.corner1);
                    return;
                }
                if(BuilderState.corner1.getY() != (int)mc.thePlayer.posY - 1){
                    Utils.addCustomMessage("Your Y level is wrong! Read #how-to-use!");
                    return;
                }
                for (ProcessModule process : CaneBuilder.processes) {
                    if (process instanceof PlaceDirt1) {
                        CaneBuilder.layerCount = 0;
                        CaneBuilder.startScript(process);
                    }
                }

            }
            return;
        }
        if (customKeyBinds[2].isKeyDown()) {
            if(BuilderState.enabled) {
                for (ProcessModule process : CaneBuilder.processes) {
                    if (process.isEnabled()) {
                        process.toggle();
                    }
                }
                CaneBuilder.disableScript();
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



    public static void setKeyBindState(int keyCode, boolean pressed) {
        if (pressed) {
            if (mc.currentScreen != null) {
                Utils.addCustomLog("In GUI, pausing");
                KeyBinding.setKeyBindState(keyCode, false);
                return;
            }
        }
        KeyBinding.setKeyBindState(keyCode, pressed);
    }

    public static void updateKeys(boolean wBool, boolean sBool, boolean aBool, boolean dBool, boolean atkBool, boolean useBool, boolean shiftBool) {
        if (mc.currentScreen != null) {
            resetKeybindState();
            return;
        }
        KeyBinding.setKeyBindState(keybindW, wBool);
        KeyBinding.setKeyBindState(keybindS, sBool);
        KeyBinding.setKeyBindState(keybindA, aBool);
        KeyBinding.setKeyBindState(keybindD, dBool);
        KeyBinding.setKeyBindState(keybindAttack, atkBool);
        KeyBinding.setKeyBindState(keybindUseItem, useBool);
        KeyBinding.setKeyBindState(keyBindShift, shiftBool);
    }

    public static void updateKeys(boolean wBool, boolean sBool, boolean aBool, boolean dBool, boolean atkBool) {
        if (mc.currentScreen != null) {
            resetKeybindState();
            return;
        }
        KeyBinding.setKeyBindState(keybindW, wBool);
        KeyBinding.setKeyBindState(keybindS, sBool);
        KeyBinding.setKeyBindState(keybindA, aBool);
        KeyBinding.setKeyBindState(keybindD, dBool);
        KeyBinding.setKeyBindState(keybindAttack, atkBool);
    }

    public static void resetKeybindState() {
        KeyBinding.setKeyBindState(keybindA, false);
        KeyBinding.setKeyBindState(keybindS, false);
        KeyBinding.setKeyBindState(keybindW, false);
        KeyBinding.setKeyBindState(keybindD, false);
        KeyBinding.setKeyBindState(keyBindShift, false);
        KeyBinding.setKeyBindState(keyBindJump, false);
        KeyBinding.setKeyBindState(keybindAttack, false);
        KeyBinding.setKeyBindState(keybindUseItem, false);
    }
}
