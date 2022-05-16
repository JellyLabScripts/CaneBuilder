package com.jelly.CaneBuilder.processes;

import com.jelly.CaneBuilder.utils.Rotation;
import com.jelly.CaneBuilder.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


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

    public abstract void onEnable();

    public abstract void onDisable();

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    protected void threadSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void ScheduleRunnable(Runnable r, int delay, TimeUnit tu) {
        ScheduledExecutorService eTemp = Executors.newScheduledThreadPool(1);
        eTemp.schedule(r, delay, tu);
        eTemp.shutdown();
    }

    protected void ExecuteRunnable(Runnable r) {
        ScheduledExecutorService eTemp = Executors.newScheduledThreadPool(1);
        eTemp.execute(r);
        eTemp.shutdown();
    }

    protected void onTick(int keyCode) {
        if (mc.currentScreen == null) {
            KeyBinding.onTick(keyCode);
        }
    }
}
