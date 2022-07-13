package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.player.Baritone;
import com.jelly.CaneBuilder.handlers.KeyBindHandler;
import com.jelly.CaneBuilder.handlers.MacroHandler;
import com.jelly.CaneBuilder.utils.AngleUtils;
import net.minecraft.util.BlockPos;

import static com.jelly.CaneBuilder.handlers.KeyBindHandler.*;

public class BackToCorner1 extends ProcessModule{
    boolean canFindPath = true;
    @Override
    public void onTick() {
        if (rotation.rotating) {
            resetKeybindState();
            return;
        }
        if(Math.floor(mc.thePlayer.posX) == BuilderState.corner1.getX() && Math.floor(mc.thePlayer.posZ) == BuilderState.corner1.getZ()){
            MacroHandler.switchToNextProcess(this);
        }
        if(!canFindPath){
            try{
                Baritone.walkTo(new BlockPos(BuilderState.corner1.getX(), BuilderState.corner1.getY() + 3, BuilderState.corner1.getZ()));
                KeyBindHandler.setKeyBindState(keybindW, false);
                canFindPath = true;
            }catch (Exception e){
                KeyBindHandler.setKeyBindState(keybindW, true);
            }
        }

    }

    @Override
    public void onEnable() {
        try {
            if (!Baritone.walking) {
                Baritone.walkTo(new BlockPos(BuilderState.corner1.getX(), BuilderState.corner1.getY() + 3, BuilderState.corner1.getZ()));
                canFindPath = true;
            }
        } catch (Exception e){
            rotation.easeTo(AngleUtils.perpendicularToC1(), 0, 1000);
            canFindPath = false;
        }


    }

    @Override
    public void onDisable() {
    }
}
