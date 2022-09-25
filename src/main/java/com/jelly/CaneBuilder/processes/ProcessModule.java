package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.handlers.ThreadHandler;
import com.jelly.CaneBuilder.player.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;


public abstract class ProcessModule {
    public Minecraft mc = Minecraft.getMinecraft();
    public boolean enabled = false;
    public Rotation rotation = new Rotation();

    public abstract void onTick();

    public void onRenderWorld() {
        if (enabled && rotation.rotating) {
            rotation.update();
        }
    }

    protected abstract void onEnable();

    protected abstract void onDisable();

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
        if(enabled)
            onEnable();
        if(!enabled)
            onDisable();
    }

    protected void threadSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected void ExecuteRunnable(Thread t) {
        ThreadHandler.executeThread(t);
    }



}
