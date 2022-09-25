package com.jelly.CaneBuilder.handlers;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.features.Failsafe;
import com.jelly.CaneBuilder.player.Rotation;
import com.jelly.CaneBuilder.processes.*;
import com.jelly.CaneBuilder.structures.Coord;
import com.jelly.CaneBuilder.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.ArrayList;
import java.util.List;

import static com.jelly.CaneBuilder.utils.InventoryUtils.clickWindow;

public class MacroHandler {

    public static List<ProcessModule> processes = new ArrayList<>();
    public static Minecraft mc = Minecraft.getMinecraft();

    public static String[] requiredSlotsName = new String[]{"Builder", "Infini", "Shovel", "Prisma", "Magical", "Pickaxe", "Aspect", "Dirt"};

    public static boolean isFastBreakOn = false;
    public static int layerCount = 0;

    static ProcessModule currentProcess;

    public static Rotation playerRotation = new Rotation(); //used by utils classes

    public static void initializeMacro(){
        processes.add(new PlaceDirt1());
        processes.add(new PlaceDirt2());
        processes.add(new PlaceDirt3());
        processes.add(new PlaceDirt4());
        processes.add(new DigTrench());
        processes.add(new FillTrench());
        processes.add(new PlaceDirt5());
        processes.add(new PlaceDirt6());
        processes.add(new DigPath1());
        processes.add(new DigPath2());
        processes.add(new PlaceSC());
        processes.add(new BackToCorner1());
    }

    @SubscribeEvent
    public void onTickPlayer(TickEvent.ClientTickEvent tickEvent) {
        if (!BuilderState.enabled || BuilderState.paused || mc.thePlayer == null || mc.theWorld == null || tickEvent.phase != TickEvent.Phase.START ||
                (ScoreboardUtils.getLocation() != ScoreboardUtils.location.ISLAND && Failsafe.pauseOnLeave))
            return;

        for (ProcessModule process : processes) {
            if (process.isEnabled()) {
                process.onTick();
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(final RenderWorldLastEvent event) {
        playerRotation.update();
        if (!BuilderState.enabled || BuilderState.paused || mc.thePlayer == null || mc.theWorld == null) return;

        for (ProcessModule process : processes) {
            if (process.isEnabled()) {
                process.onRenderWorld();
            }
        }
    }

    public static void switchToNextProcess(ProcessModule module) {
        LogUtils.addCustomLog("Switching processes " + processes.size());
        ThreadHandler.stopExistingThreads();
        Failsafe.pauseOnLeave = true;
        KeyBindHandler.updateKeys(false, false, false, false, false, false, false);
        for (int i = 0; i < processes.size(); i++) {
            if (processes.get(i).equals(module)) {
                processes.get(i).toggle();
                LogUtils.addCustomLog(i + " " + processes.get(i));
                if (i < processes.size() - 1) {
                    processes.get(i + 1).toggle();
                    currentProcess = processes.get(i + 1);
                } else {
                    layerCount ++;
                    if(layerCount < BuilderState.layer){ //switch to next layer
                        disableScript("Switching to next layer");
                        ThreadHandler.executeThread(new Thread(() -> {
                            ProcessUtils.switchLayer();
                            for (ProcessModule process : processes) {
                                if (process instanceof PlaceDirt1) {
                                    startScript(process);
                                }
                            }
                        }));

                    } else {
                        disableScript();
                        LogUtils.addCustomMessage("Completed Layer!", EnumChatFormatting.GREEN);
                    }
                }
            }
        }
    }




    public static void startScript(ProcessModule processModule){
        isFastBreakOn = false;
        if(BuilderState.layer == 0){
            LogUtils.addCustomMessage("Bozo set layer count");
            return;
        }
        if(BuilderState.direction == 0){
            if ((Math.abs(BuilderState.corner1.getZ() - BuilderState.corner2.getZ()) + 1) % 3 != 0) {
                LogUtils.addCustomMessage(suggestCoords(BuilderState.direction));
                return;
            }
        } else {
            if ((Math.abs(BuilderState.corner1.getX() - BuilderState.corner2.getX()) + 1) % 3 != 0){
                LogUtils.addCustomMessage(suggestCoords(BuilderState.direction));
                return;
            }
        }
        if(!(processModule instanceof PlaceSC)) {
            for (String s : requiredSlotsName) {
                if (!InventoryUtils.hasItemInInventory(s)) {
                    LogUtils.addCustomMessage("Not enough tools! Read how-to-use!");
                    return;
                }
            }
        }
        if(processModule instanceof PlaceDirt1) {
            if (Math.floor(mc.thePlayer.posX) == BuilderState.corner1.getX() && Math.floor(mc.thePlayer.posZ) != BuilderState.corner1.getZ()) {
                LogUtils.addCustomMessage("Stand on 1st corner to start! " + BuilderState.corner1);
                return;
            }
            if (BuilderState.corner1.getY() != (int) mc.thePlayer.posY - 1) {
                LogUtils.addCustomMessage("Your Y level is wrong! Set it again or you were starting at the wrong place!");
                return;
            }
        }
        if(processModule instanceof DigTrench) {
            if (BuilderState.corner1.getY() != (int) mc.thePlayer.posY - 2) {
                LogUtils.addCustomMessage("Your Y level is wrong! Set it again or you were starting at the wrong place!");
                return;
            }
        }

        ThreadHandler.executeThread(new Thread(() -> {
            try {
                if(!(processModule instanceof PlaceSC)) {
                    ProcessUtils.disableJumpPotion();
                    LogUtils.addCustomLog("Setting Rancher's boot's speed");
                    ProcessUtils.setRancherBootsTo400();
                    Thread.sleep(500);
                    LogUtils.addCustomLog("Preparing inventory");

                    InventoryUtils.openInventory();
                    Thread.sleep(500);

                    for (int i = 0; i < requiredSlotsName.length; i++) {
                        LogUtils.addCustomLog("Slot for " + requiredSlotsName[i] + " : " + InventoryUtils.getSlotNumberByDisplayName(requiredSlotsName[i]));

                        if(mc.thePlayer.inventoryContainer.getSlot(i + 36).getHasStack()) {
                            if (mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack().getDisplayName().contains(requiredSlotsName[i]))
                                continue;

                            clickWindow(mc.thePlayer.openContainer.windowId, 36 + i, 0, 1);
                            Thread.sleep(500);
                        }
                        if(InventoryUtils.getSlotNumberByDisplayName(requiredSlotsName[i]) >= 36){
                            clickWindow(mc.thePlayer.openContainer.windowId, InventoryUtils.getSlotNumberByDisplayName(requiredSlotsName[i]), 0, 1);
                            Thread.sleep(500);
                        }

                        clickWindow(mc.thePlayer.openContainer.windowId, InventoryUtils.getSlotNumberByDisplayName(requiredSlotsName[i]), 0, 1);
                        Thread.sleep(500);
                    }
                    mc.thePlayer.closeScreen();
                    Thread.sleep(500);
                }
                processModule.toggle();
                currentProcess = processModule;
                BuilderState.isSwitchingLayer = false;
                BuilderState.enabled = true;
                BuilderState.paused = false;
            } catch (Exception e) {
                e.printStackTrace();
                disableScript();
            }
        }));


    }

    public static void disableScript(String msg) {
        BuilderState.enabled = false;
        BuilderState.isSwitchingLayer = false;
        LogUtils.addCustomMessage(msg);
        KeyBindHandler.resetKeybindState();
        ThreadHandler.stopExistingThreads();
        for (ProcessModule process : processes) {
            if (process.isEnabled()) {
                process.toggle();
            }
        }
    }
    public static void disableScript() {
        disableScript(EnumChatFormatting.RED + "Disabling script" );
    }
    public static void continueMacro() {
        if(!currentProcess.isEnabled())
            currentProcess.toggle();
        LogUtils.addCustomMessage("Script continued");
        BuilderState.paused = false;
    }
    public static void pauseScript() {
        if(currentProcess.isEnabled())
            currentProcess.toggle();
        LogUtils.addCustomMessage("Script paused");
        BuilderState.paused = true;
    }



    private static String suggestCoords(int direction) {
        int corner1 = direction == 0 ? BuilderState.corner1.getZ() : BuilderState.corner1.getX();
        int corner2 = direction == 0 ? BuilderState.corner2.getZ() : BuilderState.corner2.getX();
        while ((Math.abs(corner1) + Math.abs(corner2) + 1) % 3 != 0) {
            if (Math.abs(corner1) < Math.abs(corner2)) {
                if (Math.abs(corner2) == 80) {
                    if (corner1 == -80) {corner1 += 2; break;}
                    else if (corner1 == 80) {corner1 -= 2;break;}
                }
                corner1 += Integer.signum(corner2);
            } else {
                if (Math.abs(corner1) == 80) {
                    if (corner2 == -80) {corner2 += 2; break;}
                    else if (corner2 == 80) {corner2 -= 2;break;}
                }
                corner2 += Integer.signum(corner2);
            }
        }
        Coord first;
        Coord second;

        if (direction == 0) {
            first = new Coord(BuilderState.corner1.getX(), BuilderState.corner1.getY(), corner1);
            second = new Coord(BuilderState.corner2.getX(), BuilderState.corner2.getY(), corner2);
        } else {
            first = new Coord(corner1, BuilderState.corner1.getY(), BuilderState.corner1.getZ());
            second = new Coord(corner2, BuilderState.corner2.getY(), BuilderState.corner2.getZ());
        }

        return ("Incorrect corners bozo, you should change them to §a" + first + " " + second + "§7. If you don't like these coords, check out #how-to-use and do it yourself");
    }



}
