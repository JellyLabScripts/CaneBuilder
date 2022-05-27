package com.jelly.CaneBuilder.commands;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

public class SetLayer extends CommandBase {
    Minecraft mc = Minecraft.getMinecraft();

    @Override
    public String getCommandName() {
        return "layer";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Set layer count";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        try {
            if (args.length == 1){
                BuilderState.layer = Integer.parseInt(args[0]);
                Utils.addCustomMessage("Set layer count to : " + args[0]);
            } else {
                Utils.addCustomMessage("Error, Usage: /layer <n>", EnumChatFormatting.RED);
            }
        } catch (Exception e) {
            Utils.addCustomMessage("Error, Usage: /layer <n>", EnumChatFormatting.RED);
        }
    }
}
