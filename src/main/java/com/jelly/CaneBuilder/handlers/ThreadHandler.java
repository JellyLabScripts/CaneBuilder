package com.jelly.CaneBuilder.handlers;

import com.jelly.CaneBuilder.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class ThreadHandler {

    static List<Thread> currentThreads = new ArrayList<>();

    public static void executeThread(Thread thread){
        currentThreads.add(thread);
        new Thread(() -> {
            try {
                thread.start();
                thread.join();
                currentThreads.remove(thread);
            } catch (InterruptedException ignored) {
            }
        }).start();
    }
    public static void stopExistingThreads(){
        for (Thread t : currentThreads) {
            try {
                LogUtils.addCustomLog("Threads interrupted");
                t.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        currentThreads.clear();
    }



}
