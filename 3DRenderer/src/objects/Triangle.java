package objects;

import java.awt.*;

public class Triangle {

    public int index1, index2, index3;
    public Color color;
    public boolean opaque;

    public Triangle(int index1, int index2, int index3, Color color, boolean opaque) {
        this.index1 = index1;
        this.index2 = index2;
        this.index3 = index3;
        this.color = color;
        this.opaque = opaque;
    }
}
