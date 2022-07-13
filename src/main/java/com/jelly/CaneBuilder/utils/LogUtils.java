package com.jelly.CaneBuilder.utils;

import com.jelly.CaneBuilder.BuilderState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class LogUtils {

    protected static Minecraft mc = Minecraft.getMinecraft();

    public static void addCustomMessage(String msg) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GREEN +
          "[Cane Builder] : " + EnumChatFormatting.GRAY + msg));
    }

    public static void addCustomMessage(String msg, EnumChatFormatting color) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GREEN +
          "[Cane Builder] : " + color + msg));
    }

    public static void addCustomLog(String log) {
        if(BuilderState.log) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.BLUE +
                    "[Log] : " + EnumChatFormatting.GRAY + log));
        }
    }

}
