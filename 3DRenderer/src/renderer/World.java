package renderer;

import objects.Line;
import objects.Triangle;
import datatypes.Vector3;
import objects.Vertex;

import java.awt.*;

public class World {

    public Vertex[] vertices;
    public Line[] lines;
    public Triangle[] triangles;

    public World(int vSize, int lSize, int tSize) {

        vertices = new Vertex[vSize];
        lines = new Line[lSize];
        triangles = new Triangle[tSize];

        createBasicWorld();
    }

    void createBasicWorld() {

        storeVertex(new Vector3(-10, -10,  20), Color.green, 10, true); // Index 0: Front-Bottom-Left
        storeVertex(new Vector3( 10, -10,  20), Color.green, 10, true); // Index 1: Front-Bottom-Right
        storeVertex(new Vector3( 10,  10,  20), Color.green, 10, true); // Index 2: Front-Top-Right
        storeVertex(new Vector3(-10,  10,  20), Color.green, 10, true); // Index 3: Front-Top-Left
        storeVertex(new Vector3(-10, -10,  40), Color.green, 10, true); // Index 4: Back-Bottom-Left
        storeVertex(new Vector3( 10, -10,  40), Color.green, 10, true); // Index 5: Back-Bottom-Right
        storeVertex(new Vector3( 10,  10,  40), Color.green, 10, true); // Index 6: Back-Top-Right
        storeVertex(new Vector3(-10,  10,  40), Color.green, 10, true); // Index 7: Back-Top-Left

        storeTriangles(0, 2, 1, Color.red, true);
        storeTriangles(0, 3, 2, Color.red, true);
        storeTriangles(5, 7, 4, Color.green, true);
        storeTriangles(5, 6, 7, Color.green, true);
        storeTriangles(3, 6, 2, Color.blue, true);
        storeTriangles(3, 7, 6, Color.blue, true);
        storeTriangles(4, 1, 5, Color.white, true);
        storeTriangles(4, 0, 1, Color.white, true);
        storeTriangles(1, 6, 5, Color.white, true);
        storeTriangles(1, 2, 6, Color.white, true);
        storeTriangles(4, 3, 0, Color.white, true);
        storeTriangles(4, 7, 3, Color.white, true);

        // Create 4 vertices spanning a large area at a fixed floor height (Y = -10)
        storeVertex(new Vector3(-3000, -50, -3000), Color.green, 10, false); // Index 0: Back-Left
        storeVertex(new Vector3( 3000, -50, -3000), Color.green, 10, false); // Index 1: Back-Right
        storeVertex(new Vector3( 3000, -50,  3000), Color.green, 10, false); // Index 2: Front-Right
        storeVertex(new Vector3(-3000, -50,  3000), Color.green, 10, false); // Index 3: Front-Left

// Connect the 4 vertices with 2 triangles to form a solid quad surface
        storeTriangles(8, 10, 9, Color.gray, true);
        storeTriangles(8, 11, 10, Color.gray, true);
    }

    public void storeVertex(Vector3 coordinates, Color color, int radius, boolean opacity) {

        int i = 0;
        while(vertices[i] != null) {
            i++;
            if (i + 1 == vertices.length) {
                return;
            }
        }

        vertices[i] = new Vertex(coordinates, color, radius, opacity);

    }

    public void storeLine(int index1, int index2, Color color, float thickness, boolean opacity) {

        int i = 0;
        while(lines[i] != null) {
            i++;
            if(i + 1 == lines.length) {
                return;
            }
        }

        lines[i] = new Line(index1, index2, color, thickness, opacity);
    }

    public void storeTriangles(int index1, int index2, int index3, Color color, boolean opacity) {

        int i = 0;
        while(triangles[i] != null) {
            i++;
            if(i + 1 == lines.length) {
                return;
            }
        }

        triangles[i] = new Triangle(index1, index2, index3, color, opacity);

    }
}
