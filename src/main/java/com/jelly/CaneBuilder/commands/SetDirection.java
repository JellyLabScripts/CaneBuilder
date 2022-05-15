package com.jelly.CaneBuilder.commands;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class SetDirection extends CommandBase {
    @Override
    public String getCommandName() {
        return "setdirection";
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
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        try {
            if (Integer.parseInt(args[0]) == 0) {
                Utils.addCustomMessage("Set direction to N/S");
                BuilderState.direction = 0;
            } else if (Integer.parseInt(args[0]) == 1) {
                Utils.addCustomMessage("Set direction to E/W");
                BuilderState.direction = 1;
            } else {
                Utils.addCustomMessage("Error parsing input, Usage: /setdirection [0/1]");
            }
        } catch (Exception e) {
            Utils.addCustomMessage("Error parsing input, Usage: /setdirection [0/1]");
        }
    }
}
