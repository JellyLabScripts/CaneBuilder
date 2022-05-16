package com.jelly.CaneBuilder.commands;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.config.Config;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

public class SetDirection extends CommandBase {
    @Override
    public String getCommandName() {
        return "direction";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Lets you choose the direction of the farm to be N/S or E/W";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        try {
            if (Integer.parseInt(args[0]) == 0) {
                Utils.addCustomMessage("Set direction to N/S", EnumChatFormatting.GREEN);
                BuilderState.direction = 0;
                Config.writeConfig();
            } else if (Integer.parseInt(args[0]) == 1) {
                Utils.addCustomMessage("Set direction to E/W", EnumChatFormatting.GREEN);
                BuilderState.direction = 1;
                Config.writeConfig();
            } else {
                Utils.addCustomMessage("Error, Usage: /direction [0/1]", EnumChatFormatting.RED);
            }
        } catch (Exception e) {
            Utils.addCustomMessage("Error, Usage: /direction [0/1]", EnumChatFormatting.RED);
        }
    }
}
