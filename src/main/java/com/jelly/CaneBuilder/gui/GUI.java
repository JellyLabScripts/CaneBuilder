package com.jelly.CaneBuilder.gui;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.handlers.MacroHandler;
import com.jelly.CaneBuilder.processes.*;
import com.jelly.CaneBuilder.utils.LogUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GUI extends GuiScreen {
    int buttonWidth = 85;
    int buttonHeight = 65;
    int buttonYSpace = 20;
    int buttonXSpace = 25;
    @Override
    public void initGui() {
        super.initGui();

        int horizontalMiddle = this.width/2 - buttonWidth/2;
        int verticalMiddle = this.height/2 - buttonHeight/2;


        this.buttonList.add(new GuiBetterButton(0,  horizontalMiddle,  verticalMiddle - buttonYSpace * 2 - buttonHeight, buttonWidth, buttonHeight, "Start"));
        this.buttonList.add(new GuiBetterButton(1, horizontalMiddle - buttonXSpace - buttonWidth,  verticalMiddle, buttonWidth, buttonHeight, "Dig trench"));
        this.buttonList.add(new GuiBetterButton(2, horizontalMiddle,  verticalMiddle, buttonWidth, buttonHeight, "Fill Trench"));
        this.buttonList.add(new GuiBetterButton(3, horizontalMiddle + buttonXSpace + buttonWidth,  verticalMiddle, buttonWidth, buttonHeight, "Place Dirt 2"));
        this.buttonList.add(new GuiBetterButton(4, horizontalMiddle - buttonXSpace  - buttonWidth,  verticalMiddle + buttonYSpace + buttonHeight, buttonWidth, buttonHeight, "Dig Path 1"));
        this.buttonList.add(new GuiBetterButton(5, horizontalMiddle,  verticalMiddle + buttonYSpace + buttonHeight , buttonWidth, buttonHeight, "Dig Path 2"));
        this.buttonList.add(new GuiBetterButton(6, horizontalMiddle + buttonXSpace +  buttonWidth,  verticalMiddle  + buttonYSpace + buttonHeight, buttonWidth, buttonHeight, "Place sugarcane"));

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawRect(0, 0, this.width, this.height, 0x30000000);
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }


    @Override
    protected void actionPerformed(GuiButton button){

        mc.thePlayer.closeScreen();
        switch (button.id){
            case 0 :
                if (!BuilderState.enabled) {
                    if (Math.floor(mc.thePlayer.posX) == BuilderState.corner1.getX() && Math.floor(mc.thePlayer.posZ) == BuilderState.corner1.getZ()) {
                        for (ProcessModule process : MacroHandler.processes) {
                            if (process instanceof PlaceDirt1) {
                                LogUtils.addCustomLog("starting");
                                MacroHandler.startScript(process);
                            }
                        }
                    } else {
                        LogUtils.addCustomMessage("Stand on 1st corner to start! " + BuilderState.corner1);
                    }
                }
                return;

            case 1 :
                if (!BuilderState.enabled) {
                    LogUtils.addCustomMessage("Enabling script (Dig trench)");
                    for (ProcessModule process : MacroHandler.processes) {
                        if (process instanceof DigTrench) {
                            MacroHandler.startScript(process);
                        }
                    }
                }
                return;
            case 2 :
                if (!BuilderState.enabled) {
                    LogUtils.addCustomMessage("Enabling script (Fill trench)");
                    for (ProcessModule process : MacroHandler.processes) {
                        if (process instanceof FillTrench) {
                            MacroHandler.startScript(process);
                        }
                    }
                }
                return;
            case 3 :
                if (!BuilderState.enabled) {
                    LogUtils.addCustomMessage("Enabling script (Place dirt 2)");
                    for (ProcessModule process : MacroHandler.processes) {
                        if (process instanceof PlaceDirt5) {
                            MacroHandler.startScript(process);
                        }
                    }
                }
                return;
            case 4 :
                if (!BuilderState.enabled) {
                    LogUtils.addCustomMessage("Enabling script (Dig path 1)");
                    for (ProcessModule process : MacroHandler.processes) {
                        if (process instanceof DigPath1) {
                            MacroHandler.startScript(process);
                        }
                    }
                }
                return;
            case 5 :
                if (!BuilderState.enabled) {
                    LogUtils.addCustomMessage("Enabling script (Dig path 2)");
                    for (ProcessModule process : MacroHandler.processes) {
                        if (process instanceof DigPath2) {
                            MacroHandler.startScript(process);
                        }
                    }
                }
                return;
            case 6 :
                if (!BuilderState.enabled) {
                    LogUtils.addCustomMessage("Enabling script (Place sugarcane)");
                    for (ProcessModule process : MacroHandler.processes) {
                        if (process instanceof PlaceSC) {
                            MacroHandler.startScript(process);
                        }
                    }
                }
        }

    }
}
