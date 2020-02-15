package de.summerfeeling.labyemoteplayer.utils.simpleinventory;

public abstract class ExtendedButtonClickEvent implements ButtonClickEvent {

    private int[] coords = new int[2];
    private int position;

    public ExtendedButtonClickEvent(int x, int y) {
        coords[0] = x;
        coords[1] = y;
    }

    public ExtendedButtonClickEvent(int position) {
        this.position = position;
    }

    public int getX() {
        return coords[0];
    }

    public int getY() {
        return coords[1];
    }

    public int getPosition() {
        return position;
    }

}
