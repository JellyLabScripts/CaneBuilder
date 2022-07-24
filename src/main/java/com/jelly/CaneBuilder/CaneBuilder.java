package com.jelly.CaneBuilder;

import com.jelly.CaneBuilder.commands.*;
import com.jelly.CaneBuilder.config.Config;
import com.jelly.CaneBuilder.player.Baritone;
import com.jelly.CaneBuilder.features.Failsafe;
import com.jelly.CaneBuilder.handlers.KeyBindHandler;
import com.jelly.CaneBuilder.handlers.MacroHandler;
import com.jelly.CaneBuilder.player.Rotation;
import com.jelly.CaneBuilder.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Field;

/*
 ** @author JellyLab, Polycrylate
 */

@Mod(modid = CaneBuilder.MODID, version = CaneBuilder.VERSION)
public class CaneBuilder {
    public static final String MODID = "canebuilder";
    public static final String NAME = "Cane Builder";
    public static final String VERSION = "2.0";

    public static Rotation rotation = new Rotation();


    static Minecraft mc = Minecraft.getMinecraft();




    @Mod.EventHandler
    public static void init(FMLInitializationEvent event) {
        try {
            Config.readConfig();
        } catch (Exception e) {
            System.out.println("Error reading config file");
        }
        for(Field f : GuiEditSign.class.getDeclaredFields()){
            LogManager.getContext().getLogger(MODID).info(f.toString() + " " + f.getType());
        }
        System.out.println("Registering");
        MinecraftForge.EVENT_BUS.register(new CaneBuilder());
        MinecraftForge.EVENT_BUS.register(new Failsafe());
        MinecraftForge.EVENT_BUS.register(new Baritone());
        MinecraftForge.EVENT_BUS.register(new BlockRenderer());
        MinecraftForge.EVENT_BUS.register(new KeyBindHandler());
        MinecraftForge.EVENT_BUS.register(new MacroHandler());
        ClientCommandHandler.instance.registerCommand(new SetDirection());
        ClientCommandHandler.instance.registerCommand(new ToggleLog());
        ClientCommandHandler.instance.registerCommand(new Corner1());
        ClientCommandHandler.instance.registerCommand(new Corner2());
        ClientCommandHandler.instance.registerCommand(new SetLayer());

        KeyBindHandler.initializeCustomKeybindings();
        MacroHandler.initializeMacro();

    }

    @SubscribeEvent
    public void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        System.out.println("Detecting leave, disabling");
        System.out.println("Detecting leave, disabling");
        MacroHandler.disableScript();
    }


    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void render(RenderGameOverlayEvent event) {
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            int topPad = 0;
            if (mc.gameSettings.showDebugInfo) {
                topPad = (new ScaledResolution(mc).getScaledHeight() / 2) - 50;
            }
            drawStringWithShadow(
              EnumChatFormatting.DARK_GREEN + "-- " + EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.BOLD + "CANE BUILDER" + EnumChatFormatting.DARK_GREEN + " --", 4, topPad + 25, 1f, -1);
            drawStringWithShadow(
              EnumChatFormatting.WHITE + "" + "Corner 1: " + (BuilderState.corner1 != null ? EnumChatFormatting.GREEN + BuilderState.corner1.toString() : EnumChatFormatting.RED + "Not set"), 4, topPad + 40, 1f, -1);
            drawStringWithShadow(
              EnumChatFormatting.WHITE + "" + "Corner 2: " + (BuilderState.corner2 != null ? EnumChatFormatting.GREEN + BuilderState.corner2.toString() : EnumChatFormatting.RED + "Not set"), 4, topPad + 50, 1f, -1);
            drawStringWithShadow(
              EnumChatFormatting.WHITE + "" + "Direction: " + (BuilderState.direction != -1 ? EnumChatFormatting.GREEN + (BuilderState.direction == 0 ? "North / South" : "East / West") : EnumChatFormatting.RED + "Not set"), 4, topPad + 60, 1f, -1);
            drawStringWithShadow(
                    EnumChatFormatting.WHITE + "" + "Layer count: " + (BuilderState.layer != 0 ? EnumChatFormatting.GREEN + "" + (BuilderState.layer) : EnumChatFormatting.RED + "Not set"), 4, topPad + 70, 1f, -1);
            drawStringWithShadow(
                    EnumChatFormatting.WHITE + "" + "Debug log: " + (BuilderState.log ? EnumChatFormatting.GREEN + "On" : EnumChatFormatting.GREEN + "Off"), 4, topPad + 80, 1f, -1);
        }
    }


    public static void drawStringWithShadow(String text, int x, int y, float size, int color) {
        GlStateManager.scale(size,size,size);
        float mSize = (float)Math.pow(size,-1);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text,Math.round(x / size),Math.round(y / size),color);
        GlStateManager.scale(mSize,mSize,mSize);
    }


}

