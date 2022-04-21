package com.jelly;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod(modid = CaneBuilder.MODID, version = CaneBuilder.VERSION)
public class CaneBuilder {
    public static final String MODID = "canebuilder";
    public static final String NAME = "Cane Builder";
    public static final String VERSION = "1.0";
    /*
     ** @author JellyLab, Polycrylate
     */
    Minecraft mc = Minecraft.getMinecraft();

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

        MinecraftForge.EVENT_BUS.register(new CaneBuilder());
    }
    @SubscribeEvent
    public void onTickPlayer(TickEvent.ClientTickEvent tickEvent){
        if(tickEvent.phase != TickEvent.Phase.START)
            return;

        if(mc.thePlayer != null && mc.theWorld != null){
            //System.out.println("hi");
        }
    }

}
