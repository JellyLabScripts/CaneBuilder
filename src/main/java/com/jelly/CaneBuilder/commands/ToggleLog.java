package com.jelly.CaneBuilder.commands;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.config.Config;
import com.jelly.CaneBuilder.utils.LogUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

public class ToggleLog extends CommandBase {
    @Override
    public String getCommandName() {
        return "log";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Toggles on/off debug logs";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        try {
            if (args[0].equals("on")) {
                BuilderState.log = true;
                LogUtils.addCustomMessage("Set debug log to on", EnumChatFormatting.RED);
                Config.writeConfig();
            } else if (args[0].equals("off")){
                BuilderState.log = false;
                LogUtils.addCustomMessage("Set debug log to off", EnumChatFormatting.RED);
                Config.writeConfig();
            } else {
                LogUtils.addCustomMessage("Error, Usage: /log [on/off]", EnumChatFormatting.RED);
            }
        } catch (Exception e) {
            LogUtils.addCustomMessage("Error, Usage: /log [on/off]", EnumChatFormatting.RED);
        }
    }
}
