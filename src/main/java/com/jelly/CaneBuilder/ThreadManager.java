package com.jelly.CaneBuilder;

import com.jelly.CaneBuilder.utils.Utils;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class ThreadManager {

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
                Utils.addCustomLog("Threads interrupted");
                t.interrupt();
            } catch (Exception e) {
            }
        }
        currentThreads.clear();
    }



}
