package com.jelly.CaneBuilder.structures;

import com.jelly.CaneBuilder.BuilderState;

public class Coord {
    private final int x;
    private final int y;
    private final int z;

    public Coord(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public int getParallel() {
        if (BuilderState.direction == 0) {
            return z;
        } else if (BuilderState.direction == 1) {
            return x;
        }
        return -1;
    }

    public int getPerpendicular() {
        if (BuilderState.direction == 0) {
            return x;
        } else if (BuilderState.direction == 1) {
            return z;
        }
        return -1;
    }

    @Override
    public String toString() {
        return "(X: " + x + ", Y: " + y + ", Z: " + z + ")";
    }
}
