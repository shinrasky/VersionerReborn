package com.shinranaruto.versioner.util;

public class Coords {
    public final int x;
    public final int y;

    public Coords(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coords add(Coords another) {
        return new Coords(this.x + another.x, this.y + another.y);
    }

    public Coords add(int x, int y) {
        return new Coords(this.x + x, this.y + y);
    }

    public Coords subtract(Coords another) {
        return new Coords(this.x - another.x, this.y - another.y);
    }

    public Coords subtract(int x, int y) {
        return new Coords(this.x - x, this.y - y);
    }

    public Coords copy() {
        return new Coords(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coords coords = (Coords) obj;
        return x == coords.x && y == coords.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
