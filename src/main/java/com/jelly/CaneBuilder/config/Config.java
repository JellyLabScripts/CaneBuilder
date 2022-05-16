package com.jelly.CaneBuilder.config;

import com.jelly.CaneBuilder.BuilderState;
import com.jelly.CaneBuilder.structures.Coord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class Config {
    public static void setConfig(Coord c1, Coord c2, int direction) {
        BuilderState.corner1 = c1;
        BuilderState.corner2 = c2;
        BuilderState.direction = direction;
    }

    public static void writeConfig() {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("config/canebuilder.txt"));
            bufferedWriter.write("\n" + BuilderState.corner1.getX());
            bufferedWriter.write("\n" + BuilderState.corner1.getY());
            bufferedWriter.write("\n" + BuilderState.corner1.getZ());

            bufferedWriter.write("\n" + BuilderState.corner2.getX());
            bufferedWriter.write("\n" + BuilderState.corner2.getY());
            bufferedWriter.write("\n" + BuilderState.corner2.getZ());

            bufferedWriter.write("\n" + BuilderState.direction);
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void readConfig() throws Exception {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("config/canebuilder.txt"));
            bufferedReader.readLine();
            String url;
            setConfig(
              new Coord(Integer.parseInt(bufferedReader.readLine()), Integer.parseInt(bufferedReader.readLine()), Integer.parseInt(bufferedReader.readLine())),
              new Coord(Integer.parseInt(bufferedReader.readLine()), Integer.parseInt(bufferedReader.readLine()), Integer.parseInt(bufferedReader.readLine())),
              Integer.parseInt(bufferedReader.readLine())
            );
            bufferedReader.close();
        } catch (Exception e) {
            throw new Exception();
        }
    }
}