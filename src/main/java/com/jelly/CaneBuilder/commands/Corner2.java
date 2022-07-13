package com.jelly.CaneBuilder.commands;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.utils.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

public class Corner2 extends CommandBase {
    Minecraft mc = Minecraft.getMinecraft();

    @Override
    public String getCommandName() {
        return "corner2";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Set corner 2";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        try {
            if (args.length == 0) {
                BuilderState.setCorner2((int) Math.floor(mc.thePlayer.posX), (int) Math.floor(mc.thePlayer.posY - 1), (int) Math.floor(mc.thePlayer.posZ));
            } else if (args.length == 3) {
                BuilderState.setCorner2(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            } else {
                LogUtils.addCustomMessage("Error, Usage: /corner2 or /corner2 <x> <y> <z>", EnumChatFormatting.RED);
            }
        } catch (Exception e) {
            LogUtils.addCustomMessage("Error, Usage: /corner2 or /corner2 <x> <y> <z>", EnumChatFormatting.RED);
        }
    }
}
